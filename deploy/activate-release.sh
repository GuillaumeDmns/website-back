#!/usr/bin/env bash
# Activates a jar of /opt/guillaumedamiens/releases: switches current.jar, restarts the service, waits for its health
# check and goes back to the previous release when it does not answer. With a URL, the jar is downloaded first (from
# the GitHub release, see .github/workflows/deploy.yml) and checked against its SHA-256. Run as the deploy user:
#   ssh deploy@host bash -s -- <jar name> [<url> <sha256>] < deploy/activate-release.sh
set -euo pipefail

SERVICE=guillaumedamiens
APP_DIR=/opt/guillaumedamiens
HEALTH_URL=http://127.0.0.1:8080/api/health
# Releases kept on disk, the active one included
KEEP=5

release="$APP_DIR/releases/${1:?usage: activate-release.sh <jar name in $APP_DIR/releases> [<url> <sha256>]}"
url=${2:-}
sha256=${3:-}

if [[ -n $url ]]; then
    echo "Downloading $url"
    curl -fsSL --retry 3 --max-time 300 -o "$release.part" "$url"
    if [[ -n $sha256 ]] && ! echo "$sha256  $release.part" | sha256sum -c --quiet; then
        rm -f "$release.part"
        echo "Checksum mismatch for $url" >&2
        exit 1
    fi
    chmod 644 "$release.part"
    mv "$release.part" "$release"
fi
[[ -f $release ]] || { echo "No $release" >&2; exit 1; }
previous=$(readlink -f "$APP_DIR/current.jar" || true)

restart() {
    ln -sfn "$1" "$APP_DIR/current.jar"
    sudo -n /usr/bin/systemctl restart "$SERVICE"
}

healthy() {
    for _ in $(seq 1 60); do
        sleep 3
        [[ $(curl -s -o /dev/null -w '%{http_code}' "$HEALTH_URL") == 200 ]] && return 0
    done
    return 1
}

restart "$release"
if healthy; then
    echo "$(basename "$release") is live"
    ls -1t "$APP_DIR"/releases/*.jar | tail -n +$((KEEP + 1)) | xargs -r rm --
    exit 0
fi

echo "$(basename "$release") does not answer" >&2
if [[ -n $previous && -f $previous && $previous != "$release" ]]; then
    echo "Back to $(basename "$previous")" >&2
    restart "$previous"
    healthy || echo "The previous release does not answer either" >&2
fi
exit 1
