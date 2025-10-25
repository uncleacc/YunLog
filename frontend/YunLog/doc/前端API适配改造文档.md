# 前端 API 适配改造文档

## 已完成的工作

### 1. ✅ 创建 API 封装模块
- **文件**: `src/utils/request.js`
- **功能**: HTTP 请求封装，支持 GET/POST/PUT/DELETE/Upload
- **特性**: 统一错误处理、Loading 提示、响应拦截

- **文件**: `src/utils/api.js`
- **功能**: 所有后端 API 接口封装
- **包含**: 日记、分类、回收站、附件、上传等全部接口

### 2. ✅ 已适配的页面
- **`src/pages/index/index.vue`** - 首页（已改造完成）
  - 调用 `getCategoryList()` 获取分类列表
  - 调用 `getGlobalStats()` 获取全局统计
  - 调用 `getCategoryStats()` 获取各分类统计

- **`src/pages/category/category.vue`** - 分类日记列表（已改造完成）
  - 调用 `getCategoryDetail()` 获取分类详情
  - 调用 `getDiaryList()` 获取日记列表
  - 调用 `deleteDiary()` 删除日记

---

## 需要继续适配的页面

### 3. 🔄 回收站页面
**文件**: `src/pages/trash/trash.vue`

**需要修改的方法**:
```javascript
// 原方法 → 新方法
storage.GetTrashList() → getTrashList({ page, limit })
storage.RestoreFromTrash(id) → restoreDiary(id)
storage.PermanentDeleteDiary(id) → permanentDeleteDiary(id)
storage.ClearTrash() → clearTrash()
```

**修改步骤**:
1. 导入 API: `import { getTrashList, restoreDiary, permanentDeleteDiary, clearTrash } from '@/utils/api.js'`
2. 将所有方法改为 async/await
3. 添加 loading 状态和错误处理

---

### 4. 🔄 日记详情页面
**文件**: `src/pages/detail/detail.vue`

**需要修改的方法**:
```javascript
// 原方法 → 新方法
storage.GetDiaryById(id) → getDiaryDetail(id)
storage.DeleteDiary(id) → deleteDiary(id)
storage.GetCategoryById(categoryId) → getCategoryDetail(categoryId)
// 附件相关
getAttachmentsByDiary(diaryId)  // 新增：获取附件列表
```

**修改步骤**:
1. 导入 API: `import { getDiaryDetail, deleteDiary, getCategoryDetail, getAttachmentsByDiary } from '@/utils/api.js'`
2. 改造 `LoadData()` 方法
3. 添加附件数据加载

---

### 5. 🔄 日记编辑页面（重点）
**文件**: `src/pages/edit/edit.vue`

**需要修改的方法**:
```javascript
// 原方法 → 新方法
storage.GetDiaryById(id) → getDiaryDetail(id)
storage.AddDiary(diary) → createDiary(data)
storage.UpdateDiary(id, diary) → updateDiary(id, data)
storage.GetCategoryList() → getCategoryList()

// 图片上传（新增）
uploadTempImage(filePath) // 临时上传
uploadImage(filePath, diaryId) // 关联日记上传
```

**重要改造点**:
1. **图片上传流程改造**:
   - 编辑时临时上传: 使用 `uploadTempImage()`
   - 保存时关联: 使用 `batchCreateAttachments()`
   
2. **保存流程**:
   ```javascript
   // 1. 先保存日记
   const diary = await createDiary(data)
   
   // 2. 如果有临时图片，关联到日记
   if (tempImages.length > 0) {
     await batchCreateAttachments({
       diaryId: diary.id,
       urls: tempImages
     })
   }
   ```

3. **图片选择和上传**:
   ```javascript
   async chooseAndUploadImage() {
     // 选择图片
     const res = await uni.chooseImage({
       count: 9,
       sizeType: ['compressed'],
       sourceType: ['album', 'camera']
     })
     
     // 上传到 OSS
     for (const filePath of res.tempFilePaths) {
       const result = await uploadTempImage(filePath)
       // 保存图片 URL
       this.tempImages.push(result.url)
     }
   }
   ```

---

### 6. 🔄 分类管理页面
**文件**: `src/pages/category-manage/category-manage.vue`

**需要修改的方法**:
```javascript
// 原方法 → 新方法
storage.GetCategoryList() → getCategoryList()
storage.AddCategory(category) → createCategory(data)
storage.UpdateCategory(id, category) → updateCategory(id, data)
storage.DeleteCategory(id) → deleteCategory(id)
```

**注意事项**:
- 删除分类时，后端会自动将该分类下的日记移到默认分类并放入回收站
- 不能删除默认分类（后端会返回错误）

---

## 数据格式对比

### 本地存储格式 vs 后端API格式

#### 日记对象
```javascript
// 本地存储格式
{
  id: "1698765432",  // 字符串
  title: "标题",
  content: "内容",
  contentHtml: "<p>内容</p>",
  attachments: ["url1", "url2"],  // URL 数组
  categoryId: "default",  // 字符串
  createTime: "2023-10-31T12:00:00.000Z",
  updateTime: "2023-10-31T12:00:00.000Z"
}

// 后端 API 格式
{
  id: 1,  // 数字
  title: "标题",
  content: "内容",
  contentHtml: "<p>内容</p>",
  categoryId: 1,  // 数字
  isDeleted: false,
  deletedTime: null,
  createTime: "2023-10-31 12:00:00",
  updateTime: "2023-10-31 12:00:00"
}

// 附件需要单独查询
// GET /attachments/diary/{diaryId}
[
  {
    id: 1,
    diaryId: 1,
    url: "https://xxx.oss.com/xxx.jpg",
    createTime: "2023-10-31 12:00:00"
  }
]
```

#### 分类对象
```javascript
// 本地存储格式
{
  id: "default",  // 字符串
  name: "默认分类",
  color: "#FF9A76",
  icon: "📝",
  createTime: "2023-10-31T12:00:00.000Z"
}

// 后端 API 格式
{
  id: 1,  // 数字
  name: "默认分类",
  color: "#FF9A76",
  icon: "📝",
  isDefault: true,  // 新增字段
  createTime: "2023-10-31 12:00:00",
  updateTime: "2023-10-31 12:00:00"
}
```

---

## 关键改造点总结

### 1. ID 类型变化
- ❌ 本地: 字符串 ID (`"1698765432"`)
- ✅ 后端: 数字 ID (`1`)
- **注意**: URL 参数传递时自动转换

### 2. 时间格式变化
- ❌ 本地: ISO 8601 (`"2023-10-31T12:00:00.000Z"`)
- ✅ 后端: 简化格式 (`"2023-10-31 12:00:00"`)
- **处理**: 使用 `new Date()` 解析都兼容

### 3. 附件处理变化
- ❌ 本地: 日记对象内包含 `attachments` 数组
- ✅ 后端: 需要单独调用 API 获取附件列表
- **处理**: 
  ```javascript
  const diary = await getDiaryDetail(id)
  const attachments = await getAttachmentsByDiary(id)
  diary.attachments = attachments
  ```

### 4. 分页处理
- ❌ 本地: 一次性返回全部数据
- ✅ 后端: 分页返回
  ```javascript
  {
    list: [...],  // 当前页数据
    total: 100,   // 总数
    page: 1,      // 当前页
    limit: 20     // 每页数量
  }
  ```

### 5. 图片上传流程
- ❌ 本地: 直接保存本地临时路径
- ✅ 后端: 需要上传到 OSS，保存 OSS URL
  ```javascript
  // 编辑时临时上传
  const result = await uploadTempImage(filePath)
  // 保存时关联
  await batchCreateAttachments({
    diaryId: diaryId,
    urls: [result.url]
  })
  ```

---

## 建议的改造顺序

1. ✅ **首页** (index.vue) - 已完成
2. ✅ **分类列表页** (category.vue) - 已完成
3. 🔄 **回收站页** (trash.vue) - 简单，优先改
4. 🔄 **详情页** (detail.vue) - 中等难度
5. 🔄 **分类管理页** (category-manage.vue) - 简单
6. 🔄 **编辑页** (edit.vue) - **最复杂，最后改**

---

## API 配置说明

### 修改后端地址
**文件**: `src/utils/request.js`
```javascript
// 开发环境
const BASE_URL = 'http://localhost:8080/api/v1'

// 生产环境
const BASE_URL = 'https://your-domain.com/api/v1'
```

### 测试后端连接
```javascript
// 在任意页面的 onLoad 中测试
import { getCategoryList } from '@/utils/api.js'

async onLoad() {
  try {
    const list = await getCategoryList()
    console.log('后端连接成功:', list)
  } catch (error) {
    console.error('后端连接失败:', error)
  }
}
```

---

## 常见问题处理

### 1. 跨域问题
- 小程序不存在跨域问题
- H5 开发需要后端配置 CORS（已配置）

### 2. 网络请求失败
- 检查后端服务是否启动
- 检查 BASE_URL 配置是否正确
- 检查手机/模拟器能否访问后端地址

### 3. 数据格式不匹配
- 使用 `console.log` 打印 API 返回数据
- 对比文档中的数据格式
- 调整前端代码以适配后端格式

### 4. 图片显示问题
- OSS 图片 URL 必须是完整路径
- 检查 OSS Bucket 读写权限（应为公共读）
- 检查图片 URL 是否可以直接访问

---

## 下一步行动

1. **修改 trash.vue**（回收站页面）
2. **修改 detail.vue**（详情页）
3. **修改 category-manage.vue**（分类管理）
4. **修改 edit.vue**（编辑页，重点）
5. **全面测试所有功能**
6. **处理边界情况和错误**

需要我帮你修改某个具体页面吗？
