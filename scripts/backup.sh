#!/bin/bash
# 毎日 pg_dump → S3 バックアップスクリプト
# cron設定例: 0 3 * * * /home/ubuntu/e-learning-management-system-backend/scripts/backup.sh >> /var/log/elms-backup.log 2>&1
set -euo pipefail

BACKEND_DIR="$HOME/e-learning-management-system-backend"
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="/tmp/elms_backup_${DATE}.sql.gz"
S3_BUCKET="${BACKUP_S3_BUCKET:-elms-backups}"  # .env から読む or デフォルト値

# .env を読み込む
if [ -f "$BACKEND_DIR/.env" ]; then
  export $(grep -v '^#' "$BACKEND_DIR/.env" | xargs)
fi

echo "[$(date)] バックアップ開始: $BACKUP_FILE"

# pg_dump を Docker コンテナ内で実行
docker exec elms-db pg_dump -U "${DB_USER}" "${DB_NAME}" | gzip > "$BACKUP_FILE"

# S3 にアップロード
aws s3 cp "$BACKUP_FILE" "s3://${S3_BUCKET}/daily/${DATE}.sql.gz"

# ローカルの一時ファイルを削除
rm -f "$BACKUP_FILE"

# S3 の古いバックアップを削除（30日以上前）
aws s3 ls "s3://${S3_BUCKET}/daily/" \
  | awk '{print $4}' \
  | while read key; do
      file_date=$(echo "$key" | grep -oE '^[0-9]{8}')
      cutoff=$(date -d "30 days ago" +%Y%m%d 2>/dev/null || date -v-30d +%Y%m%d)
      if [[ "$file_date" < "$cutoff" ]]; then
        aws s3 rm "s3://${S3_BUCKET}/daily/${key}"
        echo "[$(date)] 削除: ${key}"
      fi
    done

echo "[$(date)] バックアップ完了"
