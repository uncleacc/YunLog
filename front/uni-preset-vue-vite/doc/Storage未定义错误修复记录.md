# Storage 未定义错误修复记录

## 问题描述
在编辑日记时出现运行时错误：
```
ReferenceError: storage is not defined
at Proxy.LoadDiary (edit.js? [sm]:555)
at Proxy.onLoad (edit.js? [sm]:308)
```

## 错误位置
`src/pages/edit/edit.vue` 文件的 `LoadDiary()` 方法中：
```javascript
LoadDiary() {
  const diary = storage.GetDiaryById(this.diaryId)  // ❌ storage 未定义
  // ...
}
```

## 根本原因
在组件拆分过程中，移除了原有的 `storage` 导入，但 `edit.vue` 文件中仍有方法在使用 `storage` 对象。

## 修复方案

### 添加缺失的导入
**修复前：**
```javascript
import { useEditorFormat } from './hooks/useEditorFormat.js'
import { useEditorContent } from './hooks/useEditorContent.js'
import { useCategories } from './hooks/useCategories.js'
import { useDiarySave } from './hooks/useDiarySave.js'
// ❌ 缺少 storage 导入

export default {
  // ...
}
```

**修复后：**
```javascript
import { useEditorFormat } from './hooks/useEditorFormat.js'
import { useEditorContent } from './hooks/useEditorContent.js'
import { useCategories } from './hooks/useCategories.js'
import { useDiarySave } from './hooks/useDiarySave.js'
import storage from '../../utils/storage.js'  // ✅ 添加导入

export default {
  // ...
}
```

## 影响的方法
修复后，以下方法可以正常工作：
1. `LoadDiary()` - 加载现有日记数据
2. `SaveDiary()` - 保存日记（使用 `storage.AddDiary` 和 `storage.UpdateDiary`）

## 修复验证
✅ 编译无错误  
✅ `storage.GetDiaryById()` 可以正常调用  
✅ 编辑现有日记功能恢复正常  

## 经验总结
1. **依赖完整性**: 重构时要确保所有依赖都正确导入
2. **渐进式修改**: 可以保留部分原有代码，逐步使用新的 hooks
3. **测试覆盖**: 每个功能点都需要测试，包括新建和编辑场景
4. **代码审查**: 检查是否有遗漏的导入或引用

## 后续优化建议
考虑将 `LoadDiary` 方法也迁移到 hooks 中，实现更彻底的逻辑分离：
```javascript
// 可以创建 useDiaryLoader.js hook
const { loadDiary, currentDiary } = useDiaryLoader()
```