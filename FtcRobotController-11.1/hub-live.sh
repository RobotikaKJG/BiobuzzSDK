#!/usr/bin/env bash
# WiFi-only connect/disconnect for the shooter telemetry viewer (ftc-decode hub.sh style).
# Does NOT build or adb-install — just joins the hub AP, waits until :8765 answers, or restores WiFi.
#
#   ./hub-live.sh connect-live
#   ./hub-live.sh disconnect-live
set -uo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
[ -f "$SCRIPT_DIR/.env" ] && set -a && source "$SCRIPT_DIR/.env" && set +a

HUB_IP="${HUB_IP:-192.168.43.1}"
TELEMETRY_PORT="${TELEMETRY_PORT:-8765}"
FTC_SSID="${FTC_SSID:-24500-RC}"
FTC_PASS="${FTC_PASS:-LITBOT100}"
HOME_PASS="${HOME_PASS:-}"
LIVE_STATE="$SCRIPT_DIR/.live-wifi-state"
TIMEOUT=60

OS="$(uname -s)"
case "$OS" in
    Darwin)              WIFI_IF="${WIFI_IF:-en0}" ;;
    Linux)               WIFI_IF="${WIFI_IF:-wlan0}" ;;
    MINGW*|MSYS*|CYGWIN*) OS="Windows"; WIFI_IF="${WIFI_IF:-Wi-Fi}" ;;
    *)                   echo "Unsupported OS: $OS"; exit 1 ;;
esac

ts()  { date "+%H:%M:%S"; }
log() { echo "[$(ts)] $*"; }
logn(){ printf "\r[$(ts)] %s" "$*"; }

ping_hub_once() {
    case "$OS" in
        Darwin)  ping -c 1 -W 1000 "$HUB_IP" ;;
        Windows) ping.exe -n 1 -w 1000 "$HUB_IP" ;;
        *)       ping -c 1 -W 1 "$HUB_IP" ;;
    esac
}

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
        Windows) netsh.exe wlan connect name="$ssid" interface="$WIFI_IF" 2>&1 ;;
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

save_current_network() {
    SAVED_IP=$(get_ip || true)
    SAVED_NETWORK=$(wifi_current_ssid)
    log "Saved network: ssid='${SAVED_NETWORK:-none}' ip='${SAVED_IP:-none}'"
    if [[ "$SAVED_NETWORK" == *"not associated"* ]]; then
        if [[ "$SAVED_IP" == 172.20.10.* ]]; then
            SAVED_NETWORK="__hotspot__"
            log "Treating as iPhone Instant Hotspot"
        else
            SAVED_NETWORK=""
        fi
    fi
}

restore_wifi() {
    if [[ "$SAVED_NETWORK" == "__hotspot__" ]]; then
        log "Reconnecting to iPhone hotspot..."
        wifi_power off; sleep 1; wifi_power on
        local elapsed=0
        while [ $elapsed -lt 30 ]; do
            local ip; ip=$(get_ip || true)
            if [[ -n "$ip" && "$ip" == 172.20.10.* ]]; then
                log "iPhone hotspot reconnected ($ip)"; return 0
            fi
            sleep 1; elapsed=$((elapsed + 1))
        done
        log "Could not reconnect to iPhone hotspot — reconnect manually"
        return 1
    elif [[ -n "${SAVED_NETWORK:-}" ]]; then
        switch_wifi "$SAVED_NETWORK" "$HOME_PASS" 15 || \
            log "Could not reconnect to $SAVED_NETWORK — reconnect manually"
    else
        log "No previous network to restore"
    fi
}

probe_telemetry() {
    # Prefer curl; fall back to nc on the port.
    if command -v curl >/dev/null 2>&1; then
        curl -sf -m 2 "http://${HUB_IP}:${TELEMETRY_PORT}/api/status" 2>/dev/null
        return $?
    fi
    nc -z -w 2 "$HUB_IP" "$TELEMETRY_PORT" &>/dev/null
}

on_hub_subnet() {
    local ip="${1:-}"
    [[ "$ip" == 192.168.43.* ]]
}

cmd_status() {
    # Machine-readable JSON for the viewer — no WiFi changes.
    local ip ssid hub_ok=false telem_ok=false status_json=""
    ip=$(get_ip || true)
    ssid=$(wifi_current_ssid 2>/dev/null || true)
    if ping_hub_once &>/dev/null; then
        hub_ok=true
    fi
    if status_json=$(probe_telemetry); then
        telem_ok=true
    else
        status_json=""
    fi
    # Escape for JSON strings
    local ssid_j ip_j
    ssid_j=$(printf '%s' "${ssid:-}" | python3 -c 'import json,sys; print(json.dumps(sys.stdin.read()))' 2>/dev/null || echo "\"${ssid:-}\"")
    ip_j=$(printf '%s' "${ip:-}" | python3 -c 'import json,sys; print(json.dumps(sys.stdin.read()))' 2>/dev/null || echo "\"${ip:-}\"")
    local on_hub=false
    on_hub_subnet "$ip" && on_hub=true
    if [[ -z "$status_json" ]]; then
        status_json=null
    fi
    printf '{"onHubSubnet":%s,"ip":%s,"ssid":%s,"hubReachable":%s,"telemetryOk":%s,"telemetry":%s,"hub":"%s:%s"}\n' \
        "$on_hub" "$ip_j" "$ssid_j" "$hub_ok" "$telem_ok" "$status_json" "$HUB_IP" "$TELEMETRY_PORT"
}

cmd_connect_live() {
    local ip; ip=$(get_ip || true)
    local skipped=0

    # Auto-detect: already on Control Hub AP subnet → do not touch WiFi.
    if on_hub_subnet "$ip"; then
        skipped=1
        log "Already on hub subnet ($ip) — skipping WiFi switch"
        echo "SKIPPED_WIFI=1" > "$LIVE_STATE"
    else
        save_current_network
        {
            echo "SKIPPED_WIFI=0"
            echo "SAVED_NETWORK=$SAVED_NETWORK"
            echo "SAVED_IP=$SAVED_IP"
        } > "$LIVE_STATE"

        if ! switch_wifi "$FTC_SSID" "$FTC_PASS" 30; then
            log "Could not find $FTC_SSID — is the Control Hub powered on?"
            rm -f "$LIVE_STATE"
            exit 1
        fi

        logn "Waiting for IP "
        local elapsed=0
        ip=""
        while [ $elapsed -lt 20 ]; do
            ip=$(get_ip || true)
            if [[ -n "$ip" ]]; then
                echo ""; log "Got IP $ip"; break
            fi
            printf "."; sleep 1; elapsed=$((elapsed + 1))
        done
    fi

    logn "Pinging hub ($HUB_IP) "
    local elapsed=0
    while [ $elapsed -lt $TIMEOUT ]; do
        if ping_hub_once &>/dev/null; then
            echo ""; log "Hub reachable (${elapsed}s)"; break
        fi
        printf "."; sleep 1; elapsed=$((elapsed + 1))
    done
    if [ $elapsed -ge $TIMEOUT ]; then
        echo ""; log "Hub not reachable after ${TIMEOUT}s"
        log "Tip: Private Wi-Fi Address Off for $FTC_SSID, then retry"
        [[ "$skipped" -eq 1 ]] || rm -f "$LIVE_STATE"
        exit 1
    fi

    logn "Telemetry :${TELEMETRY_PORT} "
    elapsed=0
    local status_json=""
    while [ $elapsed -lt 30 ]; do
        if status_json=$(probe_telemetry); then
            echo ""
            log "Telemetry server OK on ${HUB_IP}:${TELEMETRY_PORT}"
            if [[ -n "$status_json" ]]; then
                log "  status: $status_json"
            fi
            log "Ready — viewer will open WebSocket to ws://${HUB_IP}:${TELEMETRY_PORT}/ws"
            if echo "$status_json" | grep -q '"opModeRunning":false'; then
                log "Note: no OpMode running yet — connect is OK; start an OpMode to stream samples"
            fi
            exit 0
        fi
        printf "."; sleep 1; elapsed=$((elapsed + 1))
    done
    echo ""
    log "Hub pings OK but :${TELEMETRY_PORT} not answering"
    log "  Start any OpMode once (server starts on Dependencies init), then retry Connect"
    exit 1
}

cmd_disconnect_live() {
    if [[ -f "$LIVE_STATE" ]]; then
        # shellcheck disable=SC1090
        source "$LIVE_STATE"
        rm -f "$LIVE_STATE"
    else
        SKIPPED_WIFI=1
        SAVED_NETWORK=""
        SAVED_IP=""
        log "No saved live-wifi state"
    fi

    if [[ "${SKIPPED_WIFI:-0}" == "1" ]]; then
        log "Connect did not switch WiFi — leaving current network as-is"
        exit 0
    fi
    restore_wifi
}

case "${1:-}" in
    connect-live)    cmd_connect_live ;;
    disconnect-live) cmd_disconnect_live ;;
    status)          cmd_status ;;
    *)
        echo "Usage: hub-live.sh <connect-live|disconnect-live|status>"
        exit 1
        ;;
esac
