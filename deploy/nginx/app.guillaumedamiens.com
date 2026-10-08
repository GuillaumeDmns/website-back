# Flutter web app (installed in /etc/nginx/sites-available by deploy/nginx/apply.sh the first time only, not enabled;
# enabled with HTTPS on 9 October 2026).
# Once the DNS record "app" points to the VPS:
#   ln -s /etc/nginx/sites-available/app.guillaumedamiens.com /etc/nginx/sites-enabled/
#   nginx -t && systemctl reload nginx
#   certbot --nginx -d app.guillaumedamiens.com    (adds HTTPS and the redirection)
# The app calls the API on https://guillaumedamiens.com/api (CORS allows this origin).
server {
    listen 80;
    listen [::]:80;
    server_name app.guillaumedamiens.com;

    root /var/www/guillaumedamiens-app/current;
    index index.html;

    include snippets/guillaumedamiens-security-headers.conf;
    add_header Permissions-Policy "geolocation=(self)" always;
    # Flutter's file names (main.dart.js, canvaskit/...) don't change between builds: always revalidate (ETag)
    add_header Cache-Control "no-cache" always;

    # assetlinks.json (Android App Links): never the app's index.html
    location /.well-known/ {
        try_files $uri =404;
    }

    # go_router URLs (/stops/IDFM:71264, /lines/C01371...) are app pages
    location / {
        try_files $uri $uri/ /index.html;
    }
}
