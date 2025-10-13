# Composition API 方法访问错误修复记录

## 问题描述
编辑日记时出现多个运行时错误：
1. `ReferenceError: getEditorContext is not defined`
2. `TypeError: this.loadCategories is not a function`  
3. `TypeError: _this3.initEditor is not a function`

## 根本原因
在使用 Vue 3 Composition API 时，从 `setup()` 函数返回的方法和状态需要正确地暴露给组件实例，以便在 `methods` 和生命周期钩子中访问。

## 错误分析

### 1. setup() 返回值不完整
**问题：** `getEditorContext` 和 `initEditor` 没有从 `useEditorContent` hook 中正确解构和返回。

**修复前：**
```javascript
const {
  // ... 其他方法
  getEditorContent,
  setEditorContent,
  clearEditor,
  insertContent
} = useEditorContent()

return {
  // ... 返回值中缺少 getEditorContext 和 initEditor
}
```

**修复后：**
```javascript
const {
  // ... 其他方法
  getEditorContent,
  setEditorContent,
  clearEditor,
  insertContent,
  getEditorContext,  // ✅ 新增
  initEditor         // ✅ 新增
} = useEditorContent()

return {
  // ... 包含所有需要的方法
  getEditorContext,
  initEditor
}
```

### 2. Vue 3 Composition API 访问机制
在 Vue 3 中，`setup()` 返回的方法可以通过 `this` 在 Options API 中访问：

```javascript
export default {
  setup() {
    const { loadCategories } = useCategories()
    return { loadCategories }
  },
  
  onLoad() {
    this.loadCategories()  // ✅ 可以正常访问
  },
  
  methods: {
    async someMethod() {
      await this.initEditor()  // ✅ 可以正常访问
    }
  }
}
```

## 修复方案

### 1. 完整解构 Hook 返回值
确保从每个 hook 中解构出所有需要的方法和状态。

### 2. 完整返回 setup() 值
确保 `setup()` 函数返回所有需要在组件中访问的方法和状态。

### 3. 保持一致的访问模式
在混合使用 Composition API 和 Options API 时，通过 `this` 访问 setup 返回的方法。

## 修复结果
✅ `getEditorContext` 和 `initEditor` 方法可以正常访问  
✅ `loadCategories` 方法可以在 `onLoad` 中正常调用  
✅ 编辑器初始化流程可以正常工作  
✅ 混合 API 模式可以正常运行

## 最佳实践

### 1. Hook 设计原则
```javascript
// 确保 hook 返回所有需要的方法
export function useEditorContent() {
  // ... 实现逻辑
  
  return {
    // 状态
    isEditing,
    currentDiary,
    editorCtx,
    
    // 方法 - 确保导出所有需要的方法
    onEditorReady,
    getEditorContent,
    setEditorContent,
    getEditorContext,  // 重要：不要遗漏
    initEditor         // 重要：不要遗漏
  }
}
```

### 2. setup() 完整性检查
```javascript
setup() {
  // 解构时确保包含所有需要的方法
  const {
    method1,
    method2,
    method3  // 确保不遗漏
  } = useHook()
  
  // 返回时确保包含所有方法
  return {
    method1,
    method2, 
    method3  // 确保不遗漏
  }
}
```

### 3. 调试技巧
- 在 `setup()` 返回后打印返回值，确认方法是否存在
- 在组件中使用前打印 `this`，检查方法是否正确绑定
- 使用 Vue DevTools 检查组件实例

## 经验总结
1. **完整性原则**: Hook 的所有导出都应该在需要时正确返回
2. **一致性原则**: 保持 Hook 接口的完整性，不要部分导出
3. **测试验证**: 每个 Hook 方法都需要验证是否可以正常访问
4. **混合模式注意**: 在使用 Composition API + Options API 时要格外注意方法的暴露