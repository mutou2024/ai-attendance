#!/usr/bin/env bash
# Helper script to push the current workspace to the GitHub repository you provided.
# Usage: chmod +x push_to_github.sh && ./push_to_github.sh

set -e
REPO=git@github.com:mutou2024/ai-attendance.git
BRANCH=main

if [ -d .git ]; then
  echo ".git already exists. Will add remote if not present."
else
  git init
fi

git add -A
git commit -m "Initial scaffold: backend + frontend + deploy workflow" || true

if git remote get-url origin >/dev/null 2>&1; then
  echo "remote origin already set"
else
  git remote add origin "$REPO"
fi

git branch -M $BRANCH

echo "Pushing to $REPO (branch $BRANCH) ..."
git push -u origin $BRANCH

echo "Done. Now go to GitHub -> Settings -> Secrets and add DEPLOY_HOST, DEPLOY_USER, SSH_PRIVATE_KEY, DEPLOY_PATH (and DEPLOY_SSH_PORT if not 22)."
