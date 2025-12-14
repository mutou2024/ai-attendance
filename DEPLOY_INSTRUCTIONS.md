部署说明（GitHub Actions + SSH 自动部署 - 方案 B）

目标：当你把代码 push 到 `main` 分支时，GitHub Actions 会把仓库拷贝到你的服务器指定路径并在服务器上执行 `docker compose up -d --build`。

前提（你已完成）：
- 服务器为 Ubuntu，已安装 Docker 与 docker-compose（你已确认）。
- 你拥有对 GitHub 仓库 `git@github.com:mutou2024/ai-attendance.git` 的写权限（或你会把远程设置和推送命令在本地运行）。

需要在 GitHub 仓库中创建的 Secrets：
- `DEPLOY_HOST`：服务器 IP 或域名（例如 1.2.3.4）
- `DEPLOY_USER`：用于部署的 SSH 用户名（例如 aiatt）
- `SSH_PRIVATE_KEY`：该用户对应的私钥（私钥文本，BEGIN/END 包含在内）
- `DEPLOY_PATH`：在服务器上放置项目的目录（例如 /opt/ai-attendance）
- `DEPLOY_SSH_PORT`：可选，默认 22，如果你使用默认端口可留空或设置为 22

在仓库添加 Secrets 的步骤（GitHub UI）：
1. 打开仓库页面 -> Settings -> Secrets and variables -> Actions -> New repository secret
2. 添加上面列出的 secrets（名称和值）

在服务器上建议的准备操作（在服务器上以 root 或 sudo 用户执行）：
```bash
# 创建 deploy 用户（如果尚未创建）
sudo useradd -m -s /bin/bash aiatt || true
sudo usermod -aG docker aiatt || true

# 创建目录并赋权
sudo mkdir -p /opt/ai-attendance
sudo chown aiatt:aiatt /opt/ai-attendance

# 可选：禁用密码登录并确保已部署公钥到 /home/aiatt/.ssh/authorized_keys
# 将部署用公钥（对应于你将写到 GitHub Secrets 的私钥）写入 authorized_keys

# 确认 docker 与 docker compose 可用
docker --version
docker compose version
```

手动把公钥写入服务器（如果你偏好让我们只提供 workflow 并且你自己手动配置）：
- 在你的本地（或由我们生成），创建一对 SSH 密钥：
  `ssh-keygen -t ed25519 -C "deploy@ai-attendance" -f deploy_key`，不要设置 passphrase
- 本地会生成 `deploy_key`（私钥）和 `deploy_key.pub`（公钥）
- 把 `deploy_key` 的内容复制到仓库 Secrets `SSH_PRIVATE_KEY`（整个文件内容）
- 把 `deploy_key.pub` 的内容追加到服务器 `/home/aiatt/.ssh/authorized_keys`

把代码推到 GitHub（本地操作）
1. 在本地仓库根目录执行：
```bash
git init
git add .
git commit -m "Initial scaffold for ai-attendance"
git remote add origin git@github.com:mutou2024/ai-attendance.git
git branch -M main
git push -u origin main
```

注意与故障排查：
- 如果 Actions 报错 `Permission denied (publickey)`，说明 `SSH_PRIVATE_KEY` 与服务器上的公钥不匹配。
- 如果 Actions 报错 `docker: command not found`，请确认服务器上已安装 docker 且 `deploy` 用户有权限运行 docker（或使用 sudo）。
- 如果需要 sudo 运行 docker 命令，可在 workflow 的 ssh script 中用 `sudo docker compose up -d --build`，并确保 `deploy` 用户在 sudoers 中无需密码（需谨慎）。

后续提升（可选）
- 使用 registry（GHCR / Docker Hub）作为镜像中转，更稳定、节省 upload 时间。
- 在服务器上用 nginx 反向代理并配置 TLS（Let's Encrypt）。
- 定期备份 SQLite 数据文件（例如 `./backend/data/app.db`）到远程存储或对象存储。


如果你愿意，我可以：
- 把当前代码提交到你指定的仓库（需要你在本地运行我们给的 `push_to_github.sh` 或把仓库权限给我），
- 或者我可以把 workflow 提交到工作区（已添加），并引导你如何在 GitHub 上配置 Secrets 并触发部署。

下一步，请选择：
- 我来生成并给出你需要在本地运行的 `git push` 命令（推荐）— 我会把脚本写入仓库；
- 或者请将仓库权限/SSH 访问信息给我（不推荐通过聊天发送私钥），我可以替你完成 push 与触发部署。
