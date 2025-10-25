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
    return editorCtx.value
  }

  // 处理编辑器选择变化
  const onEditorSelectionChange = (e, { updateToolbarFromSelection, syncGlobalFormatsWithCursor, hasActiveGlobalFormats }) => {
    if (!editorCtx.value) return
    
    // 直接从事件中获取格式状态，这通常是最准确的
    const eventFormats = e.detail || {}
    
    // 使用 nextTick 确保选择变化完成
    nextTick(() => {
      // 获取当前选中文本信息
      if (editorCtx.value.getSelectionText && typeof editorCtx.value.getSelectionText === 'function') {
        editorCtx.value.getSelectionText({
          success: (selectionRes) => {
            const hasSelection = selectionRes.text && selectionRes.text.length > 0
            
            // 更新工具栏状态
            updateToolbarFromSelection(eventFormats, hasSelection)
            
            // 如果没有选中文本且有激活的全局格式，同步到当前位置
            if (!hasSelection && hasActiveGlobalFormats()) {
              syncGlobalFormatsWithCursor(editorCtx.value, eventFormats)
            }
          },
          fail: (err) => {
            // 降级处理：假设没有选中文本
            updateToolbarFromSelection(eventFormats, false)
          }
        })
      } else {
        // 降级处理：假设没有选中文本
        updateToolbarFromSelection(eventFormats, false)
      }
    })
  }

  // 处理编辑器输入
  const onEditorInput = (e) => {
    // 这里可以添加其他输入处理逻辑，如自动保存等
  }

  // 处理编辑器聚焦
  const onEditorFocus = (e) => {
  }

  // 处理编辑器失焦
  const onEditorBlur = (e) => {
  }

  // 处理编辑器就绪
  const onEditorReady = (e) => {
    initEditor()
  }

  // 设置编辑器上下文
  const setEditorContext = (context) => {
    editorCtx.value = context
  }

  // 获取编辑器内容
  const getEditorContent = () => {
    return new Promise((resolve, reject) => {
      if (!editorCtx.value) {
        reject(new Error('编辑器实例不存在'))
        return
      }

      if (editorCtx.value.getContents && typeof editorCtx.value.getContents === 'function') {
        editorCtx.value.getContents({
          success: (res) => {
            // 确保表情符号在内容中正确保存
            resolve(res)
          },
          fail: (err) => {
            reject(err)
          }
        })
      } else {
        reject(new Error('getContents 方法不可用'))
      }
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

      if (editorCtx.value.setContents && typeof editorCtx.value.setContents === 'function') {
        editorCtx.value.setContents({
          ...contentOptions,
          success: (res) => {
            resolve(res)
          },
          fail: (err) => {
            reject(err)
          }
        })
      } else {
        reject(new Error('setContents 方法不可用'))
      }
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

      if (editorCtx.value.insertText && typeof editorCtx.value.insertText === 'function') {
        editorCtx.value.insertText({
          text: content,
          success: (res) => {
            resolve(res)
          },
          fail: (err) => {
            reject(err)
          }
        })
      } else {
        reject(new Error('insertText 方法不可用'))
      }
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