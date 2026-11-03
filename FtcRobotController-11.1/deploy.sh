#!/usr/bin/env bash
# Barebones deploy: build APK -> switch to hub WiFi -> adb install -> disconnect -> restore WiFi.
set -uo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
[ -f "$SCRIPT_DIR/.env" ] && set -a && source "$SCRIPT_DIR/.env" && set +a

HUB_IP="192.168.43.1"
HUB="$HUB_IP:5555"
APK="TeamCode/build/outputs/apk/debug/TeamCode-debug.apk"

FTC_SSID="${FTC_SSID:-24500-RC}"
FTC_PASS="${FTC_PASS:-LITBOT100}"
HOME_PASS="${HOME_PASS:-}"

OS="$(uname -s)"
case "$OS" in
    Darwin)              WIFI_IF="${WIFI_IF:-en0}" ;;
    Linux)               WIFI_IF="${WIFI_IF:-wlan0}" ;;
    MINGW*|MSYS*|CYGWIN*) OS="Windows"; WIFI_IF="${WIFI_IF:-Wi-Fi}" ;;
    *)                   echo "Unsupported OS: $OS"; exit 1 ;;
esac

TIMEOUT=60
BUILD_TIMEOUT="${BUILD_TIMEOUT:-300}"     # gradle assembleDebug
INSTALL_TIMEOUT="${INSTALL_TIMEOUT:-120}" # adb install (wireless can hang forever)
LOGCAT_TIMEOUT="${LOGCAT_TIMEOUT:-45}"    # logcat -d / matchlog pull
MAX_DEPLOY_LOGS=20
LOG_DIR="$SCRIPT_DIR/logs"
mkdir -p "$LOG_DIR"

# Tee full run (stdout+stderr) into a timestamped deploy log so we can diagnose
# offline after the robot WiFi drop (no internet). Keep a rolling window.
DEPLOY_LOG="$LOG_DIR/deploy-$(date +%Y%m%d-%H%M%S).log"
exec > >(tee -a "$DEPLOY_LOG") 2>&1
prune_deploy_logs() {
    # shellcheck disable=SC2012
    ls -1t "$LOG_DIR"/deploy-*.log 2>/dev/null | tail -n +"$((MAX_DEPLOY_LOGS + 1))" | while read -r f; do
        rm -f "$f"
    done
}
prune_deploy_logs
echo "=== deploy log: $DEPLOY_LOG ==="
echo "=== timeouts: build=${BUILD_TIMEOUT}s install=${INSTALL_TIMEOUT}s logcat=${LOGCAT_TIMEOUT}s hub-ping=${TIMEOUT}s ==="

ts()  { date "+%H:%M:%S"; }
log() { echo "[$(ts)] $*"; }
logn(){ printf "\r[$(ts)] %s" "$*"; }

# Portable timeout + heartbeat. macOS has no GNU timeout by default.
# Runs "$@", prints a CLI heartbeat every HEARTBEAT_SEC, kills on timeout.
# Sets RUN_OUTPUT to combined stdout/stderr. Returns command exit (124 = timeout).
HEARTBEAT_SEC=5
RUN_OUTPUT=""
run_with_timeout() {
    local timeout_sec=$1
    local desc=$2
    shift 2
    local start_s end_s elapsed pid rc out_file last_beat=0
    out_file=$(mktemp "${TMPDIR:-/tmp}/deploy-run.XXXXXX")
    start_s=$(date +%s)

    log "$desc — starting (timeout ${timeout_sec}s)"
    "$@" >"$out_file" 2>&1 &
    pid=$!

    while kill -0 "$pid" 2>/dev/null; do
        end_s=$(date +%s)
        elapsed=$((end_s - start_s))
        if (( elapsed >= timeout_sec )); then
            echo ""
            log "$desc — TIMEOUT after ${elapsed}s (limit ${timeout_sec}s), killing pid $pid"
            kill "$pid" 2>/dev/null || true
            sleep 1
            kill -9 "$pid" 2>/dev/null || true
            wait "$pid" 2>/dev/null || true
            RUN_OUTPUT=$(cat "$out_file" 2>/dev/null || true)
            rm -f "$out_file"
            return 124
        fi
        if (( elapsed - last_beat >= HEARTBEAT_SEC )); then
            last_beat=$elapsed
            log "$desc — still running… ${elapsed}s / ${timeout_sec}s"
        fi
        sleep 1
    done

    wait "$pid"
    rc=$?
    end_s=$(date +%s)
    elapsed=$((end_s - start_s))
    RUN_OUTPUT=$(cat "$out_file" 2>/dev/null || true)
    rm -f "$out_file"
    if [[ $rc -eq 0 ]]; then
        log "$desc — done (${elapsed}s)"
    else
        log "$desc — failed exit=$rc (${elapsed}s)"
    fi
    return $rc
}

# macOS: ping -W is milliseconds. Linux: seconds. Windows: ping.exe -w is ms.
ping_hub_once() {
    case "$OS" in
        Darwin)  ping -c 1 -W 1000 "$HUB_IP" ;;
        Windows) ping.exe -n 1 -w 1000 "$HUB_IP" ;;
        *)       ping -c 1 -W 1 "$HUB_IP" ;;
    esac
}

diag_net() {
    local ssid ip
    ssid=$(wifi_current_ssid 2>/dev/null || true)
    ip=$(get_ip || true)
    log "  diag: ssid='${ssid:-?}' ip='${ip:-none}' if=$WIFI_IF hub=$HUB_IP"
    if [[ "$OS" == "Darwin" ]]; then
        route -n get "$HUB_IP" 2>&1 | sed 's/^/  diag: /' || true
        arp -n "$HUB_IP" 2>&1 | sed 's/^/  arp: /' || true
        # Better association check than networksetup (often lies as "not associated")
        ifconfig "$WIFI_IF" 2>&1 | grep -E 'status:|inet |ether ' | sed 's/^/  ifconfig: /' || true
    fi
}

# DHCP + "No route to host" to .1 usually means L2/ARP or macOS Local Network privacy,
# not a wrong password. Other machines working is a strong signal for this Mac's settings.
hint_mac_local_network() {
    [[ "$OS" == "Darwin" ]] || return 0
    log "── macOS tip (other PCs work but this Mac can't reach hub) ──"
    log "You have a 192.168.43.x IP (AP DHCP OK) but ping .1 fails with 'No route to host'."
    log "That is usually NOT ADB — the Mac never reaches the hub at layer 2/3."
    log "Try, in order:"
    log "  1) System Settings → Privacy & Security → Local Network"
    log "     → enable Terminal (and iTerm/Cursor/Warp if you use them), then reopen the app"
    log "  2) System Settings → Wi‑Fi → 24500-RC → Details"
    log "     → turn OFF 'Limit IP Address Tracking' / Private Wi‑Fi Address"
    log "  3) Forget 24500-RC, reconnect manually, confirm a checkmark, then:"
    log "       ping -c 3 $HUB_IP"
    log "  4) Only after ping works: ./deploy.sh"
}

# ── WiFi helpers ─────────────────────────────────────────────────────────────

wifi_connect() {
    local ssid="$1" pass="${2:-}"
    case "$OS" in
        Darwin)
            if [[ -n "$pass" ]]; then
                networksetup -setairportnetwork "$WIFI_IF" "$ssid" "$pass" 2>&1
            else
                networksetup -setairportnetwork "$WIFI_IF" "$ssid" 2>&1
            fi ;;
        Linux)
            if [[ -n "$pass" ]]; then
                nmcli device wifi connect "$ssid" password "$pass" ifname "$WIFI_IF" 2>&1
            else
                nmcli device wifi connect "$ssid" ifname "$WIFI_IF" 2>&1
            fi ;;
        Windows)
            netsh.exe wlan connect name="$ssid" interface="$WIFI_IF" 2>&1 ;;
    esac
}

wifi_current_ssid() {
    case "$OS" in
        Darwin)  networksetup -getairportnetwork "$WIFI_IF" 2>&1 | sed 's/.*: //' ;;
        Linux)   nmcli -t -f active,ssid dev wifi 2>/dev/null | grep '^yes:' | cut -d: -f2 ;;
        Windows) netsh.exe wlan show interfaces 2>/dev/null | grep '^ *SSID' | head -1 | sed 's/.*: //' ;;
    esac
}

get_ip() {
    case "$OS" in
        Darwin)  ipconfig getifaddr "$WIFI_IF" 2>/dev/null ;;
        Linux)   ip -4 addr show "$WIFI_IF" 2>/dev/null | grep -oP 'inet \K[^/]+' ;;
        Windows) ipconfig.exe 2>/dev/null | sed -n "/adapter $WIFI_IF/,/^[^ ]/p" | grep 'IPv4' | sed 's/.*: //' | tr -d '\r' ;;
    esac
}

wifi_power() {
    local state="$1"
    case "$OS" in
        Darwin)  networksetup -setairportpower "$WIFI_IF" "$state" &>/dev/null ;;
        Linux)   nmcli radio wifi "$state" &>/dev/null ;;
        Windows)
            if [[ "$state" == "off" ]]; then
                netsh.exe interface set interface "$WIFI_IF" disabled &>/dev/null
            else
                netsh.exe interface set interface "$WIFI_IF" enabled &>/dev/null
            fi ;;
    esac
}

switch_wifi() {
    local ssid="$1" pass="${2:-}" max="$3" result
    logn "WiFi -> $ssid "
    for _ in $(seq 1 "$max"); do
        result=$(wifi_connect "$ssid" "$pass")
        if [[ "$result" != *"Could not find"* && "$result" != *"Failed"* && "$result" != *"Error"* ]]; then
            echo ""; log "WiFi -> $ssid OK"; return 0
        fi
        printf "."; sleep 2
    done
    echo ""; return 1
}

SAVED_NETWORK=""
SAVED_IP=""

save_current_network() {
    SAVED_IP=$(get_ip || true)
    SAVED_NETWORK=$(wifi_current_ssid)
    log "Saved network: ssid='${SAVED_NETWORK:-none}' ip='${SAVED_IP:-none}'"
    if [[ "$SAVED_NETWORK" == *"not associated"* ]]; then
        # iPhone Instant Hotspot isn't visible as a regular SSID; detect by IP range
        if [[ "$SAVED_IP" == 172.20.10.* ]]; then
            SAVED_NETWORK="__hotspot__"
            log "Treating as iPhone Instant Hotspot"
        else
            SAVED_NETWORK=""
        fi
    fi
}

graceful_disconnect() {
    adb disconnect "$HUB" &>/dev/null
    sleep 1
}

restore_wifi() {
    echo ""
    graceful_disconnect
    if [[ "$SAVED_NETWORK" == "__hotspot__" ]]; then
        # iPhone Instant Hotspot reconnects automatically once FTC is dropped
        log "Reconnecting to iPhone hotspot..."
        wifi_power off
        sleep 1
        wifi_power on
        local elapsed=0
        while [ $elapsed -lt 30 ]; do
            local ip
            ip=$(get_ip || true)
            if [[ -n "$ip" && "$ip" == 172.20.10.* ]]; then
                log "iPhone hotspot reconnected ($ip)"
                return
            fi
            sleep 1
            elapsed=$((elapsed + 1))
        done
        log "Could not reconnect to iPhone hotspot - reconnect manually"
    elif [[ -n "$SAVED_NETWORK" ]]; then
        switch_wifi "$SAVED_NETWORK" "$HOME_PASS" 15 || \
            log "Could not reconnect to $SAVED_NETWORK - reconnect manually"
    else
        log "No previous network to restore"
    fi
    log "Deploy log saved at: $DEPLOY_LOG"
}

# ── Connect to hub ───────────────────────────────────────────────────────────

connect_hub() {
    save_current_network

    if ! switch_wifi "$FTC_SSID" "$FTC_PASS" 30; then
        log "Could not find $FTC_SSID - is the Control Hub powered on?"
        exit 1
    fi
    trap restore_wifi EXIT

    # DHCP can lag behind association — wait briefly for an IP before pinging.
    logn "Waiting for IP "
    local elapsed=0 ip=""
    while [ $elapsed -lt 20 ]; do
        ip=$(get_ip || true)
        if [[ -n "$ip" ]]; then
            echo ""; log "Got IP $ip"; break
        fi
        printf "."; sleep 1; elapsed=$((elapsed + 1))
    done
    if [[ -z "$ip" ]]; then
        echo ""; log "No IP on $WIFI_IF after ${elapsed}s (still trying to ping hub)"
        diag_net
    fi

    logn "Pinging hub ($HUB_IP) "
    elapsed=0
    local ping_out=""
    while [ $elapsed -lt $TIMEOUT ]; do
        if ping_out=$(ping_hub_once 2>&1); then
            echo ""; log "Hub reachable (${elapsed}s)"
            echo "$ping_out" | sed 's/^/  ping: /' | head -5
            break
        fi
        printf "."
        # Every 5s dump why ping failed (saved to deploy log; no internet needed).
        if (( elapsed % 5 == 4 )); then
            echo ""
            log "ping still failing at ${elapsed}s — diagnostics:"
            diag_net
            echo "$ping_out" | sed 's/^/  ping: /' | tail -8
            logn "Pinging hub ($HUB_IP) "
        fi
        sleep 1; elapsed=$((elapsed + 1))
    done
    if [ $elapsed -ge $TIMEOUT ]; then
        echo ""; log "Hub not reachable after ${TIMEOUT}s"
        diag_net
        log "Last ping output:"; echo "$ping_out" | sed 's/^/  ping: /'
        if echo "$ping_out" | grep -q 'No route to host'; then
            hint_mac_local_network
        fi
        exit 1
    fi

    # Port 5555 must be open before adb connect (ftc-decode hub.sh style).
    logn "ADB port 5555 "
    local port_open=false
    for _ in $(seq 1 10); do
        case "$OS" in
            Windows)
                powershell.exe -Command "try { \$c = New-Object Net.Sockets.TcpClient('$HUB_IP',5555); \$c.Close(); exit 0 } catch { exit 1 }" &>/dev/null \
                    && port_open=true ;;
            *)
                nc -z -w 2 "$HUB_IP" 5555 &>/dev/null && port_open=true ;;
        esac
        if $port_open; then
            echo ""; log "Port 5555 open"
            connect_adb
            return
        fi
        printf "."; sleep 1
    done
    echo ""
    log "Port 5555 not responding — adbd may be stuck"
    log "  Unplug battery, wait 5s, replug, then re-run ./deploy.sh"
    diag_net
    exit 1
}

# ── ADB (from ftc-decode hub.sh: reuse / soft reconnect / hard reset) ────────

adb_state() { adb -s "$HUB" get-state 2>&1 || echo "error"; }

adb_wait_online() {
    local max_wait=$1
    local last_state=""
    for _ in $(seq 1 "$max_wait"); do
        local state
        state=$(adb_state)
        if [[ "$state" == "device" ]]; then
            echo ""
            return 0
        fi
        if [[ "$state" != "$last_state" ]]; then
            [[ -n "$last_state" ]] && echo ""
            logn "    adb:$state "
            last_state="$state"
        else
            printf "."
        fi
        sleep 1
    done
    echo ""
    return 1
}

connect_adb() {
    logn "ADB "
    local state offline_count=0
    state=$(adb_state)

    if [[ "$state" == "device" ]]; then
        echo ""
        log "ADB connected (reused)"
        adb -s "$HUB" devices -l 2>&1 | sed 's/^/  adb: /'
        return
    fi

    # Not known — normal case after graceful disconnect
    if [[ "$state" == *"not found"* ]]; then
        local adb_out
        adb_out=$(adb connect "$HUB" 2>&1) || true
        log "adb connect: $adb_out"
        if adb_wait_online 6; then
            log "ADB connected"
            adb -s "$HUB" devices -l 2>&1 | sed 's/^/  adb: /'
            return
        fi
    fi

    state=$(adb_state)
    [[ "$state" == *"offline"* ]] && offline_count=$((offline_count + 1))

    for attempt in 1 2; do
        logn "  reconnect #$attempt "
        adb disconnect "$HUB" &>/dev/null; sleep 1
        local adb_out
        adb_out=$(adb connect "$HUB" 2>&1) || true
        log "adb connect: $adb_out"
        if adb_wait_online 6; then
            log "ADB connected (reconnect #$attempt)"
            adb -s "$HUB" devices -l 2>&1 | sed 's/^/  adb: /'
            return
        fi
        state=$(adb_state)
        [[ "$state" == *"offline"* ]] && offline_count=$((offline_count + 1))
        if [[ $offline_count -ge 3 ]]; then
            log "adbd stuck offline — unplug battery, wait 5s, replug"
            exit 1
        fi
        sleep 2
    done

    for attempt in 1 2 3; do
        logn "  hard reset #$attempt "
        adb kill-server &>/dev/null; sleep 2
        adb start-server &>/dev/null 2>&1; sleep 2
        local adb_out
        adb_out=$(adb connect "$HUB" 2>&1) || true
        log "adb connect: $adb_out"
        if adb_wait_online 10; then
            log "ADB connected (hard reset #$attempt)"
            adb -s "$HUB" devices -l 2>&1 | sed 's/^/  adb: /'
            return
        fi
        state=$(adb_state)
        [[ "$state" == *"offline"* ]] && offline_count=$((offline_count + 1))
        if [[ $offline_count -ge 3 ]]; then
            log "adbd stuck offline — unplug battery, wait 5s, replug"
            exit 1
        fi
        sleep 3
    done

    log "ADB could not connect — unplug battery, wait 5s, replug"
    adb devices 2>&1 | sed 's/^/  adb: /'
    exit 1
}

# ── Install helpers ──────────────────────────────────────────────────────────

# Wireless adb install often hangs forever if the RC app is mid-OpMode / holding
# the package. Force-stop first; retry once after a soft adb reconnect on timeout.
RC_PACKAGE="${RC_PACKAGE:-com.qualcomm.ftcrobotcontroller}"

prepare_for_install() {
    local apk_bytes apk_mb
    if [[ -f "$APK" ]]; then
        apk_bytes=$(wc -c < "$APK" | tr -d ' ')
        apk_mb=$(awk "BEGIN { printf \"%.1f\", $apk_bytes / 1000000 }")
        log "APK ready: $APK (${apk_mb} MB)"
    fi
    log "Force-stopping $RC_PACKAGE (avoids install hangs while OpMode runs)"
    adb -s "$HUB" shell am force-stop "$RC_PACKAGE" 2>&1 | sed 's/^/  adb: /' || true
    # Brief settle so PackageManager releases the APK.
    sleep 1
    log "ADB state before install: $(adb_state)"
}

install_apk() {
    local attempt=1 max_attempts=2 install_exit=0
    prepare_for_install

    while (( attempt <= max_attempts )); do
        log "Install attempt $attempt/$max_attempts → adb install -r -g"
        if run_with_timeout "$INSTALL_TIMEOUT" "adb install" \
            adb -s "$HUB" install -r -g "$APK"
        then
            install_exit=0
        else
            install_exit=$?
        fi

        if [[ -n "$RUN_OUTPUT" ]]; then
            echo "$RUN_OUTPUT" | while IFS= read -r line; do
                [[ -n "$line" ]] && log "  install: $line"
            done
        fi

        if [[ $install_exit -eq 0 && "$RUN_OUTPUT" == *"Success"* ]]; then
            log "Install Success"
            return 0
        fi

        if [[ $install_exit -eq 124 ]]; then
            log "Install timed out — common when RC is busy or wireless ADB stalls"
            log "  Tip: stop OpMode on DS, or unplug battery 5s, then re-run ./deploy.sh"
            if (( attempt < max_attempts )); then
                log "Soft-reconnect ADB and retry once…"
                adb disconnect "$HUB" &>/dev/null
                sleep 1
                adb connect "$HUB" 2>&1 | sed 's/^/  adb: /' || true
                adb_wait_online 8 || true
                prepare_for_install
            fi
        else
            log "Install failed (exit $install_exit)"
        fi
        attempt=$((attempt + 1))
    done
    return 1
}

# ── Main ─────────────────────────────────────────────────────────────────────

log "── Deploy start ──"
log "Hub $HUB  SSID=$FTC_SSID  APK=$APK"

# Baked into TeamCode BuildInfo.DEPLOYED and shown on Driver Station telemetry.
export DEPLOY_STAMP
DEPLOY_STAMP="$(date '+%Y-%m-%d %H:%M:%S')"
log "Deploy stamp: $DEPLOY_STAMP (Driver Hub will show this after install)"

if ! run_with_timeout "$BUILD_TIMEOUT" "gradle assembleDebug" ./gradlew assembleDebug -q; then
    if [[ -n "$RUN_OUTPUT" ]]; then
        echo "$RUN_OUTPUT" | tail -40 | sed 's/^/  gradle: /'
    fi
    log "Build FAILED — aborting before WiFi switch"
    exit 1
fi
if [[ ! -f "$APK" ]]; then
    log "APK missing: $APK"
    exit 1
fi

log "── Connecting to Control Hub WiFi ──"
connect_hub

log "── Installing APK over wireless ADB ──"
if ! install_apk; then
    log "Install FAILED after retries"
    exit 1
fi
log "Deploy complete!"

# Still connected here (the EXIT trap restores WiFi afterwards). Pull the logcat
# ring buffer — RobotLog output: TurretThread / ControlThread / DriveLoop /
# CycleTimer — from the run *before* this deploy, then clear it so the next run
# logs cleanly. No arguments needed; this happens every deploy.
LOG_FILE="$LOG_DIR/robot-$(date +%Y%m%d-%H%M%S).log"
log "── Pulling robot logs ──"
if run_with_timeout "$LOGCAT_TIMEOUT" "adb logcat -d" \
    adb -s "$HUB" logcat -d
then
    printf '%s\n' "$RUN_OUTPUT" > "$LOG_FILE"
    log "Pulled $(wc -l < "$LOG_FILE" 2>/dev/null | tr -d ' ') lines -> logs/$(basename "$LOG_FILE")"
else
    log "logcat pull timed out / failed — continuing"
    : > "$LOG_FILE"
fi
recent=$(grep -E "TurretThread|ControlThread|DriveLoop|CycleTimer|ShooterTelemetry" "$LOG_FILE" 2>/dev/null | tail -30)
if [[ -n "$recent" ]]; then
    log "── Recent robot-loop log lines ──"
    echo "$recent"
fi

# Also pull the FTC per-OpMode match logs — complete per-run logcat dumps the SDK
# writes on opmode stop (not subject to ring-buffer rollover like `logcat -d`).
if run_with_timeout "$LOGCAT_TIMEOUT" "adb pull matchlogs" \
    adb -s "$HUB" pull /storage/emulated/0/FIRST/matchlogs "$LOG_DIR/"
then
    log "Pulled match logs -> logs/matchlogs/"
else
    log "No match logs found (or pull timed out)"
fi

adb -s "$HUB" logcat -c 2>/dev/null || true   # clear buffer so the next run starts clean
log "── Deploy finished ──"
log "Full deploy transcript: $DEPLOY_LOG"
