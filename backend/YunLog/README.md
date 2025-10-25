# 云日记后端工程部署文档

## 1. 数据库配置

### 1.1 创建数据库
```sql
CREATE DATABASE yunlog CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE yunlog;
```

### 1.2 数据库连接配置
在 `application.properties` 中已配置：
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/yunlog?useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=
```

**注意**: 请根据实际情况修改数据库密码。

## 2. 项目启动

### 2.1 编译项目
```bash
cd backend/YunLog
mvn clean compile
```

### 2.2 启动项目
```bash
mvn spring-boot:run
```

### 2.3 验证启动
访问: http://localhost:8080/api/v1/health

期望响应:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": "云日记API服务运行正常",
  "timestamp": "2024-10-14 10:00:00"
}
```

## 3. 数据库表结构

项目启动时会自动创建以下表：

### 3.1 用户表 (users)
- id: 主键，自增
- username: 用户名，唯一
- password: 加密密码
- email: 邮箱
- avatar: 头像URL
- create_time: 创建时间
- update_time: 更新时间

### 3.2 分类表 (categories)
- id: 主键，自增
- name: 分类名称
- icon: 图标
- color: 颜色
- user_id: 用户ID
- is_default: 是否默认分类
- create_time: 创建时间
- update_time: 更新时间

### 3.3 日记表 (diaries)
- id: 主键，自增
- title: 标题
- content: 纯文本内容
- content_html: 富文本内容
- user_id: 用户ID
- category_id: 分类ID
- is_deleted: 是否删除
- deleted_time: 删除时间
- create_time: 创建时间
- update_time: 更新时间

### 3.4 附件表 (attachments)
- id: 主键，自增
- filename: 文件名
- original_name: 原始文件名
- url: 文件URL
- size: 文件大小
- type: 文件类型
- user_id: 用户ID
- diary_id: 日记ID
- upload_time: 上传时间

## 4. API接口说明

### 4.1 认证接口
- POST /api/v1/auth/register - 用户注册
- POST /api/v1/auth/login - 用户登录
- POST /api/v1/auth/refresh - 刷新token

### 4.2 分类接口
- GET /api/v1/categories - 获取分类列表
- POST /api/v1/categories - 创建分类
- PUT /api/v1/categories/{id} - 更新分类
- DELETE /api/v1/categories/{id} - 删除分类
- GET /api/v1/categories/{id}/stats - 获取分类统计

### 4.3 日记接口
- GET /api/v1/diaries - 获取日记列表（支持分页、分类筛选、搜索）
- POST /api/v1/diaries - 创建日记
- GET /api/v1/diaries/{id} - 获取日记详情
- PUT /api/v1/diaries/{id} - 更新日记
- DELETE /api/v1/diaries/{id} - 删除日记（移到垃圾桶）
- DELETE /api/v1/diaries/batch - 批量删除日记

### 4.4 垃圾桶接口
- GET /api/v1/trash - 获取垃圾桶列表
- POST /api/v1/trash/{id}/restore - 恢复日记
- DELETE /api/v1/trash/{id} - 永久删除日记
- DELETE /api/v1/trash/clear - 清空垃圾桶
- POST /api/v1/trash/batch-restore - 批量恢复

## 5. 安全配置

### 5.1 JWT Token
- 访问token有效期: 1小时
- 刷新token有效期: 24小时
- 密钥: 可在 `application.properties` 中修改

### 5.2 认证方式
除了认证接口外，其他接口都需要在请求头中携带token:
```
Authorization: Bearer <token>
```

## 6. 开发调试

### 6.1 查看日志
项目启动后，JPA会打印SQL语句，方便调试。

### 6.2 数据库管理
推荐使用 phpMyAdmin, Navicat 或其他数据库管理工具查看数据。

### 6.3 API测试
推荐使用 Postman 或其他API测试工具进行接口测试。

## 7. 注意事项

1. **数据库密码**: 请在 `application.properties` 中配置正确的数据库密码
2. **端口冲突**: 默认使用8080端口，如有冲突可在配置文件中修改
3. **跨域配置**: 已配置允许所有来源的跨域请求，生产环境建议限制具体域名
4. **JWT密钥**: 生产环境请使用更强的密钥并妥善保管

## 8. 后续功能扩展

当前已实现基础功能，后续可扩展：
- 文件上传功能
- 统计分析功能
- 搜索功能增强
- 数据导入导出
- 用户设置功能