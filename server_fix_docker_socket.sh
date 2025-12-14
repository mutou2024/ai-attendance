#!/usr/bin/env bash
# Fix Docker socket permissions for snap-installed Docker
# Usage: sudo bash server_fix_docker_socket.sh [deploy_user]

set -euo pipefail

DEPLOY_USER=${1:-aiatt}

if [ "$(id -u)" -ne 0 ]; then
  echo "This script must be run as root (use sudo)."
  exit 1
fi

echo "Starting docker socket fix script for user: $DEPLOY_USER"

# 1) create docker group if missing
if getent group docker >/dev/null 2>&1; then
  echo "group 'docker' already exists"
else
  echo "creating group 'docker'"
  groupadd docker
fi

# 2) ensure deploy user exists
if id "$DEPLOY_USER" >/dev/null 2>&1; then
  echo "user $DEPLOY_USER exists"
else
  echo "user $DEPLOY_USER not found. Creating user..."
  useradd -m -s /bin/bash "$DEPLOY_USER"
fi

echo "Adding $DEPLOY_USER to docker group"
usermod -aG docker "$DEPLOY_USER"

# 3) find docker socket
SOCK="/var/run/docker.sock"
if [ ! -e "$SOCK" ] && [ -e /var/snap/docker/common/docker.sock ]; then
  SOCK="/var/snap/docker/common/docker.sock"
fi

if [ ! -e "$SOCK" ]; then
  echo "ERROR: Docker socket not found at /var/run/docker.sock or /var/snap/docker/common/docker.sock"
  echo "Please ensure dockerd is running. Exiting."
  exit 2
fi

echo "Using socket: $SOCK"

echo "Setting socket owner to root:docker and permissions to 660"
chown root:docker "$SOCK" || true
chmod 660 "$SOCK" || true

echo "Creating systemd unit to keep socket perms after docker restarts"
UNIT_PATH=/etc/systemd/system/docker-socket-perms.service
cat > "$UNIT_PATH" <<'EOF'
[Unit]
Description=Fix docker socket permissions for deploy user group
After=snap.docker.dockerd.service
Requires=snap.docker.dockerd.service

[Service]
Type=oneshot
ExecStart=/bin/chown root:docker /var/run/docker.sock || /bin/true
ExecStart=/bin/chmod 660 /var/run/docker.sock || /bin/true
RemainAfterExit=yes

[Install]
WantedBy=multi-user.target
EOF

echo "Reloading systemd and enabling service"
systemctl daemon-reload
systemctl enable --now docker-socket-perms.service || true

echo "Done. Important next steps (run as non-root):"
echo "1) Re-login as $DEPLOY_USER or run: su - $DEPLOY_USER or newgrp docker"
echo "2) Test: docker ps"
echo
echo "If 'docker ps' returns permission denied in the GitHub Actions remote SSH session,"
echo "you may need to restart the SSH service or ensure the Actions user session picks up the new group (re-login)."

exit 0
