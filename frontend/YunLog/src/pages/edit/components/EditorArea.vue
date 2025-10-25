<template>
    <view class="editor-card" @tap="handleCardClick">
    <editor
      id="editor"
      class="editor-content"
      :placeholder="placeholder"
      :show-img-size="true"
      :show-img-toolbar="true"
      :show-img-resize="true"
      :auto-focus="false"
      @ready="onEditorReady"
      @blur="onEditorBlur"
      @focus="onEditorFocus"
      @input="onEditorInput"
      @selectionchange="onEditorSelectionChange"
      @statuschange="onEditorStatusChange"
      @tap.stop="handleEditorClick"
    ></editor>
    <view class="char-count">字符数: {{textLength}}</view>
  </view>
</template>

<script>
import { hasEmoji, countEmoji } from '../../../utils/emojiUtils.js'

export default {
  name: 'EditorArea',
  props: {
    contentLength: {
      type: Number,
      default: 0
    }
  },
  emits: ['editor-ready', 'editor-input', 'editor-selection-change', 'editor-context-ready', 'editor-status-change'],
  data() {
    return {
      editorCtx: null
    }
  },
  methods: {
    onEditorReady(e) {
      // 编辑器就绪时，获取编辑器上下文
      this.getEditorContext()
      this.$emit('editor-ready', e)
    },
    onEditorInput(e) {
      // 检查输入内容中是否包含表情符号
      this.$emit('editor-input', e)
    },
    onEditorSelectionChange(e) {
      this.$emit('editor-selection-change', e)
    },
    onEditorStatusChange(e) {
      this.$emit('editor-status-change', e)
    },
    
    // 处理编辑器直接点击事件
    handleEditorClick() {
      this.moveCursorToEnd()
    },
    
    // 处理卡片点击事件（点击编辑器外围区域）
    handleCardClick() {
      if (this.editorCtx) {
        // 聚焦编辑器
        if (this.editorCtx.focus && typeof this.editorCtx.focus === 'function') {
          this.editorCtx.focus()
        }
        // 将光标移至末尾
        this.moveCursorToEnd()
      }
    },
    
    // 将光标移动到内容末尾的通用方法
    moveCursorToEnd() {
      if (!this.editorCtx) return
      
      // 延迟一小段时间确保编辑器已聚焦
      setTimeout(() => {
        // 获取编辑器内容
        if (this.editorCtx.getContents && typeof this.editorCtx.getContents === 'function') {
          this.editorCtx.getContents({
            success: (res) => {
              // 如果有内容，使用简单的HTML标签替换来估算文本长度
              if (res && res.html) {
                // 移除HTML标签并计算纯文本长度
                const textContent = res.html.replace(/<[^>]*>/g, '').replace(/&nbsp;/g, ' ')
                const textLength = textContent.length
                
                if (this.editorCtx.setSelectionRange && typeof this.editorCtx.setSelectionRange === 'function') {
                  this.editorCtx.setSelectionRange(textLength, textLength)
                }
              } else {
                // 没有内容时光标在开头
                if (this.editorCtx.setSelectionRange && typeof this.editorCtx.setSelectionRange === 'function') {
                  this.editorCtx.setSelectionRange(0, 0)
                }
              }
            },
            fail: (err) => {
              // 如果获取失败，直接尝试移到一个较大的位置，编辑器会自动调整到实际末尾
              if (this.editorCtx.setSelectionRange && typeof this.editorCtx.setSelectionRange === 'function') {
                this.editorCtx.setSelectionRange(9999, 9999)
              }
            }
          })
        }
      }, 100)
    },
    
    // 获取编辑器上下文
    getEditorContext() {
      uni.createSelectorQuery()
        .in(this)
        .select('#editor')
        .context((res) => {
          if (res && res.context) {
            this.editorCtx = res.context
            this.$emit('editor-context-ready', res.context)
          }
        })
        .exec()
    },
    
    // 暴露编辑器上下文给父组件
    getEditorInstance() {
      return this.editorCtx
    }
  }
}
</script>

<style scoped>
/* 富文本编辑器 */
.editor-card {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 32rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(255, 154, 118, 0.1);
  min-height: 600rpx;
  flex: 1;
}

.editor-content {
  width: 100%;
  min-height: 500rpx;
  font-size: 32rpx;
  color: #666666;
  line-height: 1.8;
  margin-bottom: 16rpx;
  /* 确保表情符号正确显示 */
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", "Roboto", "Helvetica Neue", Arial, "Noto Color Emoji", "Apple Color Emoji", "Segoe UI Emoji", "Segoe UI Symbol", sans-serif;
  word-wrap: break-word;
  word-break: break-all;
}

.char-count {
  font-size: 24rpx;
  color: #cccccc;
  text-align: right;
}
</style>