# 云日记 API 测试示例

## 1. 用户注册
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "123456",
    "email": "test@example.com"
  }'
```

## 2. 用户登录
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "123456"
  }'
```

## 3. 获取分类列表（需要token）
```bash
curl -X GET http://localhost:8080/api/v1/categories \
  -H "Authorization: Bearer YOUR_TOKEN"
```

## 4. 创建分类
```bash
curl -X POST http://localhost:8080/api/v1/categories \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "name": "工作日志",
    "icon": "💼",
    "color": "#FF6B6B"
  }'
```

## 5. 创建日记
```bash
curl -X POST http://localhost:8080/api/v1/diaries \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "title": "我的第一篇日记",
    "content": "今天是个好日子...",
    "categoryId": 1
  }'
```

## 6. 获取日记列表
```bash
curl -X GET "http://localhost:8080/api/v1/diaries?page=1&limit=10" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

## 7. 搜索日记
```bash
curl -X GET "http://localhost:8080/api/v1/diaries?keyword=工作&page=1&limit=10" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

## 8. 更新日记
```bash
curl -X PUT http://localhost:8080/api/v1/diaries/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "title": "修改后的标题",
    "content": "修改后的内容...",
    "categoryId": 1
  }'
```

## 9. 删除日记（移到垃圾桶）
```bash
curl -X DELETE http://localhost:8080/api/v1/diaries/1 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

## 10. 获取垃圾桶列表
```bash
curl -X GET "http://localhost:8080/api/v1/trash?page=1&limit=10" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

## 响应格式示例

### 成功响应
```json
{
  "code": 200,
  "message": "操作成功",
  "data": { 
    // 具体数据
  },
  "timestamp": "2024-10-14 10:00:00"
}
```

### 错误响应
```json
{
  "code": 400,
  "message": "请求参数错误",
  "timestamp": "2024-10-14 10:00:00"
}
```