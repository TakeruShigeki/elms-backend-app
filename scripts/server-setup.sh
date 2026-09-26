#!/bin/bash
# Lightsail サーバー初回セットアップスクリプト（Ubuntu 22.04）
# 使い方: ssh ubuntu@サーバーIP "bash -s" < server-setup.sh
set -euo pipefail

echo "=== [1/5] システム更新 ==="
sudo apt-get update -y
sudo apt-get upgrade -y

echo "=== [2/5] Docker インストール ==="
sudo apt-get install -y ca-certificates curl gnupg lsb-release
sudo mkdir -p /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" \
  | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
sudo apt-get update -y
sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin

# ubuntu ユーザーを docker グループに追加（sudo なしで docker を使えるように）
sudo usermod -aG docker ubuntu
newgrp docker || true

echo "=== [3/5] その他ツールインストール ==="
sudo apt-get install -y git awscli jq

echo "=== [4/5] アプリディレクトリ準備 ==="
cd ~
git clone -b main https://github.com/stra24/e-learning-management-system-backend.git || (cd e-learning-management-system-backend && git pull origin main)
git clone -b main https://github.com/stra24/e-learning-management-system-frontend.git || (cd e-learning-management-system-frontend && git pull origin main)

echo "=== [5/5] .env ファイルを作成してください ==="
echo ""
echo "  cp ~/e-learning-management-system-backend/.env.prd.example ~/e-learning-management-system-backend/.env"
echo "  nano ~/e-learning-management-system-backend/.env"
echo ""
echo "セットアップ完了！.env を編集してから deploy.sh を実行してください。"
echo "手順の全体像: Desktop/EverRefine/elms-deploy/ON_OFF_PLAYBOOK.md"
