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
    console.log('UpdateToolbarFromSelection - 更新工具栏按钮状态:', currentFormats, '有选中文本:', hasSelection)
    
    // 更新按钮状态以反映当前位置的实际格式
    formatStates.value.bold = !!currentFormats.bold
    formatStates.value.italic = !!currentFormats.italic  
    formatStates.value.underline = !!currentFormats.underline
    formatStates.value.strikethrough = !!currentFormats.strikeThrough
    formatStates.value.listOrdered = currentFormats.list === 'ordered'
    formatStates.value.listBullet = currentFormats.list === 'bullet'
    
    console.log('UpdateToolbarFromSelection - 按钮状态已更新:', formatStates.value)
    
    if (!hasSelection) {
      // 无选中文本：按钮状态反映当前光标位置格式，同时这就是全局格式状态
      globalFormatStates.value = { ...formatStates.value }
      console.log('UpdateToolbarFromSelection - 无选中文本，更新全局格式状态:', globalFormatStates.value)
    } else {
      // 有选中文本：按钮状态临时反映选中内容格式，不改变全局状态
      console.log('UpdateToolbarFromSelection - 有选中文本，按钮状态临时反映选中内容格式，全局状态保持:', globalFormatStates.value)
    }
  }

  // 智能同步全局格式状态与当前光标位置
  const syncGlobalFormatsWithCursor = (editorCtx, currentFormats) => {
    if (!editorCtx) return
    
    console.log('SyncGlobalFormatsWithCursor - 同步全局格式与光标位置')
    console.log('- 全局状态:', globalFormatStates.value)
    console.log('- 光标位置状态:', currentFormats)
    
    // 只在有激活的全局格式且当前位置没有对应格式时才应用
    // 这样可以避免过度干预编辑器的自然状态
    Object.keys(globalFormatStates.value).forEach(format => {
      const isGlobalActive = globalFormatStates.value[format]
      const isCursorActive = currentFormats[format] || false
      
      if (isGlobalActive && !isCursorActive) {
        // 全局格式激活，但当前位置没有该格式 -> 应用格式
        console.log(`SyncGlobalFormatsWithCursor - 应用全局 ${format} 格式到当前位置`)
        editorCtx.format(formatMap[format])
        
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
    editorCtx.getSelectionText({
      success: (res) => {
        if (res.text && res.text.length > 0) {
          // 有选中文本，直接使用编辑器的 format 方法
          console.log(`ToggleFormat - 对选中文本应用 ${format} 格式`)
          editorCtx.format(formatMap[format])
          
          // 格式应用后，工具栏状态会通过 selectionchange 事件自动更新
        } else {
          // 没有选中文本，切换全局格式状态
          globalFormatStates.value[format] = !globalFormatStates.value[format]
          formatStates.value[format] = globalFormatStates.value[format]
          
          // 应用或取消格式
          editorCtx.format(formatMap[format])
          
          if (globalFormatStates.value[format]) {
            console.log(`ToggleFormat - 激活全局 ${format} 格式，后续输入将应用此格式`)
          } else {
            console.log(`ToggleFormat - 取消全局 ${format} 格式`)
          }
        }
      },
      fail: () => {
        // 获取选中文本失败，按无选中文本处理（切换全局状态）
        globalFormatStates.value[format] = !globalFormatStates.value[format]
        formatStates.value[format] = globalFormatStates.value[format]
        editorCtx.format(formatMap[format])
        console.log(`ToggleFormat - 获取选中文本失败，切换全局 ${format} 格式状态:`, globalFormatStates.value[format])
      }
    })
  }

  // 切换列表格式
  const toggleList = (editorCtx, listType) => {
    if (!editorCtx) return

    console.log('ToggleList - 切换列表格式:', listType)
    
    // 直接使用编辑器的 format 方法
    editorCtx.format('list', listType)
    
    console.log(`ToggleList - 已调用编辑器列表格式方法: list, ${listType}`)
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