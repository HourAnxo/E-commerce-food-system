#!/usr/bin/env bash
# Build the backend JAR and frontend bundle, then upload both to the server.
# Run from the repo root:  SERVER=user@1.2.3.4 ./deploy/deploy.sh
#
# Requires JDK 21+ on JAVA_HOME and Node installed locally. Re-run any time to
# ship a new build; the systemd service is restarted to pick up the new JAR.
set -euo pipefail

: "${SERVER:?Set SERVER, e.g. SERVER=user@1.2.3.4 ./deploy/deploy.sh}"

echo "==> Building backend JAR"
./mvnw.cmd clean package -DskipTests

echo "==> Building frontend bundle"
( cd food-frontend && npm ci && npm run build )

JAR=$(ls target/*-SNAPSHOT.jar | head -1)

echo "==> Uploading JAR to $SERVER:/opt/foodapp/app.jar"
ssh "$SERVER" "sudo mkdir -p /opt/foodapp /var/www/foodapp"
scp "$JAR" "$SERVER:/tmp/app.jar"
ssh "$SERVER" "sudo mv /tmp/app.jar /opt/foodapp/app.jar"

echo "==> Uploading frontend to $SERVER:/var/www/foodapp"
scp -r food-frontend/dist/* "$SERVER:/tmp/foodapp-dist/" 2>/dev/null || \
  ( ssh "$SERVER" "mkdir -p /tmp/foodapp-dist" && scp -r food-frontend/dist/* "$SERVER:/tmp/foodapp-dist/" )
ssh "$SERVER" "sudo rm -rf /var/www/foodapp/* && sudo cp -r /tmp/foodapp-dist/* /var/www/foodapp/ && rm -rf /tmp/foodapp-dist"

echo "==> Restarting backend service"
ssh "$SERVER" "sudo systemctl restart foodapp && sudo systemctl reload nginx"

echo "==> Done. Tail logs with: ssh $SERVER 'sudo journalctl -u foodapp -f'"
