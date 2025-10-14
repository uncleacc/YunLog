// 编辑器内容管理 Hook
import { ref, nextTick } from 'vue'

export function useEditorContent() {
  // 是否正在编辑（修改已有日记）
  const isEditing = ref(false)
  
  // 当前日记数据
  const currentDiary = ref(null)
  
  // 编辑器实例
  const editorCtx = ref(null)

  // 初始化编辑器（现在通过 setEditorContext 设置）
  const initEditor = () => {
    console.log('InitEditor - 编辑器初始化（等待上下文设置）')
    return editorCtx.value
  }

  // 处理编辑器选择变化
  const onEditorSelectionChange = (e, { updateToolbarFromSelection, syncGlobalFormatsWithCursor, hasActiveGlobalFormats }) => {
    if (!editorCtx.value) return
    
    console.log('onEditorSelectionChange - 编辑器选择变化:', e.detail)
    
    // 直接从事件中获取格式状态，这通常是最准确的
    const eventFormats = e.detail || {}
    
    // 使用 nextTick 确保选择变化完成
    nextTick(() => {
      // 获取当前选中文本信息
      editorCtx.value.getSelectionText({
        success: (selectionRes) => {
          const hasSelection = selectionRes.text && selectionRes.text.length > 0
          console.log('onEditorSelectionChange - 选中文本:', hasSelection ? `"${selectionRes.text}"` : '(无)', '长度:', selectionRes.text?.length || 0)
          
          // 更新工具栏状态
          console.log('onEditorSelectionChange - 使用格式状态:', eventFormats)
          updateToolbarFromSelection(eventFormats, hasSelection)
          
          // 如果没有选中文本且有激活的全局格式，同步到当前位置
          if (!hasSelection && hasActiveGlobalFormats()) {
            syncGlobalFormatsWithCursor(editorCtx.value, eventFormats)
          }
        },
        fail: (err) => {
          console.error('onEditorSelectionChange - 获取选中文本失败:', err)
          // 降级处理：假设没有选中文本
          console.log('onEditorSelectionChange - 降级处理，使用格式状态:', eventFormats)
          updateToolbarFromSelection(eventFormats, false)
        }
      })
    })
  }

  // 处理编辑器输入
  const onEditorInput = (e) => {
    console.log('OnEditorInput - 编辑器输入事件:', e.detail)
    // 这里可以添加其他输入处理逻辑，如自动保存等
  }

  // 处理编辑器聚焦
  const onEditorFocus = (e) => {
    console.log('OnEditorFocus - 编辑器获得焦点:', e.detail)
  }

  // 处理编辑器失焦
  const onEditorBlur = (e) => {
    console.log('OnEditorBlur - 编辑器失去焦点:', e.detail)
  }

  // 处理编辑器就绪
  const onEditorReady = (e) => {
    console.log('OnEditorReady - 编辑器就绪:', e?.detail || e)
    initEditor()
  }

  // 设置编辑器上下文
  const setEditorContext = (context) => {
    editorCtx.value = context
    console.log('SetEditorContext - 编辑器上下文已设置')
  }

  // 获取编辑器内容
  const getEditorContent = () => {
    return new Promise((resolve, reject) => {
      if (!editorCtx.value) {
        reject(new Error('编辑器实例不存在'))
        return
      }

      editorCtx.value.getContents({
        success: (res) => {
          // 确保表情符号在内容中正确保存
          if (res.html && res.text) {
            // 检查是否包含表情符号
            const emojiRegex = /[\u{1F600}-\u{1F64F}]|[\u{1F300}-\u{1F5FF}]|[\u{1F680}-\u{1F6FF}]|[\u{1F1E0}-\u{1F1FF}]|[\u{2600}-\u{26FF}]|[\u{2700}-\u{27BF}]/gu;
            if (emojiRegex.test(res.text)) {
              console.log('getEditorContent - 内容包含表情符号，正常保存');
            }
          }
          resolve(res)
        },
        fail: (err) => {
          reject(err)
        }
      })
    })
  }

  // 设置编辑器内容
  const setEditorContent = (content) => {
    return new Promise((resolve, reject) => {
      if (!editorCtx.value) {
        reject(new Error('编辑器实例不存在'))
        return
      }

      const contentOptions = typeof content === 'string' 
        ? { html: content }
        : content

      editorCtx.value.setContents({
        ...contentOptions,
        success: (res) => {
          resolve(res)
        },
        fail: (err) => {
          reject(err)
        }
      })
    })
  }

  // 清空编辑器内容
  const clearEditor = () => {
    return setEditorContent({ html: '' })
  }

  // 在编辑器中插入内容
  const insertContent = (content) => {
    return new Promise((resolve, reject) => {
      if (!editorCtx.value) {
        reject(new Error('编辑器实例不存在'))
        return
      }

      editorCtx.value.insertText({
        text: content,
        success: (res) => {
          resolve(res)
        },
        fail: (err) => {
          reject(err)
        }
      })
    })
  }

  return {
    isEditing,
    currentDiary,
    editorCtx,
    onEditorSelectionChange,
    onEditorInput,
    onEditorFocus,
    onEditorBlur,
    onEditorReady,
    getEditorContent,
    setEditorContent,
    clearEditor,
    insertContent,
    setEditorContext,
    initEditor
  }
}