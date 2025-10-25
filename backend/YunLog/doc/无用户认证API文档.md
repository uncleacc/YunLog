# 云日记 API 文档（无用户认证版）

> 本文档适用于无用户认证版本，所有接口无需登录即可直接访问。

---

## 🩺 健康检查

### GET /api/v1/health
- 功能：检查服务是否正常运行
- 响应示例：
```json
{
  "code": 200,
  "message": "success",
  "data": "云日记API服务运行正常"
}
```

---

## 📂 分类管理

### GET /api/v1/categories
- 功能：获取所有分类列表
- 响应：`List<Category>`

### GET /api/v1/categories/{id}
- 功能：获取分类详情
- 响应：`Category`

### POST /api/v1/categories
- 功能：创建新分类
- 请求体：
```json
{
  "name": "分类名",
  "icon": "图标",
  "color": "颜色"
}
```
- 响应：`Category`

### PUT /api/v1/categories/{id}
- 功能：更新分类
- 请求体同上
- 响应：`Category`

### DELETE /api/v1/categories/{id}
- 功能：删除分类
- 响应：无内容

### GET /api/v1/categories/{id}/stats
- 功能：获取分类统计信息
- 响应：
```json
{
  "totalCount": 10,
  "thisMonth": 2,
  "thisWeek": 1,
  "recentDiary": {
    "id": 1,
    "title": "最近的日记标题",
    "createTime": "2025-10-14T12:00:00"
  }
}
```

---

## 📒 日记管理

### GET /api/v1/diaries
- 功能：获取日记列表
- 查询参数：
  - `page`（默认1）
  - `limit`（默认20）
  - `categoryId`（可选）
  - `keyword`（可选，模糊搜索标题和内容）
- 响应：分页对象 `PageResponse<Diary>`

### GET /api/v1/diaries/{id}
- 功能：获取日记详情
- 响应：`Diary`

### POST /api/v1/diaries
- 功能：创建日记
- 请求体：
```json
{
  "title": "日记标题",
  "content": "正文内容",
  "contentHtml": "HTML内容",
  "categoryId": 1
}
```
- 响应：`Diary`

### PUT /api/v1/diaries/{id}
- 功能：更新日记
- 请求体同上
- 响应：`Diary`

### DELETE /api/v1/diaries/{id}
- 功能：删除日记（移到垃圾桶）
- 响应：无内容

### DELETE /api/v1/diaries/batch
- 功能：批量删除日记
- 请求体：
```json
{
  "ids": [1, 2, 3]
}
```
- 响应：无内容

---

## 🗑️ 垃圾桶管理

### GET /api/v1/trash
- 功能：获取垃圾桶中的日记列表
- 查询参数：`page`、`limit`
- 响应：分页对象 `PageResponse<Diary>`

### POST /api/v1/trash/{id}/restore
- 功能：恢复指定日记
- 响应：无内容

### DELETE /api/v1/trash/{id}
- 功能：永久删除指定日记
- 响应：无内容

### DELETE /api/v1/trash/clear
- 功能：清空垃圾桶
- 响应：无内容

### POST /api/v1/trash/batch-restore
- 功能：批量恢复日记
- 请求体：
```json
{
  "ids": [1, 2, 3]
}
```
- 响应：无内容

---

## 📦 数据结构说明

### Category
```json
{
  "id": 1,
  "name": "分类名",
  "icon": "图标",
  "color": "颜色",
  "isDefault": false,
  "createTime": "2025-10-14T12:00:00",
  "updateTime": "2025-10-14T12:00:00"
}
```

### Diary
```json
{
  "id": 1,
  "title": "日记标题",
  "content": "正文内容",
  "contentHtml": "HTML内容",
  "categoryId": 1,
  "isDeleted": false,
  "deletedTime": null,
  "createTime": "2025-10-14T12:00:00",
  "updateTime": "2025-10-14T12:00:00"
}
```

### PageResponse<T>
```json
{
  "content": [ ... ],
  "total": 100,
  "page": 1,
  "limit": 20
}
```

---

> 如需更多示例或有疑问，请联系开发者。
