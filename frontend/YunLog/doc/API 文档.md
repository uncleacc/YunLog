# 📚 云日记 API 文档 V1.0

## 🎯 项目概览

### 技术栈
- **前端**: uni-app (Vue.js) + 微信小程序
- **后端**: RESTful API (建议 Node.js/Express 或 Python/FastAPI)
- **数据库**: MongoDB 或 MySQL
- **认证**: JWT Token

### 基础信息
- **Base URL**: `https://api.cloudiary.com/v1`
- **Content-Type**: `application/json`
- **认证方式**: Bearer Token

---

<!-- ## 🔐 认证模块

### 1.1 用户注册
```http
POST /auth/register
```

**请求参数:**
```json
{
  "username": "string",      // 用户名 (3-20字符)
  "password": "string",      // 密码 (6-20字符)
  "email": "string",         // 邮箱 (可选)
  "avatar": "string"         // 头像URL (可选)
}
```

**响应:**
```json
{
  "code": 200,
  "message": "注册成功",
  "data": {
    "userId": "string",
    "username": "string",
    "token": "string",
    "refreshToken": "string",
    "expiresIn": 3600
  }
}
```

### 1.2 用户登录
```http
POST /auth/login
```

**请求参数:**
```json
{
  "username": "string",
  "password": "string"
}
```

### 1.3 刷新Token
```http
POST /auth/refresh
```

**请求参数:**
```json
{
  "refreshToken": "string"
}
```

--- -->

## 📝 日记管理模块

### 2.1 获取日记列表
```http
GET /diaries
```

**查询参数:**
```
page: integer (页码，默认1)
limit: integer (每页条数，默认20)
categoryId: string (分类ID，可选)
keyword: string (搜索关键词，可选)
startDate: string (开始日期，格式: 2024-01-01)
endDate: string (结束日期)
sortBy: string (排序字段: createTime, updateTime)
sortOrder: string (排序方向: asc, desc，默认desc)
```

**响应:**
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "list": [
      {
        "id": "string",
        "title": "string",
        "content": "string",
        "contentHtml": "string",
        "attachments": ["string"],
        "categoryId": "string",
        "categoryName": "string",
        "createTime": "2024-01-01T10:00:00Z",
        "updateTime": "2024-01-01T10:30:00Z"
      }
    ],
    "total": 100,
    "page": 1,
    "limit": 20,
    "totalPages": 5
  }
}
```

### 2.2 获取日记详情
```http
GET /diaries/{id}
```

**响应:**
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "id": "string",
    "title": "string",
    "content": "string",
    "contentHtml": "string",
    "attachments": [
      {
        "id": "string",
        "filename": "string",
        "url": "string",
        "size": 1024,
        "type": "image/jpeg"
      }
    ],
    "categoryId": "string",
    "category": {
      "id": "string",
      "name": "string",
      "icon": "string",
      "color": "string"
    },
    "createTime": "2024-01-01T10:00:00Z",
    "updateTime": "2024-01-01T10:30:00Z"
  }
}
```

### 2.3 创建日记
```http
POST /diaries
```

**请求参数:**
```json
{
  "title": "string",          // 标题 (可选，默认"无标题")
  "content": "string",        // 纯文本内容
  "contentHtml": "string",    // 富文本内容 (可选)
  "categoryId": "string",     // 分类ID (可选，默认default)
  "attachments": [            // 附件 (可选)
    {
      "filename": "string",
      "url": "string",
      "size": 1024,
      "type": "string"
    }
  ]
}
```

**响应:**
```json
{
  "code": 201,
  "message": "创建成功",
  "data": {
    "id": "string",
    "title": "string",
    "content": "string",
    "contentHtml": "string",
    "attachments": [],
    "categoryId": "string",
    "createTime": "2024-01-01T10:00:00Z",
    "updateTime": "2024-01-01T10:00:00Z"
  }
}
```

### 2.4 更新日记
```http
PUT /diaries/{id}
```

**请求参数:**
```json
{
  "title": "string",
  "content": "string",
  "contentHtml": "string",
  "categoryId": "string",
  "attachments": []
}
```

### 2.5 删除日记 (移到垃圾桶)
```http
DELETE /diaries/{id}
```

**响应:**
```json
{
  "code": 200,
  "message": "已移到垃圾桶",
  "data": null
}
```

### 2.6 批量删除日记
```http
DELETE /diaries/batch
```

**请求参数:**
```json
{
  "ids": ["string", "string"]
}
```

---

## 🗂️ 分类管理模块

### 3.1 获取分类列表
```http
GET /categories
```

**响应:**
```json
{
  "code": 200,
  "message": "获取成功",
  "data": [
    {
      "id": "string",
      "name": "string",
      "icon": "string",          // 图标名称或emoji
      "color": "string",         // 颜色值 #FF6B6B
      "diaryCount": 5,           // 日记数量
      "recentDiary": {           // 最近一篇日记
        "id": "string",
        "title": "string",
        "createTime": "2024-01-01T10:00:00Z"
      },
      "createTime": "2024-01-01T10:00:00Z",
      "updateTime": "2024-01-01T10:00:00Z"
    }
  ]
}
```

### 3.2 获取分类详情
```http
GET /categories/{id}
```

### 3.3 创建分类
```http
POST /categories
```

**请求参数:**
```json
{
  "name": "string",     // 分类名称 (1-10字符)
  "icon": "string",     // 图标 (可选)
  "color": "string"     // 颜色 (可选，默认#FF9A76)
}
```

### 3.4 更新分类
```http
PUT /categories/{id}
```

### 3.5 删除分类
```http
DELETE /categories/{id}
```

**说明**: 删除分类时，该分类下的日记会自动移到默认分类，默认分类不能删除

### 3.6 获取分类统计
```http
GET /categories/{id}/stats
```

**响应:**
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "totalCount": 10,
    "thisMonth": 3,
    "thisWeek": 1,
    "recentDiary": {
      "id": "string",
      "title": "string",
      "createTime": "2024-01-01T10:00:00Z"
    }
  }
}
```

---

## 🗑️ 垃圾桶管理模块

### 4.1 获取垃圾桶列表
```http
GET /trash
```

**查询参数:**
```
page: integer
limit: integer
```

**响应:**
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "list": [
      {
        "id": "string",
        "title": "string",
        "content": "string",
        "categoryName": "string",
        "deletedTime": "2024-01-01T10:00:00Z",
        "expireTime": "2024-01-31T10:00:00Z"  // 30天后过期
      }
    ],
    "total": 5,
    "autoCleanDays": 30
  }
}
```

### 4.2 恢复日记
```http
POST /trash/{id}/restore
```

### 4.3 永久删除日记
```http
DELETE /trash/{id}
```

### 4.4 清空垃圾桶
```http
DELETE /trash/clear
```

### 4.5 批量恢复
```http
POST /trash/batch-restore
```

**请求参数:**
```json
{
  "ids": ["string", "string"]
}
```

---

## 📊 统计分析模块

### 5.1 获取全局统计
```http
GET /stats/global
```

**响应:**
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "totalDiaries": 100,
    "totalCategories": 8,
    "totalWords": 50000,
    "continuousDays": 15,        // 连续记录天数
    "thisMonth": 12,             // 本月日记数
    "thisWeek": 3,               // 本周日记数
    "averageWordsPerDiary": 500,
    "mostActiveCategory": {
      "id": "string",
      "name": "string",
      "count": 25
    }
  }
}
```

### 5.2 获取时间统计
```http
GET /stats/timeline
```

**查询参数:**
```
type: string (day/week/month/year)
year: integer
month: integer (可选)
```

**响应:**
```json
{
  "code": 200,
  "message": "获取成功",
  "data": [
    {
      "date": "2024-01-01",
      "count": 2,
      "words": 1000
    }
  ]
}
```

### 5.3 获取分类统计
```http
GET /stats/categories
```

**响应:**
```json
{
  "code": 200,
  "message": "获取成功",
  "data": [
    {
      "categoryId": "string",
      "categoryName": "string",
      "count": 10,
      "percentage": 25.5,
      "totalWords": 5000
    }
  ]
}
```

---

## 📎 文件上传模块

### 6.1 上传单个文件
```http
POST /upload/single
```

**请求**: `multipart/form-data`
```
file: File (图片/文档等)
type: string (diary/avatar/other)
```

**响应:**
```json
{
  "code": 200,
  "message": "上传成功",
  "data": {
    "id": "string",
    "filename": "string",
    "originalName": "string",
    "url": "string",
    "size": 1024,
    "type": "image/jpeg",
    "uploadTime": "2024-01-01T10:00:00Z"
  }
}
```

### 6.2 批量上传文件
```http
POST /upload/batch
```

### 6.3 删除文件
```http
DELETE /upload/{fileId}
```

---

## 🔍 搜索模块

### 7.1 全文搜索
```http
GET /search
```

**查询参数:**
```
q: string (搜索关键词)
type: string (diary/category/all)
page: integer
limit: integer
categoryId: string (可选)
dateRange: string (可选, 7d/30d/90d/1y)
```

**响应:**
```json
{
  "code": 200,
  "message": "搜索成功",
  "data": {
    "diaries": [
      {
        "id": "string",
        "title": "string",
        "content": "string",
        "highlight": "string",     // 高亮片段
        "categoryName": "string",
        "createTime": "2024-01-01T10:00:00Z"
      }
    ],
    "categories": [],
    "total": 10,
    "searchTime": "0.05s"
  }
}
```

### 7.2 搜索建议
```http
GET /search/suggestions
```

**查询参数:**
```
q: string (部分关键词)
```

---

## ⚙️ 用户设置模块

### 8.1 获取用户设置
```http
GET /settings
```

**响应:**
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "theme": "light",           // 主题 light/dark
    "autoSave": true,           // 自动保存
    "autoSaveInterval": 30,     // 自动保存间隔(秒)
    "trashRetentionDays": 30,   // 垃圾桶保留天数
    "defaultCategory": "string", // 默认分类
    "exportFormat": "json",     // 导出格式
    "notifications": {
      "dailyReminder": true,
      "reminderTime": "20:00"
    }
  }
}
```

### 8.2 更新用户设置
```http
PUT /settings
```

### 8.3 获取用户信息
```http
GET /user/profile
```

### 8.4 更新用户信息
```http
PUT /user/profile
```

---

## 📤 数据导入导出模块

### 9.1 导出数据
```http
GET /export
```

**查询参数:**
```
format: string (json/csv/markdown)
categoryId: string (可选)
startDate: string (可选)
endDate: string (可选)
includeTrash: boolean (是否包含垃圾桶，默认false)
```

**响应**: 文件下载

### 9.2 导入数据
```http
POST /import
```

**请求**: `multipart/form-data`
```
file: File (支持json/csv格式)
format: string
mergeStrategy: string (replace/merge)
```

---

## 📱 同步模块

### 10.1 获取同步状态
```http
GET /sync/status
```

### 10.2 手动同步
```http
POST /sync/manual
```

### 10.3 获取冲突列表
```http
GET /sync/conflicts
```

### 10.4 解决冲突
```http
POST /sync/conflicts/resolve
```

---

## 🚨 错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 201 | 创建成功 |
| 400 | 请求参数错误 |
| 401 | 未授权/Token无效 |
| 403 | 权限不足 |
| 404 | 资源不存在 |
| 409 | 资源冲突 |
| 413 | 文件过大 |
| 422 | 参数验证失败 |
| 429 | 请求频率限制 |
| 500 | 服务器内部错误 |

## 📝 通用响应格式

### 成功响应
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {},
  "timestamp": "2024-01-01T10:00:00Z"
}
```

### 错误响应
```json
{
  "code": 400,
  "message": "请求参数错误",
  "error": {
    "field": "title",
    "reason": "标题不能为空"
  },
  "timestamp": "2024-01-01T10:00:00Z"
}
```

## 🔒 安全规范

1. **认证**: 所有API(除登录注册外)需要Bearer Token
2. **HTTPS**: 生产环境必须使用HTTPS
3. **频率限制**: 
   - 普通API: 100次/分钟
   - 上传API: 10次/分钟
   - 搜索API: 30次/分钟
4. **文件上传限制**:
   - 单文件最大: 10MB
   - 支持格式: jpg, png, gif, pdf, txt, doc, docx
5. **数据验证**: 严格的参数验证和SQL注入防护