#!/bin/bash
# elms デプロイスクリプト
# サーバー上で実行: ~/e-learning-management-system-backend/scripts/deploy.sh
set -euo pipefail

BACKEND_DIR="$HOME/e-learning-management-system-backend"
FRONTEND_DIR="$HOME/e-learning-management-system-frontend"

echo "=== [1/3] 最新コードを取得 ==="
cd "$BACKEND_DIR" && git pull origin main
cd "$FRONTEND_DIR" && git pull origin main

echo "=== [2/3] Docker Compose 起動（ビルド含む） ==="
cd "$BACKEND_DIR"
docker compose -f docker-compose-prd.yml up -d --build

echo "=== [3/3] 起動確認 ==="
sleep 10
docker compose -f docker-compose-prd.yml ps

echo ""
echo "デプロイ完了！ http://$(curl -s ifconfig.me) にアクセスして確認してください。"
