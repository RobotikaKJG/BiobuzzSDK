package org.firstinspires.ftc.teamcode.Main.ShooterTelemetry;

import com.qualcomm.robotcore.util.RobotLog;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoWSD;

/**
 * Embedded HTTP + WebSocket server for shooter telemetry, reachable from a laptop on
 * the robot's WiFi at http://192.168.43.1:8765.
 *
 * Lifecycle: lazily started as a static singleton on first OpMode init and never
 * stopped — the RC app process persists across OpModes, so recorded sessions stay
 * downloadable whenever the robot is powered, not just mid-run.
 *
 * HTTP API (CORS: *):
 *   GET /api/status            -> {opModeRunning, opMode, startedAtMs}
 *   GET /api/sessions          -> [{name, size, mtimeMs}, ...] newest first
 *   GET /api/sessions/{name}   -> raw JSONL session file
 * WebSocket at /ws: on-connect hello (+header if a session is live), then
 * {"type":"batch","samples":[...]} every ~100 ms while an OpMode runs.
 * Viewer may send {"type":"cmd",...} frames; see {@link RemoteControl}.
 */
public class ShooterTelemetryServer extends NanoWSD {
    private static final String TAG = "ShooterTelemetrySrv";
    public static final int PORT = 8765;

    private static final long BATCH_PERIOD_MS = 100;
    private static final long PING_PERIOD_MS = 2000;
    private static final int SOCKET_TIMEOUT_MS = 5000;
    private static final int MAX_PENDING = 5000;

    private static volatile ShooterTelemetryServer instance;

    private final CopyOnWriteArrayList<LiveSocket> sockets = new CopyOnWriteArrayList<>();
    private final ConcurrentLinkedQueue<String> pending = new ConcurrentLinkedQueue<>();
    private volatile int pendingCount = 0; // approximate; only one producer (writer thread)

    private volatile String currentOpMode = null;
    private volatile long opModeStartedAtMs = 0;
    private volatile String currentHeaderJson = null;

    private ShooterTelemetryServer() {
        super(PORT);
    }

    /** Idempotent lazy start. Never throws (a dead server must not kill an OpMode). */
    public static void ensureStarted() {
        if (instance != null) return;
        synchronized (ShooterTelemetryServer.class) {
            if (instance != null) return;
            try {
                ShooterTelemetryServer s = new ShooterTelemetryServer();
                s.start(SOCKET_TIMEOUT_MS, true);
                s.startBroadcaster();
                instance = s;
                RobotLog.ii(TAG, "listening on port %d", PORT);
            } catch (Throwable t) {
                RobotLog.ee(TAG, t, "failed to start telemetry server (continuing without)");
            }
        }
    }

    public static ShooterTelemetryServer getInstanceOrNull() {
        return instance;
    }

    // ---------------- session hooks (called by ShooterLogger) ----------------

    public void onSessionStart(String opModeName, String headerJson) {
        currentOpMode = opModeName;
        opModeStartedAtMs = System.currentTimeMillis();
        currentHeaderJson = headerJson;
        pending.clear();
        pendingCount = 0;
        broadcast(headerJson);
    }

    public void onSessionEnd() {
        currentOpMode = null;
        currentHeaderJson = null;
        broadcast("{\"type\":\"sessionEnd\"}");
    }

    /** Queue one pre-serialized sample line for the next live batch. Non-blocking. */
    public void publishSample(String jsonLine) {
        if (sockets.isEmpty()) return; // nobody listening — don't accumulate
        if (pendingCount >= MAX_PENDING) {
            pending.poll();
        } else {
            pendingCount++;
        }
        pending.offer(jsonLine);
    }

    // ---------------- broadcaster ----------------

    private void startBroadcaster() {
        Thread t = new Thread(() -> {
            long lastPingMs = 0;
            while (true) {
                try {
                    Thread.sleep(BATCH_PERIOD_MS);
                    flushBatch();
                    long now = System.currentTimeMillis();
                    if (now - lastPingMs >= PING_PERIOD_MS) {
                        for (LiveSocket ws : sockets) {
                            try {
                                ws.ping(new byte[0]);
                            } catch (IOException e) {
                                closeQuietly(ws);
                            }
                        }
                        lastPingMs = now;
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                } catch (Throwable th) {
                    RobotLog.ee(TAG, th, "broadcaster error (continuing)");
                }
            }
        }, TAG + "-broadcast");
        t.setDaemon(true);
        t.start();
    }

    private void flushBatch() {
        if (pending.isEmpty() || sockets.isEmpty()) {
            if (sockets.isEmpty()) { pending.clear(); pendingCount = 0; }
            return;
        }
        StringBuilder sb = new StringBuilder(4096);
        sb.append("{\"type\":\"batch\",\"samples\":[");
        String line;
        boolean first = true;
        while ((line = pending.poll()) != null) {
            if (!first) sb.append(',');
            sb.append(line);
            first = false;
        }
        pendingCount = 0;
        sb.append("]}");
        broadcast(sb.toString());
    }

    private void broadcast(String message) {
        for (LiveSocket ws : sockets) {
            try {
                ws.send(message);
            } catch (IOException e) {
                closeQuietly(ws);
            }
        }
    }

    private void closeQuietly(LiveSocket ws) {
        sockets.remove(ws);
        try {
            ws.close(WebSocketFrame.CloseCode.GoingAway, "send failed", false);
        } catch (IOException ignored) {
        }
    }

    // ---------------- WebSocket ----------------

    @Override
    protected WebSocket openWebSocket(IHTTPSession handshake) {
        return new LiveSocket(handshake);
    }

    private class LiveSocket extends WebSocket {
        LiveSocket(IHTTPSession handshake) {
            super(handshake);
        }

        @Override
        protected void onOpen() {
            sockets.add(this);
            try {
                send("{\"type\":\"hello\",\"opModeRunning\":" + (currentOpMode != null)
                        + ",\"opMode\":" + jsonString(currentOpMode) + "}");
                if (currentHeaderJson != null) send(currentHeaderJson);
            } catch (IOException e) {
                closeQuietly(this);
            }
            RobotLog.ii(TAG, "ws client connected (%d total)", sockets.size());
        }

        @Override
        protected void onClose(WebSocketFrame.CloseCode code, String reason, boolean initiatedByRemote) {
            sockets.remove(this);
            RobotLog.ii(TAG, "ws client disconnected (%d left)", sockets.size());
        }

        @Override
        protected void onMessage(WebSocketFrame message) {
            try {
                RemoteControl.onMessage(message.getTextPayload());
            } catch (Throwable t) {
                RobotLog.ww(TAG, "cmd parse failed: %s", t.getMessage());
            }
        }

        @Override
        protected void onPong(WebSocketFrame pong) {
        }

        @Override
        protected void onException(IOException exception) {
            sockets.remove(this);
        }
    }

    // ---------------- HTTP ----------------

    @Override
    protected Response serveHttp(IHTTPSession session) {
        try {
            if (session.getMethod() == Method.OPTIONS) {
                return cors(newFixedLengthResponse(Response.Status.NO_CONTENT, "text/plain", ""));
            }
            String uri = session.getUri();
            if ("/api/status".equals(uri)) {
                String body = "{\"app\":\"shooter-telemetry\",\"opModeRunning\":" + (currentOpMode != null)
                        + ",\"opMode\":" + jsonString(currentOpMode)
                        + ",\"startedAtMs\":" + opModeStartedAtMs + "}";
                return cors(newFixedLengthResponse(Response.Status.OK, "application/json", body));
            }
            if ("/api/sessions".equals(uri)) {
                return cors(newFixedLengthResponse(Response.Status.OK, "application/json", listSessionsJson()));
            }
            if (uri.startsWith("/api/sessions/")) {
                return cors(serveSessionFile(uri.substring("/api/sessions/".length())));
            }
            if ("/".equals(uri)) {
                return cors(newFixedLengthResponse(Response.Status.OK, "text/plain",
                        "shooter-telemetry server. API: /api/status /api/sessions /api/sessions/{name}, WS at /ws"));
            }
            return cors(newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "not found"));
        } catch (Throwable t) {
            RobotLog.ee(TAG, t, "http error");
            return cors(newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", "error"));
        }
    }

    private String listSessionsJson() {
        File[] files = ShooterLogger.LOG_DIR.listFiles((d, n) -> n.endsWith(".jsonl"));
        List<File> list = files == null ? new ArrayList<>() : new ArrayList<>(Arrays.asList(files));
        list.sort(Comparator.comparingLong(File::lastModified).reversed());
        StringBuilder sb = new StringBuilder(1024);
        sb.append('[');
        for (int i = 0; i < list.size(); i++) {
            File f = list.get(i);
            if (i > 0) sb.append(',');
            sb.append("{\"name\":").append(jsonString(f.getName()))
                    .append(",\"size\":").append(f.length())
                    .append(",\"mtimeMs\":").append(f.lastModified()).append('}');
        }
        sb.append(']');
        return sb.toString();
    }

    private Response serveSessionFile(String name) throws IOException {
        // Reject anything that could escape the log dir.
        if (name.contains("/") || name.contains("\\") || name.contains("..") || !name.endsWith(".jsonl")) {
            return newFixedLengthResponse(Response.Status.BAD_REQUEST, "text/plain", "bad name");
        }
        File f = new File(ShooterLogger.LOG_DIR, name);
        if (!f.isFile()) {
            return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "no such session");
        }
        Response r = newFixedLengthResponse(Response.Status.OK, "application/x-ndjson",
                new FileInputStream(f), f.length());
        r.addHeader("Content-Disposition", "attachment; filename=\"" + f.getName() + "\"");
        return r;
    }

    private static Response cors(Response r) {
        r.addHeader("Access-Control-Allow-Origin", "*");
        r.addHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
        r.addHeader("Access-Control-Allow-Headers", "*");
        return r;
    }

    private static String jsonString(String s) {
        if (s == null) return "null";
        return '"' + s.replace("\\", "\\\\").replace("\"", "\\\"") + '"';
    }
}
