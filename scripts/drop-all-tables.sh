#!/bin/bash
# elmsDB 全テーブル削除スクリプト
# 用途: データ破損時のリセット。実行後にアプリを再起動するとFlywayが自動でスキーマを再作成する。
# 実行場所: Lightsailサーバー上
set -euo pipefail

BACKEND_DIR="$HOME/e-learning-management-system-backend"

# .env を読み込む
if [ -f "$BACKEND_DIR/.env" ]; then
  export $(grep -v '^#' "$BACKEND_DIR/.env" | xargs)
fi

DB_USER="${DB_USER:-root}"
DB_NAME="${DB_NAME:-elms_db}"

echo "======================================"
echo "  elmsDB 全テーブル削除スクリプト"
echo "======================================"
echo ""
echo "対象DB   : $DB_NAME"
echo "対象ユーザー: $DB_USER"
echo ""
echo "⚠️  警告: この操作は取り消せません。全データが失われます。"
echo ""
read -p "本当に実行しますか？ (yes と入力して確認): " confirm
if [ "$confirm" != "yes" ]; then
  echo "キャンセルしました。"
  exit 1
fi

echo ""
echo "=== [1/3] アプリコンテナを停止（DBは残す） ==="
cd "$BACKEND_DIR"
docker compose -f docker-compose-prd.yml stop elms-app elms-frontend nginx
echo "停止完了"

echo ""
echo "=== [2/3] 全テーブルを削除 ==="
docker exec elms-db psql -U "$DB_USER" -d "$DB_NAME" -v ON_ERROR_STOP=1 \
  -c "DROP SCHEMA public CASCADE;" \
  -c "CREATE SCHEMA public;" \
  -c "GRANT ALL ON SCHEMA public TO PUBLIC;"
echo "削除完了"

echo ""
echo "=== [3/3] アプリコンテナを再起動（Flywayがスキーマを自動再作成） ==="
docker compose -f docker-compose-prd.yml up -d
echo "再起動コマンド発行完了"

echo ""
echo "=== 起動待ち（30秒） ==="
sleep 30
docker compose -f docker-compose-prd.yml ps

echo ""
echo "✅ 完了！Flywayによるスキーマ再作成が完了していれば使用可能な状態です。"
echo "   確認: curl http://localhost/api/hello"
