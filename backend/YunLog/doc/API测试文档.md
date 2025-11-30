# 云日记后端 API 测试文档

## 测试环境

- **Base URL**: `http://localhost:8080/api/v1`
- **OSS Bucket**: `yunlog-diary`
- **OSS Region**: `oss-cn-hangzhou`
- **认证方式**: JWT Token (Bearer Authentication)

---

## 0. 用户认证 API 🔐

### 0.1 用户注册
```http
POST /auth/register
Content-Type: application/json

{
  "username": "testuser",
  "password": "123456",
  "phone": "13800138000"
}
```

**请求参数**:
- `username` (必填): 用户名，3-20个字符，仅支持字母、数字和下划线
- `password` (必填): 密码，6-20个字符
- `phone` (可选): 手机号

**响应示例**:
```json
{
  "code": 200,
  "message": "注册成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwidXNlcm5hbWUiOiJ0ZXN0dXNlciIsImlhdCI6MTY5ODEyMzQ1NiwiZXhwIjoxNjk4NzI4MjU2fQ.xyz123",
    "userInfo": {
      "id": 1,
      "username": "testuser",
      "phone": "13800138000",
      "wechatOpenid": null
    }
  }
}
```

**说明**:
- 注册成功后自动登录，返回token
- 系统会自动为新用户创建默认分类
- token有效期为7天

### 0.2 账号密码登录
```http
POST /auth/login
Content-Type: application/json

{
  "username": "testuser",
  "password": "123456"
}
```

**请求参数**:
- `username` (必填): 用户名
- `password` (必填): 密码

**响应示例**:
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwidXNlcm5hbWUiOiJ0ZXN0dXNlciIsImlhdCI6MTY5ODEyMzQ1NiwiZXhwIjoxNjk4NzI4MjU2fQ.xyz123",
    "userInfo": {
      "id": 1,
      "username": "testuser",
      "phone": "13800138000",
      "wechatOpenid": null
    }
  }
}
```

### 0.3 微信登录
```http
POST /auth/wechat/login
Content-Type: application/json

{
  "code": "081xYZ2w3ABCDE2wx..."
}
```

**请求参数**:
- `code` (必填): 微信小程序登录code，通过`wx.login()`获取

**响应示例**:
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "userInfo": {
      "id": 2,
      "username": "wx_abc12345",
      "phone": null,
      "wechatOpenid": "oABC123xyz..."
    }
  }
}
```

**说明**:
- 首次微信登录会自动创建用户账号
- 自动生成用户名格式: `wx_{openid前8位}`
- 同一个微信openid只会创建一次用户

### 0.4 登出
```http
POST /auth/logout
Authorization: Bearer {token}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "登出成功"
}
```

**说明**:
- 实际上是客户端删除token即可
- 服务端使用无状态JWT，不需要特殊处理

### 0.5 使用Token访问API
所有需要认证的API（除了注册和登录）都需要在请求头中携带token：

```http
GET /diaries
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**请求头格式**:
```
Authorization: Bearer {your_token_here}
```

**认证失败响应** (401):
```json
{
  "error": "未授权，请先登录"
}
```

### 0.6 测试账号
系统预置了一个测试账号（数据库迁移时创建）：
- **用户名**: `test`
- **密码**: `123456`

---

## 1. 分类管理 API

### 1.1 获取分类列表
```http
GET /categories
```

**响应示例**:
```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "name": "默认分类",
      "icon": "📝",
      "color": "#FF9A76",
      "isDefault": true,
      "createTime": "2025-10-14 10:00:00",
      "updateTime": null
    }
  ]
}
```

### 1.2 创建分类
```http
POST /categories
Content-Type: application/json

{
  "name": "工作",
  "icon": "💼",
  "color": "#4CAF50"
}
```

### 1.3 更新分类
```http
PUT /categories/{id}
Content-Type: application/json

{
  "name": "生活",
  "icon": "🏠",
  "color": "#2196F3"
}
```

### 1.4 删除分类
```http
DELETE /categories/{id}
```
**注意**: 默认分类不可删除,该分类下的日记会移至默认分类

---

## 2. 日记管理 API

### 2.1 获取日记列表
```http
GET /diaries?page=1&limit=20&categoryId=1
```

### 2.2 获取日记详情
```http
GET /diaries/{id}
```

### 2.3 创建日记
```http
POST /diaries
Content-Type: application/json

{
  "title": "今天的心情",
  "content": "今天天气不错",
  "contentHtml": "<p>今天天气不错</p>",
  "categoryId": 1
}
```

### 2.4 更新日记
```http
PUT /diaries/{id}
Content-Type: application/json

{
  "title": "修改后的标题",
  "content": "修改后的内容",
  "contentHtml": "<p>修改后的内容</p>",
  "categoryId": 2
}
```

### 2.5 删除日记(移至回收站)
```http
DELETE /diaries/{id}
```

### 2.6 批量删除日记
```http
DELETE /diaries/batch
Content-Type: application/json

{
  "ids": [1, 2, 3]
}
```

### 2.7 搜索日记
```http
GET /diaries?keyword=天气&page=1&limit=20
```

---

## 3. 回收站管理 API

### 3.1 获取回收站列表
```http
GET /trash?page=1&limit=20
```

### 3.2 恢复日记
```http
POST /trash/{id}/restore
```

### 3.3 永久删除日记
```http
DELETE /trash/{id}
```
**注意**: 永久删除会同时删除日记的所有附件(包括 OSS 文件)

### 3.4 清空回收站
```http
DELETE /trash/clear
```
**注意**: 会删除所有回收站日记及其附件文件

### 3.5 批量恢复
```http
POST /trash/batch-restore
Content-Type: application/json

{
  "ids": [1, 2, 3]
}
```

---

## 4. 文件上传 API

### 4.1 上传单张图片
```http
POST /upload/image
Content-Type: multipart/form-data

file: (图片文件)
diaryId: 1  (可选,提供则自动创建附件记录)
```

**响应示例**:
```json
{
  "code": 200,
  "message": "图片上传成功",
  "data": {
    "url": "https://yunlog-diary.oss-cn-hangzhou.aliyuncs.com/diary/images/20251014/abc123.jpg",
    "attachmentId": 1,
    "diaryId": 1
  }
}
```

### 4.2 批量上传图片
```http
POST /upload/images
Content-Type: multipart/form-data

files: (多个图片文件)
diaryId: 1  (可选)
```

**响应示例**:
```json
{
  "code": 200,
  "message": "批量上传成功",
  "data": {
    "urls": ["url1", "url2"],
    "count": 2,
    "attachmentIds": [1, 2],
    "diaryId": 1
  }
}
```

### 4.3 临时上传图片
```http
POST /upload/temp-image
Content-Type: multipart/form-data

file: (图片文件)
```
**说明**: 仅上传到 OSS,不创建附件记录,用于编辑器临时图片

**响应示例**:
```json
{
  "code": 200,
  "message": "临时图片上传成功",
  "data": {
    "url": "https://yunlog-diary.oss-cn-hangzhou.aliyuncs.com/diary/images/20251014/def456.jpg"
  }
}
```

### 4.4 删除 OSS 文件
```http
DELETE /upload/file?url=https://xxx.oss-cn-hangzhou.aliyuncs.com/xxx.jpg
```

**限制**:
- 文件类型: 仅支持图片(`image/*`)
- 文件大小: 最大 10MB
- 文件名格式: `yyyyMMdd/UUID.扩展名`

---

## 5. 附件管理 API

### 5.1 获取日记的附件列表
```http
GET /attachments/diary/{diaryId}
```

### 5.2 获取附件详情
```http
GET /attachments/{id}
```

### 5.3 创建附件记录
```http
POST /attachments
Content-Type: application/json

{
  "diaryId": 1,
  "url": "https://yunlog-diary.oss-cn-hangzhou.aliyuncs.com/diary/images/xxx.jpg"
}
```

### 5.4 批量创建附件
```http
POST /attachments/batch
Content-Type: application/json

{
  "diaryId": 1,
  "urls": ["url1", "url2", "url3"]
}
```

### 5.5 删除附件
```http
DELETE /attachments/{id}
```
**注意**: 会同时删除数据库记录和 OSS 文件

### 5.6 删除日记的所有附件
```http
DELETE /attachments/diary/{diaryId}
```

### 5.7 批量删除附件
```http
DELETE /attachments/batch
Content-Type: application/json

{
  "ids": [1, 2, 3]
}
```

---

## 注意事项

1. **用户认证**: 除了注册和登录接口，其他所有API都需要在请求头中携带token
2. **数据隔离**: 所有数据按用户ID严格隔离，用户只能访问自己的数据
3. **级联删除**: 永久删除日记会同时删除附件记录和 OSS 文件
4. **默认分类**: 系统必须保留一个默认分类,删除其他分类时日记会移至默认分类
5. **软删除**: 普通删除只是标记 `is_deleted=true`,不会删除 OSS 文件
6. **文件命名**: OSS 文件按日期分文件夹存储: `diary/images/yyyyMMdd/UUID.ext`
7. **URL 格式**: 附件 URL 为完整的 OSS 访问地址
8. **并发上传**: 支持批量上传,但建议每批不超过 10 张图片
9. **Token有效期**: JWT token默认有效期为7天，过期需重新登录

---

## 测试工具

推荐使用以下工具测试:
- **Postman**: 全功能 API 测试
- **curl**: 命令行快速测试
- **前端集成**: uni-app 小程序

### Postman Collection 快速导入
```json
{
  "info": { "name": "云日记 API" },
  "item": [
    {
      "name": "上传图片",
      "request": {
        "method": "POST",
        "url": "http://localhost:8080/api/v1/upload/image",
        "body": {
          "mode": "formdata",
          "formdata": [
            { "key": "file", "type": "file" },
            { "key": "diaryId", "value": "1" }
          ]
        }
      }
    }
  ]
}
```

### curl 测试示例

#### 0. 用户认证测试 🔐

```bash
# 1. 用户注册
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "123456",
    "phone": "13800138000"
  }'

# 2. 用户登录
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "test",
    "password": "123456"
  }'

# 响应示例（保存返回的token）:
# {
#   "code": 200,
#   "data": {
#     "token": "eyJhbGciOiJIUzI1NiJ9...",
#     "userInfo": { "id": 1, "username": "test" }
#   }
# }

# 3. 使用token访问需要认证的API
# 将上一步获取的token替换到下面的{YOUR_TOKEN}
TOKEN="eyJhbGciOiJIUzI1NiJ9..."

# 获取分类列表（需要token）
curl -X GET http://localhost:8080/api/v1/categories \
  -H "Authorization: Bearer $TOKEN"

# 创建日记（需要token）
curl -X POST http://localhost:8080/api/v1/diaries \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "content": "今天天气不错",
    "contentHtml": "<p>今天天气不错</p>",
    "categoryId": 1
  }'

# 4. 登出
curl -X POST http://localhost:8080/api/v1/auth/logout \
  -H "Authorization: Bearer $TOKEN"
```

**认证流程说明**:
1. 首次使用：注册账号 → 获取token
2. 后续使用：登录 → 获取token
3. 所有业务API都需要携带token
4. token有效期7天，过期需重新登录

**快速测试脚本**:
```bash
# 保存为 test-auth.sh
#!/bin/bash

# 登录获取token
echo "=== 1. 登录测试 ==="
RESPONSE=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"123456"}')

echo $RESPONSE | jq '.'

# 提取token
TOKEN=$(echo $RESPONSE | jq -r '.data.token')
echo "Token: $TOKEN"

# 使用token访问API
echo -e "\n=== 2. 访问分类列表 ==="
curl -s -X GET http://localhost:8080/api/v1/categories \
  -H "Authorization: Bearer $TOKEN" | jq '.'

echo -e "\n=== 3. 创建日记 ==="
curl -s -X POST http://localhost:8080/api/v1/diaries \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "content": "测试日记",
    "contentHtml": "<p>测试日记</p>",
    "categoryId": 1
  }' | jq '.'
```

#### 1. 图片上传测试
```bash
# 方式1: 单张图片上传(推荐,带日记ID自动创建附件记录)
curl -X POST http://localhost:8080/api/v1/upload/image \
  -F "file=@/Users/doraemon/Documents/工作/云日记/resources/图标.png" \
  -F "diaryId=1"

# 方式2: 单张图片上传(不关联日记,仅上传到OSS)
curl -X POST http://localhost:8080/api/v1/upload/image \
  -F "file=@/Users/doraemon/Documents/工作/云日记/resources/图标.jpg"

# 方式3: 批量上传图片
curl -X POST http://localhost:8080/api/v1/upload/images \
  -F "files=@/Users/doraemon/Documents/工作/云日记/resources/图标.jpg" \
  -F "files=@/Users/doraemon/Documents/工作/云日记/resources/图标.png" \
  -F "diaryId=1"

# 方式4: 临时图片上传(编辑器预览用,不创建附件记录)
curl -X POST http://localhost:8080/api/v1/upload/temp-image \
  -F "file=@/Users/doraemon/Documents/工作/云日记/resources/图标.png"
```

**关键点说明**:
- ✅ 使用 `-F` 参数表示 `multipart/form-data` 格式
- ✅ 文件路径前必须加 `@` 符号
- ✅ 支持的图片格式: `.jpg`, `.jpeg`, `.png`, `.gif`, `.webp`
- ✅ 文件大小限制: 最大 10MB
- ✅ 提供 `diaryId` 参数会自动创建附件记录
- ✅ 不提供 `diaryId` 只上传到 OSS,需要后续手动关联

**文件路径填写规则**:
```bash
# macOS/Linux 绝对路径
-F "file=@/Users/用户名/Pictures/test.jpg"

# macOS/Linux 相对路径(相对于执行命令的目录)
-F "file=@./resources/test.jpg"

# Windows 绝对路径
-F "file=@C:/Users/用户名/Pictures/test.jpg"

# 项目中可用的测试图片
-F "file=@/Users/doraemon/Documents/工作/云日记/resources/图标.png"
-F "file=@/Users/doraemon/Documents/工作/云日记/frontend/YunLog/src/static/logo.png"
```

#### 2. 其他 API 测试

**重要**: 以下所有API都需要携带token！

```bash
# 先登录获取token
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"123456"}' | jq -r '.data.token')

# 创建日记（需要token）
curl -X POST http://localhost:8080/api/v1/diaries \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"content":"测试内容","contentHtml":"<p>测试内容</p>","categoryId":1}'

# 获取日记列表（需要token）
curl -X GET "http://localhost:8080/api/v1/diaries?page=1&limit=10" \
  -H "Authorization: Bearer $TOKEN"

# 创建分类（需要token）
curl -X POST http://localhost:8080/api/v1/categories \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"工作","icon":"💼","color":"#4CAF50"}'

# 上传图片（需要token）
curl -X POST http://localhost:8080/api/v1/upload/image \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@/path/to/image.jpg" \
  -F "diaryId=1"

# 删除分类（需要token）
curl -X DELETE http://localhost:8080/api/v1/categories/2 \
  -H "Authorization: Bearer $TOKEN"
```

---

## 数据隔离说明 🔒

用户认证系统实现后，所有数据都按用户ID进行严格隔离：

### 隔离规则
1. **日记隔离**: 每个用户只能查看、编辑、删除自己的日记
2. **分类隔离**: 每个用户有独立的分类系统
3. **附件隔离**: 附件跟随日记，只能访问自己日记的附件
4. **回收站隔离**: 每个用户有独立的回收站

### 自动操作
- **注册时**: 自动创建默认分类
- **创建日记**: 自动关联当前登录用户
- **创建分类**: 自动关联当前登录用户

### 错误处理
- **未登录**: 返回401错误，提示"未授权，请先登录"
- **访问他人数据**: 返回404错误，提示"资源不存在"

---

## 常见问题 FAQ

### Q1: 如何获取token？
A: 通过注册或登录API获取，保存返回的`data.token`字段。

### Q2: token如何使用？
A: 在请求头中添加: `Authorization: Bearer {token}`

### Q3: token过期了怎么办？
A: token有效期7天，过期后需要重新登录获取新token。

### Q4: 如何测试多用户数据隔离？
A: 
```bash
# 1. 注册用户A
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"userA","password":"123456"}'
# 保存tokenA

# 2. 注册用户B
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"userB","password":"123456"}'
# 保存tokenB

# 3. 用户A创建日记
curl -X POST http://localhost:8080/api/v1/diaries \
  -H "Authorization: Bearer $TOKEN_A" \
  -H "Content-Type: application/json" \
  -d '{"content":"用户A的日记","contentHtml":"<p>用户A的日记</p>","categoryId":1}'

# 4. 用户B获取日记列表（看不到用户A的日记）
curl -X GET http://localhost:8080/api/v1/diaries \
  -H "Authorization: Bearer $TOKEN_B"
```

### Q5: 微信登录如何测试？
A: 微信登录需要在微信小程序环境中测试，通过`wx.login()`获取code后调用API。

---

## 版本更新记录

### v2.0.0 (2025-10-28)
- ✅ 新增用户认证系统（注册、登录、微信登录）
- ✅ 实现JWT token认证机制
- ✅ 实现多用户数据隔离
- ✅ 新用户自动创建默认分类
- ✅ 所有API添加token验证

### v1.0.0 (2025-10-14)
- 初始版本，无用户认证模式
- 基础日记、分类、附件管理功能

````
