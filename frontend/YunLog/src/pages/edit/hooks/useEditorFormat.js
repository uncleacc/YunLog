// 编辑器格式管理 Hook
import { ref } from 'vue'

export function useEditorFormat() {
  // 格式状态
  const formatStates = ref({
    bold: false,
    italic: false,
    underline: false,
    strikethrough: false,
    listOrdered: false,
    listBullet: false
  })

  // 全局格式状态
  const globalFormatStates = ref({
    bold: false,
    italic: false,
    underline: false,
    strikethrough: false,
    listOrdered: false,
    listBullet: false
  })

  // 格式映射
  const formatMap = {
    bold: 'bold',
    italic: 'italic',
    underline: 'underline',
    strikethrough: 'strikeThrough',
  }

  // 根据当前格式状态更新工具栏按钮
  const updateToolbarFromSelection = (currentFormats, hasSelection = false) => {
    // 更新按钮状态以反映当前位置的实际格式
    formatStates.value.bold = !!currentFormats.bold
    formatStates.value.italic = !!currentFormats.italic  
    formatStates.value.underline = !!currentFormats.underline
    formatStates.value.strikethrough = !!currentFormats.strikeThrough
    formatStates.value.listOrdered = currentFormats.list === 'ordered'
    formatStates.value.listBullet = currentFormats.list === 'bullet'
    
    if (!hasSelection) {
      // 无选中文本：按钮状态反映当前光标位置格式，同时这就是全局格式状态
      globalFormatStates.value = { ...formatStates.value }
    }
  }

  // 智能同步全局格式状态与当前光标位置
  const syncGlobalFormatsWithCursor = (editorCtx, currentFormats) => {
    if (!editorCtx) return
    
    // 只在有激活的全局格式且当前位置没有对应格式时才应用
    // 这样可以避免过度干预编辑器的自然状态
    Object.keys(globalFormatStates.value).forEach(format => {
      const isGlobalActive = globalFormatStates.value[format]
      const isCursorActive = currentFormats[format] || false
      
      if (isGlobalActive && !isCursorActive) {
        // 全局格式激活，但当前位置没有该格式 -> 应用格式
        if (editorCtx.format && typeof editorCtx.format === 'function') {
          editorCtx.format(formatMap[format])
        }
        
        // 应用格式后更新工具栏状态
        formatStates.value[format] = true
      }
      // 注意：我们不主动移除当前位置的格式，让用户手动控制
    })
  }

  // 切换格式
  const toggleFormat = (editorCtx, format) => {
    if (!editorCtx) return

    // 检查是否有选中文本
    if (editorCtx.getSelectionText && typeof editorCtx.getSelectionText === 'function') {
      editorCtx.getSelectionText({
        success: (res) => {
          if (res.text && res.text.length > 0) {
            // 有选中文本，直接使用编辑器的 format 方法
            if (editorCtx.format && typeof editorCtx.format === 'function') {
              editorCtx.format(formatMap[format])
            }
            
            // 格式应用后，工具栏状态会通过 selectionchange 事件自动更新
          } else {
            // 没有选中文本，切换全局格式状态
            globalFormatStates.value[format] = !globalFormatStates.value[format]
            formatStates.value[format] = globalFormatStates.value[format]
            
            // 应用或取消格式
            if (editorCtx.format && typeof editorCtx.format === 'function') {
              editorCtx.format(formatMap[format])
            }
          }
        },
        fail: () => {
          // 获取选中文本失败，按无选中文本处理（切换全局状态）
          globalFormatStates.value[format] = !globalFormatStates.value[format]
          formatStates.value[format] = globalFormatStates.value[format]
          if (editorCtx.format && typeof editorCtx.format === 'function') {
            editorCtx.format(formatMap[format])
          }
        }
      })
    } else {
      // 降级处理：切换全局状态
      globalFormatStates.value[format] = !globalFormatStates.value[format]
      formatStates.value[format] = globalFormatStates.value[format]
    }
  }

  // 切换列表格式
  const toggleList = (editorCtx, listType) => {
    if (!editorCtx) return
    
    // 直接使用编辑器的 format 方法
    if (editorCtx.format && typeof editorCtx.format === 'function') {
      editorCtx.format('list', listType)
    }
  }

  // 检查是否有激活的全局格式状态
  const hasActiveGlobalFormats = () => {
    return Object.values(globalFormatStates.value).some(state => state === true)
  }

  return {
    formatStates,
    globalFormatStates,
    updateToolbarFromSelection,
    syncGlobalFormatsWithCursor,
    toggleFormat,
    toggleList,
    hasActiveGlobalFormats
  }
}