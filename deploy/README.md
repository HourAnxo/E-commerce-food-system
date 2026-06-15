# Deployment (single Ubuntu VPS)

Deploys the E-Commerce Food System to one Linux box: **MySQL + the Spring Boot
JAR (systemd) + Nginx**. Nginx serves the built React frontend and proxies
`/api` to the JAR on `localhost:8081`, so frontend and API share one origin and
no CORS config is needed.

## Files
| File | Purpose | Goes on server at |
|------|---------|-------------------|
| `db-init.sql` | Creates the empty schema + app DB user (Flyway makes the tables) | run once via `sudo mysql` |
| `foodapp.service` | systemd unit running the JAR; holds DB creds as env vars | `/etc/systemd/system/foodapp.service` |
| `nginx-foodapp.conf` | Nginx site: static frontend + `/api` reverse proxy | `/etc/nginx/sites-available/foodapp` |
| `deploy.sh` | Local build + upload + restart helper | run from repo root |

Replace `CHANGE_ME_STRONG_PW` (in `db-init.sql` and `foodapp.service`) and
`your-domain.com` (in `nginx-foodapp.conf`) before using.

## First-time server setup
```bash
sudo apt update
sudo apt install -y openjdk-21-jre-headless mysql-server nginx

# 1. Database (Flyway creates tables on first app start)
sudo mysql < db-init.sql

# 2. Backend service
sudo cp foodapp.service /etc/systemd/system/
sudo systemctl daemon-reload
sudo systemctl enable foodapp           # started after the first deploy uploads the JAR

# 3. Nginx
sudo cp nginx-foodapp.conf /etc/nginx/sites-available/foodapp
sudo ln -s /etc/nginx/sites-available/foodapp /etc/nginx/sites-enabled/
sudo nginx -t && sudo systemctl reload nginx

# 4. HTTPS (optional, recommended)
sudo apt install -y certbot python3-certbot-nginx
sudo certbot --nginx -d your-domain.com
```

## Deploying a build
From the repo root, with JDK 21+ and Node installed locally:
```bash
SERVER=user@your-server-ip ./deploy/deploy.sh
```
This builds the JAR and frontend, uploads them, and restarts the service.
Watch startup (Flyway migrations + boot) with:
```bash
ssh user@your-server-ip 'sudo journalctl -u foodapp -f'
```

## Notes
- **Frontend API URL:** `food-frontend/.env.production` sets `VITE_API_URL=`
  (empty) so the app makes relative `/api` calls that Nginx proxies. If you ever
  split frontend and backend onto different domains, put the backend URL there
  and set `app.cors.allowed-origins` on the backend.
- **DB schema:** the backend runs with `ddl-auto=validate` — it refuses to start
  if tables don't match the entities. All schema changes must be new Flyway
  migrations (`src/main/resources/db/migration/V{n}__*.sql`), never manual edits.
- **No authentication:** every `/api/**` route is open and admin passwords are
  stored in plaintext. Anyone who reaches the server can read/write all data.
  Add a firewall and real auth before exposing this publicly.
