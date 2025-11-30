# Azure 部署指南 - 云日记后端服务

## 部署方式选择

Azure提供多种部署Spring Boot应用的方式，推荐以下两种：

### 方式一：Azure App Service (推荐 - 最简单)
- ✅ 无需管理服务器
- ✅ 自动扩展
- ✅ 内置负载均衡
- ✅ 支持自定义域名和SSL
- ❌ 费用相对较高

### 方式二：Azure VM (虚拟机)
- ✅ 完全控制
- ✅ 费用较低
- ✅ 可安装任何软件
- ❌ 需要手动配置和维护

---

## 方式一：使用 Azure App Service 部署 (推荐)

### 第一步：准备工作

#### 1.1 安装 Azure CLI
```bash
# macOS
brew update && brew install azure-cli

# 验证安装
az --version
```

#### 1.2 登录 Azure
```bash
az login
```

#### 1.3 创建资源组
```bash
# 设置默认位置（选择离你最近的区域）
az configure --defaults location=eastasia

# 创建资源组
az group create --name yunlog-rg --location eastasia
```

### 第二步：创建 MySQL 数据库

#### 2.1 创建 Azure Database for MySQL
```bash
# 创建 MySQL 服务器
az mysql flexible-server create \
  --resource-group yunlog-rg \
  --name yunlog-mysql-server \
  --location eastasia \
  --admin-user adminuser \
  --admin-password 'YourStrongPassword123!' \
  --sku-name Standard_B1ms \
  --tier Burstable \
  --storage-size 32 \
  --version 8.0

# 配置防火墙规则（允许 Azure 服务访问）
az mysql flexible-server firewall-rule create \
  --resource-group yunlog-rg \
  --name yunlog-mysql-server \
  --rule-name AllowAzureServices \
  --start-ip-address 0.0.0.0 \
  --end-ip-address 0.0.0.0

# 允许你的本地IP访问（用于数据导入）
az mysql flexible-server firewall-rule create \
  --resource-group yunlog-rg \
  --name yunlog-mysql-server \
  --rule-name AllowMyIP \
  --start-ip-address YOUR_IP \
  --end-ip-address YOUR_IP
```

#### 2.2 创建数据库
```bash
# 连接到 MySQL 并创建数据库
mysql -h yunlog-mysql-server.mysql.database.azure.com \
  -u adminuser \
  -p'YourStrongPassword123!' \
  -e "CREATE DATABASE yunlog CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
```

#### 2.3 导入本地数据（可选）
```bash
# 导出本地数据
mysqldump -u root -p123456 yunlog > yunlog_backup.sql

# 导入到 Azure MySQL
mysql -h yunlog-mysql-server.mysql.database.azure.com \
  -u adminuser \
  -p'YourStrongPassword123!' \
  yunlog < yunlog_backup.sql
```

### 第三步：创建 Redis 实例

```bash
# 创建 Azure Cache for Redis
az redis create \
  --resource-group yunlog-rg \
  --name yunlog-redis \
  --location eastasia \
  --sku Basic \
  --vm-size c0

# 获取 Redis 连接信息
az redis show \
  --resource-group yunlog-rg \
  --name yunlog-redis \
  --query [hostName,sslPort,accessKeys.primaryKey] \
  --output tsv
```

### 第四步：创建 App Service

#### 4.1 创建 App Service Plan
```bash
az appservice plan create \
  --name yunlog-plan \
  --resource-group yunlog-rg \
  --sku B1 \
  --is-linux
```

#### 4.2 创建 Web App
```bash
az webapp create \
  --resource-group yunlog-rg \
  --plan yunlog-plan \
  --name yunlog-api \
  --runtime "JAVA:11-java11"
```

#### 4.3 配置环境变量
```bash
# 配置数据库连接
az webapp config appsettings set \
  --resource-group yunlog-rg \
  --name yunlog-api \
  --settings \
    SPRING_DATASOURCE_URL="jdbc:mysql://yunlog-mysql-server.mysql.database.azure.com:3306/yunlog?useSSL=true&requireSSL=false&serverTimezone=Asia/Shanghai" \
    SPRING_DATASOURCE_USERNAME="adminuser" \
    SPRING_DATASOURCE_PASSWORD="YourStrongPassword123!" \
    REDIS_HOST="yunlog-redis.redis.cache.windows.net" \
    REDIS_PORT="6380" \
    REDIS_PASSWORD="YOUR_REDIS_KEY" \
    JWT_SECRET="yunlog-production-jwt-secret-key-2024-very-secure-and-long-enough-string" \
    WECHAT_APPID="your-wechat-appid" \
    WECHAT_SECRET="your-wechat-secret" \
    ALIYUN_SMS_ENABLED="false" \
    ALIYUN_OSS_ACCESS_KEY_ID="your-oss-key" \
    ALIYUN_OSS_ACCESS_KEY_SECRET="your-oss-secret"
```

### 第五步：部署应用

#### 5.1 打包应用
```bash
cd /Users/doraemon/Documents/工作/云日记/backend/YunLog

# 使用 Maven 打包
mvn clean package -DskipTests
```

#### 5.2 部署到 Azure
```bash
# 使用 Azure CLI 部署
az webapp deploy \
  --resource-group yunlog-rg \
  --name yunlog-api \
  --src-path target/YunLog-0.0.1-SNAPSHOT.jar \
  --type jar

# 或者使用 Maven 插件部署（推荐）
# 先配置 pom.xml，然后运行：
mvn azure-webapp:deploy
```

#### 5.3 验证部署
```bash
# 查看应用日志
az webapp log tail \
  --resource-group yunlog-rg \
  --name yunlog-api

# 访问应用
curl https://yunlog-api.azurewebsites.net/api/v1/auth/sms/send
```

### 第六步：配置自定义域名和SSL（可选）

```bash
# 添加自定义域名
az webapp config hostname add \
  --resource-group yunlog-rg \
  --webapp-name yunlog-api \
  --hostname api.yourdomain.com

# 绑定 SSL 证书（App Service 提供免费 SSL）
az webapp config ssl bind \
  --resource-group yunlog-rg \
  --name yunlog-api \
  --certificate-thumbprint AUTO \
  --ssl-type SNI
```

---

## 方式二：使用 Azure VM 部署

### 第一步：创建虚拟机

```bash
# 创建 Ubuntu VM
az vm create \
  --resource-group yunlog-rg \
  --name yunlog-vm \
  --image Ubuntu2204 \
  --size Standard_B2s \
  --admin-username azureuser \
  --generate-ssh-keys

# 开放端口
az vm open-port \
  --resource-group yunlog-rg \
  --name yunlog-vm \
  --port 8080
```

### 第二步：连接到虚拟机

```bash
# 获取 VM 的公网 IP
VM_IP=$(az vm show \
  --resource-group yunlog-rg \
  --name yunlog-vm \
  --show-details \
  --query publicIps \
  --output tsv)

# SSH 连接
ssh azureuser@$VM_IP
```

### 第三步：在 VM 上安装环境

```bash
# 更新系统
sudo apt update && sudo apt upgrade -y

# 安装 Java 11
sudo apt install openjdk-11-jdk -y
java -version

# 安装 MySQL
sudo apt install mysql-server -y
sudo mysql_secure_installation

# 安装 Redis
sudo apt install redis-server -y
sudo systemctl enable redis-server
sudo systemctl start redis-server

# 创建应用目录
sudo mkdir -p /opt/yunlog
sudo chown azureuser:azureuser /opt/yunlog
```

### 第四步：配置 MySQL

```bash
# 登录 MySQL
sudo mysql

# 创建数据库和用户
CREATE DATABASE yunlog CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'yunlog'@'localhost' IDENTIFIED BY 'YourPassword123!';
GRANT ALL PRIVILEGES ON yunlog.* TO 'yunlog'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

### 第五步：上传并运行应用

```bash
# 从本地上传 JAR 文件
scp target/YunLog-0.0.1-SNAPSHOT.jar azureuser@$VM_IP:/opt/yunlog/

# SSH 到 VM
ssh azureuser@$VM_IP

# 创建配置文件
cd /opt/yunlog
cat > application-prod.yml << 'EOF'
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/yunlog?useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=Asia/Shanghai
    username: yunlog
    password: YourPassword123!
  redis:
    host: localhost
    port: 6379
server:
  port: 8080
EOF

# 创建 systemd 服务
sudo tee /etc/systemd/system/yunlog.service > /dev/null << 'EOF'
[Unit]
Description=YunLog Spring Boot Application
After=syslog.target network.target

[Service]
User=azureuser
ExecStart=/usr/bin/java -jar /opt/yunlog/YunLog-0.0.1-SNAPSHOT.jar --spring.config.location=/opt/yunlog/application-prod.yml --spring.profiles.active=prod
SuccessExitStatus=143
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
EOF

# 启动服务
sudo systemctl daemon-reload
sudo systemctl enable yunlog
sudo systemctl start yunlog

# 查看状态
sudo systemctl status yunlog
```

---

## 生产环境配置建议

### 1. 安全配置

```yaml
# application-prod.yml
spring:
  jpa:
    show-sql: false  # 生产环境关闭 SQL 日志
    
logging:
  level:
    root: WARN
    io.github.uncleacc.yunlog: INFO
```

### 2. 性能优化

```yaml
server:
  tomcat:
    max-threads: 200
    min-spare-threads: 10
    
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
```

### 3. 健康检查

访问：`https://yunlog-api.azurewebsites.net/actuator/health`

### 4. 监控配置

```bash
# 启用 Application Insights
az monitor app-insights component create \
  --app yunlog-insights \
  --location eastasia \
  --resource-group yunlog-rg

# 配置应用
az webapp config appsettings set \
  --resource-group yunlog-rg \
  --name yunlog-api \
  --settings APPLICATIONINSIGHTS_CONNECTION_STRING="YOUR_CONNECTION_STRING"
```

---

## 前端配置更新

部署完成后，需要更新前端的API地址：

```javascript
// frontend/YunLog/src/utils/request.js
const getBaseURL = () => {
  // #ifdef H5
  return 'https://yunlog-api.azurewebsites.net'  // 改为 Azure 域名
  // #endif
  
  // #ifdef MP-WEIXIN
  return 'https://yunlog-api.azurewebsites.net'  // 改为 Azure 域名
  // #endif
}
```

---

## 成本估算（方式一 - App Service）

- **App Service (B1)**: ~$13/月
- **MySQL (Burstable B1ms)**: ~$12/月
- **Redis (Basic C0)**: ~$16/月
- **带宽**: 按使用量计费

**总计**: 约 $41/月（约 ¥300/月）

---

## 成本估算（方式二 - VM）

- **VM (B2s)**: ~$30/月
- **磁盘**: ~$5/月
- **带宽**: 按使用量计费

**总计**: 约 $35/月（约 ¥250/月）

---

## 故障排查

### 查看日志
```bash
# App Service
az webapp log tail --resource-group yunlog-rg --name yunlog-api

# VM
ssh azureuser@$VM_IP
sudo journalctl -u yunlog -f
```

### 常见问题

1. **数据库连接失败**
   - 检查防火墙规则
   - 验证连接字符串
   - 确认用户名密码

2. **应用启动失败**
   - 检查 Java 版本
   - 查看应用日志
   - 验证环境变量

3. **Redis 连接超时**
   - 检查 Redis 是否启动
   - 验证端口和密码
   - 检查网络规则

---

## 下一步

1. 设置 CI/CD 自动部署（GitHub Actions）
2. 配置备份策略
3. 设置监控告警
4. 优化性能和成本
