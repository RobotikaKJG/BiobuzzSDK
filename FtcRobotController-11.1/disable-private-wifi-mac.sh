#!/usr/bin/env bash
# Turn OFF macOS "Private Wi-Fi Address" for the Control Hub AP (or a given SSID).
# Needs sudo. Prefers direct plist reads (python / plutil) over `defaults`, which
# often fails under TCC even when the file exists and is readable as root.
#
# Usage (from FtcRobotController-11.1/):
#   ./disable-private-wifi-mac.sh              # default SSID 24500-RC
#   ./disable-private-wifi-mac.sh 'MyHub-RC'
#   ./disable-private-wifi-mac.sh --list
#   ./disable-private-wifi-mac.sh --no-bounce
set -euo pipefail

SSID="${FTC_SSID:-24500-RC}"
BOUNCE_WIFI=1
LIST_ONLY=0
WIFI_IF="${WIFI_IF:-en0}"
DOMAIN="/Library/Preferences/com.apple.wifi.known-networks"
PLIST="${DOMAIN}.plist"

for arg in "$@"; do
    case "$arg" in
        --no-bounce) BOUNCE_WIFI=0 ;;
        --list) LIST_ONLY=1 ;;
        -h|--help)
            sed -n '2,10p' "$0"
            exit 0
            ;;
        *)
            SSID="$arg"
            ;;
    esac
done

if [[ "$(uname -s)" != "Darwin" ]]; then
    echo "This script is macOS-only."
    exit 1
fi

if [[ $EUID -ne 0 ]]; then
    echo "Re-running with sudo (password prompt)…"
    exec sudo -- "$0" "$@"
fi

ssid_key() {
    echo "wifi.network.ssid.${1}"
}

print_fda_help() {
    cat <<'EOF'

Full Disk Access is likely blocking reads of:
  /Library/Preferences/com.apple.wifi.known-networks.plist

Grant Full Disk Access to BOTH (Cursor's integrated terminal needs Cursor.app):

  1. System Settings → Privacy & Security → Full Disk Access
  2. Enable or add (+):
       • Terminal.app  (/System/Applications/Utilities/Terminal.app)
       • Cursor.app    (/Applications/Cursor.app — or your install path)
  3. Quit Cursor completely (Cmd+Q), reopen, then re-run:
       cd FtcRobotController-11.1
       ./disable-private-wifi-mac.sh --list

If Cursor still fails, run the same command in Apple Terminal after granting FDA there.
EOF
}

is_tcc_block() {
    local msg="${1:-}"
    [[ "$msg" == *"Operation not permitted"* ]] \
        || [[ "$msg" == *"Permission denied"* ]] \
        || [[ "$msg" == *"don’t have permission"* ]] \
        || [[ "$msg" == *"don't have permission"* ]] \
        || [[ "$msg" == *"couldn’t be opened"* ]] \
        || [[ "$msg" == *"couldn't be opened"* ]] \
        || [[ "$msg" == *"[Errno 1]"* ]]
}

# Try to open the plist. Sets READ_METHOD on success.
# On failure, prints per-method diagnostics and sets LAST_READ_ERR / SAW_TCC.
# Must not run in a command-substitution subshell (SAW_TCC must persist).
LAST_READ_ERR=""
SAW_TCC=0
READ_METHOD=""
plist_can_read() {
    local err
    LAST_READ_ERR=""
    SAW_TCC=0
    READ_METHOD=""

    # 1) Python plistlib — most reliable for binary plists + nested keys
    if err="$(python3 -c "
import plistlib
with open(r'''$PLIST''', 'rb') as f:
    d = plistlib.load(f)
print(len(d))
" 2>&1)"; then
        READ_METHOD="python-plistlib"
        return 0
    fi
    echo "  python-plistlib: FAIL — $err" >&2
    LAST_READ_ERR="python: $err"
    is_tcc_block "$err" && SAW_TCC=1

    # 2) plutil -p
    err="$(plutil -p "$PLIST" 2>&1 | head -c 400 || true)"
    if [[ -n "$err" ]] && ! is_tcc_block "$err" && [[ "$err" != *"Error"* ]]; then
        READ_METHOD="plutil-p"
        return 0
    fi
    echo "  plutil -p: FAIL — ${err:-no output}" >&2
    LAST_READ_ERR="plutil: $err"
    is_tcc_block "$err" && SAW_TCC=1

    # 3) plutil -convert xml1 from path
    if plutil -convert xml1 -o /dev/null -- "$PLIST" 2>/tmp/disable-private-wifi-mac.plutil.err; then
        READ_METHOD="plutil-convert"
        return 0
    fi
    err="$(cat /tmp/disable-private-wifi-mac.plutil.err 2>/dev/null || true)"
    echo "  plutil -convert: FAIL — $err" >&2
    LAST_READ_ERR="plutil-convert: $err"
    is_tcc_block "$err" && SAW_TCC=1

    # 4) /bin/cat + plutil (avoid shell cat→bat aliases)
    if /bin/cat "$PLIST" 2>/tmp/disable-private-wifi-mac.cat.err \
        | plutil -convert xml1 -o /dev/null -- - 2>/tmp/disable-private-wifi-mac.cat2.err; then
        READ_METHOD="cat+plutil"
        return 0
    fi
    err="$(cat /tmp/disable-private-wifi-mac.cat.err /tmp/disable-private-wifi-mac.cat2.err 2>/dev/null || true)"
    echo "  /bin/cat+plutil: FAIL — $err" >&2
    LAST_READ_ERR="cat+plutil: $err"
    is_tcc_block "$err" && SAW_TCC=1

    # 5) defaults export — with and without .plist suffix
    for domain in "$DOMAIN" "$PLIST"; do
        if defaults export "$domain" - >/tmp/disable-private-wifi-mac.export.xml 2>/tmp/disable-private-wifi-mac.export.err \
            && [[ -s /tmp/disable-private-wifi-mac.export.xml ]] \
            && [[ "$(wc -c </tmp/disable-private-wifi-mac.export.xml)" -gt 200 ]]; then
            READ_METHOD="defaults-export ($domain)"
            return 0
        fi
        err="$(cat /tmp/disable-private-wifi-mac.export.err 2>/dev/null || true)"
        echo "  defaults export ${domain}: FAIL — ${err:-empty/failed}" >&2
        LAST_READ_ERR="defaults export ${domain}: ${err:-empty/failed}"
        is_tcc_block "$err" && SAW_TCC=1
    done

    return 1
}

list_known_ssids() {
    # Prefer python on the live file
    if python3 - "$PLIST" <<'PY' 2>/dev/null
import plistlib, sys
path = sys.argv[1]
with open(path, "rb") as f:
    data = plistlib.load(f)
prefix = "wifi.network.ssid."
for s in sorted(k[len(prefix):] for k in data if isinstance(k, str) and k.startswith(prefix)):
    print(s)
PY
    then
        return 0
    fi

    # Fallback: defaults export + xmllint (domain path quirks)
    local xml
    xml="$(mktemp)"
    for domain in "$DOMAIN" "$PLIST"; do
        if defaults export "$domain" - >"$xml" 2>/dev/null && [[ -s "$xml" ]]; then
            xmllint --xpath '/plist/dict/key/text()' "$xml" 2>/dev/null \
                | sed 's/^wifi\.network\.ssid\.//' \
                | grep -v '^$' \
                | sort -u
            rm -f "$xml"
            return 0
        fi
    done
    rm -f "$xml"
    return 1
}

read_mode() {
    local ssid="$1" key
    key="$(ssid_key "$ssid")"
    if python3 - "$PLIST" "$ssid" <<'PY' 2>/dev/null
import plistlib, sys
path, ssid = sys.argv[1], sys.argv[2]
key = "wifi.network.ssid." + ssid
with open(path, "rb") as f:
    data = plistlib.load(f)
entry = data.get(key)
if isinstance(entry, dict):
    mode = entry.get("PrivateMACAddressModeUserSetting")
    if mode is not None:
        print(mode)
PY
    then
        return 0
    fi
    defaults read "$DOMAIN" "$key" 2>/dev/null \
        | awk -F' = ' '/PrivateMACAddressModeUserSetting/{gsub(/[";]/,"",$2); print $2; exit}' \
        || true
}

network_exists() {
    local ssid="$1" key rc=0
    key="$(ssid_key "$ssid")"
    python3 - "$PLIST" "$ssid" <<'PY' 2>/dev/null || rc=$?
import plistlib, sys
path, ssid = sys.argv[1], sys.argv[2]
key = "wifi.network.ssid." + ssid
try:
    with open(path, "rb") as f:
        data = plistlib.load(f)
except Exception:
    sys.exit(2)  # unreadable
sys.exit(0 if key in data else 1)
PY
    [[ $rc -eq 0 ]] && return 0
    [[ $rc -eq 1 ]] && return 1
    # Python could not open the file — try defaults
    defaults read "$DOMAIN" "$key" >/dev/null 2>&1
}

set_private_mac_off() {
    local ssid="$1"
    local key
    key="$(ssid_key "$ssid")"

    # Prefer python: hyphen-safe key, merges into existing SSID dict
    if python3 - "$PLIST" "$ssid" <<'PY'
import plistlib, sys, os
path, ssid = sys.argv[1], sys.argv[2]
key = "wifi.network.ssid." + ssid
with open(path, "rb") as f:
    data = plistlib.load(f)
if key not in data or not isinstance(data[key], dict):
    print(f"missing or non-dict entry for {key}", file=sys.stderr)
    sys.exit(1)
data[key]["PrivateMACAddressModeUserSetting"] = "off"
tmp = path + ".tmp." + str(os.getpid())
with open(tmp, "wb") as f:
    plistlib.dump(data, f, fmt=plistlib.FMT_BINARY)
os.chmod(tmp, 0o600)
os.replace(tmp, path)
print("python-plistlib")
PY
    then
        return 0
    fi

    if defaults write "$DOMAIN" "$key" -dict-add PrivateMACAddressModeUserSetting -string off \
        2>/tmp/disable-private-wifi-mac.write.err; then
        echo "defaults-write"
        return 0
    fi

    if /usr/libexec/PlistBuddy -c "Set :'${key}':PrivateMACAddressModeUserSetting off" "$PLIST" \
            2>/tmp/disable-private-wifi-mac.write.err \
        || /usr/libexec/PlistBuddy -c "Add :'${key}':PrivateMACAddressModeUserSetting string off" "$PLIST" \
            2>/tmp/disable-private-wifi-mac.write.err; then
        echo "PlistBuddy"
        return 0
    fi

    echo "All write methods failed." >&2
    cat /tmp/disable-private-wifi-mac.write.err 2>/dev/null >&2 || true
    return 1
}

# --- main ---

if [[ ! -f "$PLIST" ]]; then
    echo "Missing $PLIST"
    exit 1
fi

echo "plist: $PLIST ($(stat -f%z "$PLIST") bytes)"

echo "Probing read methods…"
if ! plist_can_read; then
    echo "Could not read known-networks plist (file exists, $(stat -f%z "$PLIST") bytes)."
    [[ -n "$LAST_READ_ERR" ]] && echo "Last error: $LAST_READ_ERR"
    if [[ "$SAW_TCC" -eq 1 ]] || is_tcc_block "$LAST_READ_ERR"; then
        echo ""
        echo "TCC blocked: macOS Full Disk Access is preventing this terminal from opening the file,"
        echo "even under sudo. defaults/plutil/python all hit the same privacy gate."
    fi
    print_fda_help
    exit 1
fi
echo "Read method: ${READ_METHOD}"

if [[ "$LIST_ONLY" -eq 1 ]]; then
    echo "Known SSIDs:"
    known="$(list_known_ssids || true)"
    if [[ -z "$known" ]]; then
        top="$(python3 - "$PLIST" <<'PY' 2>/dev/null || true
import plistlib, sys
with open(sys.argv[1], "rb") as f:
    data = plistlib.load(f)
for i, k in enumerate(data):
    if i >= 30:
        break
    print(k)
print(f"(total keys: {len(data)})", file=sys.stderr)
PY
)"
        if [[ -z "$top" ]]; then
            echo "  (none readable — unexpected after successful open)"
            print_fda_help
        else
            echo "  (no wifi.network.ssid.* keys)"
            echo "Raw top keys (first 30):"
            echo "$top" | sed 's/^/  /'
        fi
    else
        while IFS= read -r s; do
            [[ -z "$s" ]] && continue
            mode="$(read_mode "$s")"
            echo "  - ${s}  [PrivateMAC=${mode:-unset}]"
        done <<< "$known"
    fi
    echo ""
    echo "Preferred networks (networksetup):"
    networksetup -listpreferredwirelessnetworks "$WIFI_IF" 2>/dev/null | sed 's/^/  /' | head -20
    exit 0
fi

KEY="$(ssid_key "$SSID")"

if ! network_exists "$SSID"; then
    echo "SSID '${SSID}' not in known-networks (${KEY})."
    echo ""
    known="$(list_known_ssids || true)"
    if [[ -n "$known" ]]; then
        echo "Known SSIDs:"
        echo "$known" | sed 's/^/  - /'
    else
        echo "Known SSIDs: (none with wifi.network.ssid.* keys)"
    fi
    preferred="$(networksetup -listpreferredwirelessnetworks "$WIFI_IF" 2>/dev/null || true)"
    if echo "$preferred" | grep -Fq "${SSID}"; then
        echo ""
        echo "'${SSID}' appears under Preferred Networks but has no known-networks entry yet."
        echo "Join it once (Wi‑Fi checkmark + IP), then re-run this script."
    else
        echo ""
        echo "Join '${SSID}' once until you get a Wi‑Fi checkmark and an IP, then re-run."
    fi
    exit 1
fi

if pgrep -x "System Settings" >/dev/null 2>&1; then
    echo "Closing System Settings…"
    killall "System Settings" 2>/dev/null || true
    sleep 0.5
fi

before="$(read_mode "$SSID")"
echo "SSID: ${SSID}"
echo "PrivateMACAddressModeUserSetting before: ${before:-unset}"

write_via="$(set_private_mac_off "$SSID")" || {
    echo "Write failed."
    print_fda_help
    echo ""
    echo "Or set Off manually:"
    echo "  System Settings → Wi‑Fi → ${SSID} → Details → Private Wi‑Fi Address → Off"
    exit 1
}
echo "Write method: ${write_via}"

after="$(read_mode "$SSID")"
echo "PrivateMACAddressModeUserSetting after:  ${after:-unset}"

if [[ "$after" != "off" ]]; then
    echo "Write did not stick."
    print_fda_help
    echo ""
    echo "Or set Off in System Settings → Wi‑Fi → ${SSID} → Details → Private Wi‑Fi Address → Off"
    exit 1
fi

killall cfprefsd 2>/dev/null || true
sleep 0.5
killall airportd 2>/dev/null || true
sleep 0.5

if [[ "$BOUNCE_WIFI" -eq 1 ]]; then
    detected="$(networksetup -listallhardwareports 2>/dev/null \
        | awk '/Hardware Port: Wi-Fi/{getline; sub(/^Device: /,""); print; exit}')"
    [[ -n "$detected" ]] && WIFI_IF="$detected"

    echo "Bouncing Wi‑Fi on ${WIFI_IF}…"
    mac_before="$(ifconfig "$WIFI_IF" 2>/dev/null | awk '/ether/{print $2; exit}')"
    networksetup -setairportpower "$WIFI_IF" off
    sleep 1
    networksetup -setairportpower "$WIFI_IF" on
    echo "Waiting for reconnect…"
    sleep 7
    mac_after="$(ifconfig "$WIFI_IF" 2>/dev/null | awk '/ether/{print $2; exit}')"
    echo "MAC before: ${mac_before:-?}"
    echo "MAC after:  ${mac_after:-?}"
fi

echo ""
echo "Done. Rejoin ${SSID} if needed, then:"
echo "  ping -c 3 192.168.43.1"
echo "  ./deploy.sh"
