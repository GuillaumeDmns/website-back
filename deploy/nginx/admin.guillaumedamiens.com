# Administration (website-admin), installed in /etc/nginx/sites-available by deploy/nginx/apply.sh the first time only,
# not enabled. Once the DNS record "admin" points to the VPS:
#   install -d -o deploy -g deploy /var/www/guillaumedamiens-admin/releases
#   ln -s /etc/nginx/sites-available/admin.guillaumedamiens.com /etc/nginx/sites-enabled/
#   nginx -t && systemctl reload nginx
#   certbot --nginx -d admin.guillaumedamiens.com    (adds HTTPS and the redirection)
# The pages call the API on https://guillaumedamiens.com/api (CORS allows this origin), admins only: the site itself
# holds no data. Google's sign-in script and frame are the only other origins.
server {
    listen 80;
    listen [::]:80;
    server_name admin.guillaumedamiens.com;

    root /var/www/guillaumedamiens-admin/current;
    index index.html;

    # Each location sets its own headers (an add_header there drops the server's)

    # Vite's file names carry their hash
    location /assets/ {
        add_header Cache-Control "public, max-age=31536000, immutable" always;
        include snippets/guillaumedamiens-security-headers.conf;
        try_files $uri =404;
    }

    # React Router pages (/usage, /network...); CSP: the page and Google's sign-in only
    location / {
        add_header Cache-Control "no-cache" always;
        include snippets/guillaumedamiens-security-headers.conf;
        add_header Content-Security-Policy "default-src 'self'; script-src 'self' https://accounts.google.com/gsi/client; style-src 'self' https://accounts.google.com/gsi/style; frame-src https://accounts.google.com/gsi/; connect-src 'self' https://guillaumedamiens.com https://accounts.google.com/gsi/; img-src 'self' data:; base-uri 'none'; form-action 'none'; frame-ancestors 'none'" always;
        add_header X-Robots-Tag "noindex, nofollow" always;
        try_files $uri $uri/ /index.html;
    }
}
