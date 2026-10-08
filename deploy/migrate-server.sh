#!/usr/bin/env bash
# One-time migration of the backend on the VPS (run as root):
# - the service runs as the system user "guillaumedamiens" instead of root, from /opt/guillaumedamiens/current.jar;
# - its secrets move to /etc/guillaumedamiens/application-prod.yml, copied from the application-dev.yml embedded in
#   the jar running today (/root/target);
# - the "deploy" user (GitHub Actions) can upload releases and restart the service, nothing else.
# If the new service does not answer, the previous unit is put back.
#
# Usage: bash migrate-server.sh <new jar> <deploy public key file>
set -euo pipefail

SERVICE=guillaumedamiens
APP_USER=guillaumedamiens
DEPLOY_USER=deploy
APP_DIR=/opt/guillaumedamiens
CONF_DIR=/etc/guillaumedamiens
WEB_DIR=/var/www/guillaumedamiens-app
OLD_JAR=/root/target/website-0.0.1-SNAPSHOT.jar
UNIT=/etc/systemd/system/$SERVICE.service
HEALTH_URL=http://127.0.0.1:8080/api/health
SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)

NEW_JAR=${1:?usage: migrate-server.sh <new jar> <deploy public key file>}
DEPLOY_KEY=${2:?usage: migrate-server.sh <new jar> <deploy public key file>}

fail() {
    echo "ERROR: $*" >&2
    exit 1
}

[[ $EUID -eq 0 ]] || fail "run as root"
[[ -f $NEW_JAR ]] || fail "no jar $NEW_JAR"
[[ -f $DEPLOY_KEY ]] || fail "no public key $DEPLOY_KEY"
[[ -f $SCRIPT_DIR/$SERVICE.service ]] || fail "no $SCRIPT_DIR/$SERVICE.service next to this script"
for command in java curl sudo visudo; do
    command -v "$command" >/dev/null || fail "$command is missing (apt install $command)"
done

echo "== Users"
id -u "$APP_USER" >/dev/null 2>&1 || useradd --system --no-create-home --home-dir "$APP_DIR" --shell /usr/sbin/nologin "$APP_USER"
id -u "$DEPLOY_USER" >/dev/null 2>&1 || useradd --create-home --shell /bin/bash "$DEPLOY_USER"
install -d -m 700 -o "$DEPLOY_USER" -g "$DEPLOY_USER" "/home/$DEPLOY_USER/.ssh"
# "restrict": no port forwarding, agent or terminal for this key
echo "restrict $(cat "$DEPLOY_KEY")" > "/home/$DEPLOY_USER/.ssh/authorized_keys"
chown "$DEPLOY_USER:$DEPLOY_USER" "/home/$DEPLOY_USER/.ssh/authorized_keys"
chmod 600 "/home/$DEPLOY_USER/.ssh/authorized_keys"

echo "== Directories"
# Releases are written by deploy and read by the service; the web app is read by nginx
install -d -m 755 -o "$DEPLOY_USER" -g "$APP_USER" "$APP_DIR" "$APP_DIR/releases"
install -d -m 750 -o root -g "$APP_USER" "$CONF_DIR"
install -d -m 755 -o "$DEPLOY_USER" -g www-data "$WEB_DIR" "$WEB_DIR/releases"

echo "== Configuration"
if [[ -f $CONF_DIR/application-prod.yml ]]; then
    echo "$CONF_DIR/application-prod.yml already exists, kept"
else
    [[ -f $OLD_JAR ]] || fail "no $OLD_JAR to take the configuration from"
    if command -v unzip >/dev/null; then
        unzip -p "$OLD_JAR" BOOT-INF/classes/application-dev.yml > "$CONF_DIR/application-prod.yml"
    elif command -v python3 >/dev/null; then
        python3 -c 'import sys, zipfile; sys.stdout.buffer.write(zipfile.ZipFile(sys.argv[1]).read("BOOT-INF/classes/application-dev.yml"))' "$OLD_JAR" > "$CONF_DIR/application-prod.yml"
    else
        fail "unzip or python3 is needed to read the configuration of $OLD_JAR (apt install unzip)"
    fi
    [[ -s $CONF_DIR/application-prod.yml ]] || fail "no application-dev.yml in $OLD_JAR"
fi
chown root:"$APP_USER" "$CONF_DIR/application-prod.yml"
chmod 640 "$CONF_DIR/application-prod.yml"
echo "Keys of $CONF_DIR/application-prod.yml (values hidden), check nothing dev-only is left:"
sed -E 's/^([[:space:]]*-?[[:space:]]*[^:#]+:)[[:space:]]*[^[:space:]].*$/\1 ***/' "$CONF_DIR/application-prod.yml"

echo "== Release"
release="$APP_DIR/releases/website-$(date +%Y%m%d-%H%M%S).jar"
install -m 644 -o "$DEPLOY_USER" -g "$APP_USER" "$NEW_JAR" "$release"
ln -sfn "$release" "$APP_DIR/current.jar"
chown -h "$DEPLOY_USER:$APP_USER" "$APP_DIR/current.jar"

echo "== Sudo rights of $DEPLOY_USER"
sudoers=/etc/sudoers.d/$SERVICE-deploy
echo "$DEPLOY_USER ALL=(root) NOPASSWD: /usr/bin/systemctl restart $SERVICE, /usr/bin/systemctl is-active $SERVICE" > "$sudoers.tmp"
chmod 440 "$sudoers.tmp"
visudo -cf "$sudoers.tmp" >/dev/null || fail "invalid sudoers file"
mv "$sudoers.tmp" "$sudoers"

echo "== Service"
backup=""
if [[ -f $UNIT ]]; then
    backup="$UNIT.bak-$(date +%Y%m%d-%H%M%S)"
    cp "$UNIT" "$backup"
fi
install -m 644 "$SCRIPT_DIR/$SERVICE.service" "$UNIT"
systemctl daemon-reload
systemctl enable "$SERVICE" >/dev/null 2>&1
systemctl restart "$SERVICE"

echo "Waiting for $HEALTH_URL (Liquibase and Spring take a while)..."
for _ in $(seq 1 60); do
    sleep 3
    if [[ $(curl -s -o /dev/null -w '%{http_code}' "$HEALTH_URL") == 200 ]]; then
        echo
        echo "The service runs as $APP_USER from $release."
        echo "Next: check 'journalctl -u $SERVICE -f' and the app, then delete /root/target (old jars with the"
        echo "secrets, logs since 2025) and $backup."
        exit 0
    fi
done

echo "The new service does not answer: last logs" >&2
journalctl -u "$SERVICE" -n 50 --no-pager >&2 || true
if [[ -n $backup ]]; then
    echo "Putting the previous unit back" >&2
    cp "$backup" "$UNIT"
    systemctl daemon-reload
    systemctl restart "$SERVICE"
fi
exit 1
