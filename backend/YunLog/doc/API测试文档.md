# 云日记后端 API 测试文档

## 测试环境

- **Base URL**: `http://localhost:8080/api/v1`
- **OSS Bucket**: `yunlog-diary`
- **OSS Region**: `oss-cn-hangzhou`

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

1. **级联删除**: 永久删除日记会同时删除附件记录和 OSS 文件
2. **默认分类**: 系统必须保留一个默认分类,删除其他分类时日记会移至默认分类
3. **软删除**: 普通删除只是标记 `is_deleted=true`,不会删除 OSS 文件
4. **文件命名**: OSS 文件按日期分文件夹存储: `diary/images/yyyyMMdd/UUID.ext`
5. **URL 格式**: 附件 URL 为完整的 OSS 访问地址
6. **并发上传**: 支持批量上传,但建议每批不超过 10 张图片

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
```bash
# 创建日记
curl -X POST http://localhost:8080/api/v1/diaries \
  -H "Content-Type: application/json" \
  -d '{"title":"测试","content":"内容","categoryId":1}'

# 获取日记列表
curl http://localhost:8080/api/v1/diaries?page=1&limit=10

# 删除分类
curl -X DELETE http://localhost:8080/api/v1/categories/2
```
