#!/usr/bin/env bash
# Applies the nginx configuration (run as root): http-level settings (conf.d), API and security header snippets, and
# the Flutter web app site (sites-available only). In the guillaumedamiens.com site, the former "location /api" block
# is replaced by the API snippet. Everything is put back when nginx -t fails.
#
# Usage: bash apply.sh [site file, /etc/nginx/sites-enabled/default by default]
set -euo pipefail

SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)
SITE=$(readlink -f "${1:-/etc/nginx/sites-enabled/default}")
# Outside sites-enabled and conf.d: nginx would load the copies
BACKUP_DIR=/root/nginx-backup-$(date +%Y%m%d-%H%M%S)

[[ $EUID -eq 0 ]] || { echo "Run as root" >&2; exit 1; }
grep -q "server_name guillaumedamiens.com" "$SITE" || { echo "$SITE does not hold the guillaumedamiens.com site" >&2; exit 1; }

declare -A targets=(
    [guillaumedamiens.conf]=/etc/nginx/conf.d/guillaumedamiens.conf
    [guillaumedamiens-api.conf]=/etc/nginx/snippets/guillaumedamiens-api.conf
    [guillaumedamiens-security-headers.conf]=/etc/nginx/snippets/guillaumedamiens-security-headers.conf
    [app.guillaumedamiens.com]=/etc/nginx/sites-available/app.guillaumedamiens.com
)

mkdir -p "$BACKUP_DIR"
cp "$SITE" "$BACKUP_DIR/site"
for name in "${!targets[@]}"; do
    [[ -f ${targets[$name]} ]] && cp "${targets[$name]}" "$BACKUP_DIR/$name"
done

rollback() {
    echo "Putting the previous configuration back (copies in $BACKUP_DIR)" >&2
    cp "$BACKUP_DIR/site" "$SITE"
    for name in "${!targets[@]}"; do
        if [[ -f $BACKUP_DIR/$name ]]; then
            cp "$BACKUP_DIR/$name" "${targets[$name]}"
        else
            rm -f "${targets[$name]}"
        fi
    done
    exit 1
}

install -d /etc/nginx/snippets
for name in "${!targets[@]}"; do
    # The app site is only installed the first time: certbot then adds its HTTPS block to it, which must stay
    if [[ $name == app.guillaumedamiens.com && -f ${targets[$name]} ]]; then
        echo "${targets[$name]} already exists (certbot's HTTPS block), kept"
        continue
    fi
    install -m 644 "$SCRIPT_DIR/$name" "${targets[$name]}"
done

if ! grep -q "snippets/guillaumedamiens-api.conf" "$SITE"; then
    # The former block holds no nested braces
    perl -0pi -e 's/^([ \t]*)location \/api \{[^}]*\}/$1include snippets\/guillaumedamiens-api.conf;\n$1include snippets\/guillaumedamiens-security-headers.conf;/m' "$SITE"
    grep -q "snippets/guillaumedamiens-api.conf" "$SITE" || { echo "No 'location /api' block found in $SITE" >&2; rollback; }
fi

nginx -t || rollback
systemctl reload nginx
echo "nginx reloaded (previous files in $BACKUP_DIR)"
echo "The app site is in /etc/nginx/sites-available/app.guillaumedamiens.com: see its first lines to enable it."
