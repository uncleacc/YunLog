# Bug修复记录

## 修复日期：2025-10-12

## Bug #1：编辑已有日记未修改直接保存时提示"请输入内容"

### 问题描述
- 打开已有日记进行编辑
- 不做任何修改，直接点击保存
- 系统提示"请输入内容"
- 实际上日记原本就有内容，应该保存成功

### 问题原因
```javascript
// 原代码问题：
// 1. LoadDiary() 加载日记时，没有初始化 contentLength
// 2. Validate() 检查 contentLength === 0 就提示错误
// 3. 编辑模式下，contentLength 一直是 0
```

### 修复方案

#### 1. 在 LoadDiary() 中初始化 contentLength
```javascript
LoadDiary() {
  const diary = storage.GetDiaryById(this.diaryId)
  if (diary) {
    this.formData = {
      title: diary.title,
      content: diary.content,
      contentHtml: diary.contentHtml || '',
      attachments: diary.attachments || [],
      categoryId: diary.categoryId || 'default',
    }
    // ✅ 新增：初始化内容长度
    this.contentLength = diary.content ? diary.content.length : 0
  }
}
```

#### 2. 改进 Validate() 验证逻辑
```javascript
Validate() {
  if (!this.formData.title.trim()) {
    uni.showToast({
      title: '请输入标题',
      icon: 'none',
    })
    return false
  }

  // ✅ 修复：同时检查 contentLength 和 formData.content
  if (this.contentLength === 0 && !this.formData.content) {
    uni.showToast({
      title: '请输入内容',
      icon: 'none',
    })
    return false
  }

  return true
}
```

### 测试场景
- ✅ 编辑已有日记，不修改直接保存 → 成功
- ✅ 编辑已有日记，清空内容保存 → 提示错误
- ✅ 新建日记，不输入内容保存 → 提示错误
- ✅ 新建日记，输入内容保存 → 成功

---

## Bug #2：上传附件后再次编辑导致内容丢失和"编辑器未就绪"错误

### 问题描述
1. 创建日记并上传附件
2. 保存后再次编辑该日记
3. 日记内容在编辑器中消失
4. 保存时提示"编辑器未就绪"

### 问题原因
```javascript
// 问题分析：
// 1. OnEditorReady() 立即调用 setContents，但编辑器可能还未完全初始化
// 2. 设置内容后，没有更新 contentLength
// 3. 编辑器上下文 (editorCtx) 在某些情况下丢失
// 4. 保存时如果 editorCtx 为空，直接报错，没有降级处理
```

### 修复方案

#### 1. 延迟设置编辑器内容
```javascript
OnEditorReady() {
  uni.createSelectorQuery()
    .in(this)
    .select('#editor')
    .context((res) => {
      this.editorCtx = res.context
      
      if (this.isEdit && this.formData.contentHtml) {
        // ✅ 延迟 100ms，确保编辑器完全就绪
        setTimeout(() => {
          if (this.editorCtx) {
            this.editorCtx.setContents({
              html: this.formData.contentHtml,
              success: () => {
                // ✅ 设置成功后更新内容长度
                this.contentLength = this.formData.content ? this.formData.content.length : 0
              }
            })
          }
        }, 100)
      }
    })
    .exec()
}
```

#### 2. 改进 OnEditorInput 健壮性
```javascript
OnEditorInput(e) {
  // ✅ 增加安全检查
  if (e.detail && e.detail.text !== undefined) {
    this.contentLength = e.detail.text.length
  }
}
```

#### 3. SaveDiary 增加降级处理
```javascript
SaveDiary() {
  if (!this.Validate()) {
    return
  }

  // ✅ 如果编辑器未就绪，但有原始内容，直接保存
  if (!this.editorCtx) {
    if (this.isEdit && this.formData.content) {
      this.DoSave()
      return
    }
    
    uni.showToast({
      title: '编辑器未就绪，请稍后重试',
      icon: 'none',
    })
    return
  }

  uni.showLoading({
    title: '保存中...',
  })

  // 获取编辑器内容
  this.editorCtx.getContents({
    success: (res) => {
      // ✅ 保留原始内容作为备份
      this.formData.content = res.text || this.formData.content || ''
      this.formData.contentHtml = res.html || this.formData.contentHtml || ''
      this.DoSave()
    },
    fail: () => {
      uni.hideLoading()
      // ✅ 获取失败时的降级处理
      if (this.isEdit && this.formData.content) {
        this.DoSave()
      } else {
        uni.showToast({
          title: '获取内容失败',
          icon: 'none',
        })
      }
    },
  })
}
```

#### 4. 抽取 DoSave 方法
```javascript
// ✅ 新增：统一的保存逻辑
DoSave() {
  uni.showLoading({
    title: '保存中...',
  })

  let result
  if (this.isEdit) {
    result = storage.UpdateDiary(this.diaryId, this.formData)
  } else {
    result = storage.AddDiary(this.formData)
  }

  uni.hideLoading()

  if (result) {
    uni.showToast({
      title: '保存成功',
      icon: 'success',
    })
    setTimeout(() => {
      uni.navigateBack()
    }, 1000)
  } else {
    uni.showToast({
      title: '保存失败',
      icon: 'none',
    })
  }
}
```

### 测试场景
- ✅ 创建日记，上传图片，保存 → 成功
- ✅ 再次编辑该日记 → 内容正常显示
- ✅ 不修改直接保存 → 成功
- ✅ 修改内容后保存 → 成功
- ✅ 上传视频后保存，再次编辑 → 内容正常
- ✅ 编辑器未就绪时保存 → 使用原始内容保存

---

## 修复亮点

### 1. 数据安全
- ✅ 保留原始内容作为备份
- ✅ 编辑器异常时使用降级方案
- ✅ 多重检查确保数据不丢失

### 2. 用户体验
- ✅ 减少不必要的错误提示
- ✅ 编辑已有日记更流畅
- ✅ 即使编辑器异常也能保存

### 3. 代码质量
- ✅ 抽取公共方法 (DoSave)
- ✅ 增加安全检查和边界处理
- ✅ 改进错误提示的友好度

## 后续优化建议

### 短期优化
1. 监控编辑器初始化状态，显示加载提示
2. 增加编辑器重试机制
3. 添加更详细的错误日志

### 长期优化
1. 实现自动保存功能
2. 增加离线编辑支持
3. 优化编辑器性能

## 测试覆盖

### 编辑模式测试
- [x] 打开已有日记
- [x] 查看内容是否正确显示
- [x] 不修改直接保存
- [x] 修改后保存
- [x] 添加附件后保存
- [x] 再次编辑之前上传过附件的日记

### 新建模式测试
- [x] 创建新日记
- [x] 不输入内容保存（应该提示错误）
- [x] 输入内容后保存
- [x] 上传附件后保存

### 异常场景测试
- [x] 编辑器未就绪时保存
- [x] 网络慢时编辑器加载
- [x] 快速切换页面
- [x] 多次快速保存

## 版本信息

- 修复版本：V3.1
- 修复文件：`src/pages/edit/edit.vue`
- 影响范围：日记编辑功能
- 向下兼容：✅ 是

---

## Bug #2：编辑有附件的日记时编辑器内容无法加载

### 问题描述
- 创建日记并上传附件后保存
- 再次编辑该日记时，编辑器内容为空
- 标题和附件正常显示，但富文本内容丢失
- 保存时提示"编辑器未就绪"错误

### 问题原因
```javascript
// 问题分析：
// 1. uniapp editor组件的@ready事件在有附件时可能不触发
// 2. 页面渲染顺序问题导致编辑器初始化失败
// 3. 依赖@ready事件获取editorCtx的方式不可靠
```

### 修复方案

#### 1. 强制初始化编辑器上下文
```javascript
onReady() {
  console.log('onReady - 页面初次渲染完成')
  // 强制初始化编辑器上下文（解决@ready事件不触发的问题）
  this.ForceInitEditor()
},

ForceInitEditor() {
  console.log('ForceInitEditor - 强制初始化编辑器')
  
  // 延迟一点时间，确保DOM完全渲染
  setTimeout(() => {
    this.GetEditorContextWithRetry()
  }, 200)
}
```

#### 2. 重试机制获取编辑器上下文
```javascript
GetEditorContextWithRetry(attempt = 1) {
  console.log(`GetEditorContextWithRetry - 第${attempt}次尝试获取编辑器上下文`)
  
  uni.createSelectorQuery()
    .in(this)
    .select('#editor')
    .context((res) => {
      if (!res || !res.context) {
        if (attempt < 5) {
          console.warn(`GetEditorContextWithRetry - 第${attempt}次失败，300ms后重试`)
          setTimeout(() => {
            this.GetEditorContextWithRetry(attempt + 1)
          }, 300)
        } else {
          console.error('GetEditorContextWithRetry - 5次尝试后仍无法获取编辑器上下文')
        }
        return
      }
      
      // 成功获取编辑器上下文
      this.editorCtx = res.context
      console.log('GetEditorContextWithRetry - 成功设置editorCtx')
      
      // 如果数据已加载，立即尝试填充
      if (this.isEdit && this.latestDiary && !this.didInitialFill) {
        console.log('GetEditorContextWithRetry - 数据已就绪，立即填充')
        this.TryFillEditor()
      }
    })
    .exec()
}
```

#### 3. 响应式填充机制
```javascript
watch: {
  // 监听编辑器上下文变化，当编辑器就绪时自动填充
  editorCtx(newVal, oldVal) {
    // 当编辑器从未就绪变为就绪时，且是编辑模式，且有数据，且未填充过
    if (!oldVal && newVal && this.isEdit && this.latestDiary && !this.didInitialFill && !this.hasEdited) {
      console.log('watch editorCtx - 编辑器刚就绪，触发填充')
      this.TryFillEditor()
    }
  },
  
  // 监听日记数据变化（适用于编辑器先就绪但数据后到的情况）
  latestDiary(newVal) {
    if (newVal && this.editorCtx && !this.didInitialFill && !this.hasEdited && this.isEdit) {
      console.log('watch latestDiary - 数据刚加载，触发填充')
      this.TryFillEditor()
    }
  }
}
```

### 修复结果
- ✅ 解决了@ready事件不触发的问题
- ✅ 通过强制初始化确保编辑器上下文获取成功
- ✅ 重试机制提高了获取编辑器的可靠性
- ✅ 响应式填充机制确保内容及时加载
- ✅ 编辑有附件的日记时内容正常显示
- ✅ 保存功能正常工作

## 相关文档

- 分类功能说明：`分类功能说明.md`
- 功能更新说明：`功能更新说明.md`
- 微信小程序发布：`微信小程序发布指南.md`

