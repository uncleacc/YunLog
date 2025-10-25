# 云日记项目配置说明

## 环境配置

### 后端配置

后端使用 Spring Boot Profile 来管理不同环境的配置。

#### 1. 创建本地配置文件

复制 `application-example.yml` 为 `application-local.yml`：

```bash
cd backend/YunLog/src/main/resources/
cp application-example.yml application-local.yml
```

#### 2. 填写真实的配置信息

编辑 `application-local.yml`，填入你的：
- 数据库密码
- 阿里云 OSS AccessKey ID
- 阿里云 OSS AccessKey Secret

```yaml
spring:
  datasource:
    password: your_real_password

aliyun:
  oss:
    access-key-id: YOUR_REAL_ACCESS_KEY_ID
    access-key-secret: YOUR_REAL_ACCESS_KEY_SECRET
```

#### 3. 注意事项

- ⚠️ `application-local.yml` 文件已被 `.gitignore` 忽略，不会提交到 Git
- ⚠️ 永远不要把真实的密钥提交到公开仓库
- ✅ `application.yml` 使用环境变量占位符，是安全的

### 生产环境部署

生产环境建议使用环境变量或配置中心：

```bash
export ALIYUN_OSS_ACCESS_KEY_ID="your_key_id"
export ALIYUN_OSS_ACCESS_KEY_SECRET="your_key_secret"
```

## 运行项目

### 后端

```bash
cd backend/YunLog
./mvnw spring-boot:run
```

应用会自动使用 `application-local.yml` 配置（通过 `spring.profiles.active=local`）。

### 前端

```bash
cd frontend/YunLog
npm install
npm run dev
```

## 安全提醒

1. 定期轮换密钥
2. 使用 RAM 子账号，最小化权限
3. 不要在日志中打印敏感信息
4. 生产环境使用更安全的密钥管理方案
