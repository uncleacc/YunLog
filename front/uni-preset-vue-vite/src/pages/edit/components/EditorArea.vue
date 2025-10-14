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
      if (e.detail && e.detail.text) {
        if (hasEmoji(e.detail.text)) {
          const emojiCount = countEmoji(e.detail.text);
          console.log('EditorArea - 检测到表情符号输入:', {
            emojiCount,
            textLength: e.detail.text.length,
            sample: e.detail.text.substring(0, 50)
          });
        }
      }
      
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
      console.log('EditorArea - 编辑器被直接点击，将光标移至末尾')
      this.moveCursorToEnd()
    },
    
    // 处理卡片点击事件（点击编辑器外围区域）
    handleCardClick() {
      console.log('EditorArea - 卡片外围被点击，聚焦编辑器并将光标移至末尾')
      
      if (this.editorCtx) {
        // 聚焦编辑器
        this.editorCtx.focus()
        // 将光标移至末尾
        this.moveCursorToEnd()
      } else {
        console.log('EditorArea - 编辑器上下文尚未准备好')
      }
    },
    
    // 将光标移动到内容末尾的通用方法
    moveCursorToEnd() {
      if (!this.editorCtx) return
      
      // 延迟一小段时间确保编辑器已聚焦
      setTimeout(() => {
        // 获取编辑器内容
        this.editorCtx.getContents({
          success: (res) => {
            console.log('EditorArea - 获取编辑器内容成功', res)
            
            // 如果有内容，使用简单的HTML标签替换来估算文本长度
            if (res && res.html) {
              // 移除HTML标签并计算纯文本长度
              const textContent = res.html.replace(/<[^>]*>/g, '').replace(/&nbsp;/g, ' ')
              const textLength = textContent.length
              
              console.log('EditorArea - 内容长度:', textLength, '移动光标到末尾')
              this.editorCtx.setSelectionRange(textLength, textLength)
            } else {
              // 没有内容时光标在开头
              console.log('EditorArea - 无内容，光标设置到开头')
              this.editorCtx.setSelectionRange(0, 0)
            }
          },
          fail: (err) => {
            console.log('EditorArea - 获取编辑器内容失败，使用备用方案', err)
            // 如果获取失败，直接尝试移到一个较大的位置，编辑器会自动调整到实际末尾
            this.editorCtx.setSelectionRange(9999, 9999)
          }
        })
      }, 100)
    },
    
    // 获取编辑器上下文
    getEditorContext() {
      console.log('EditorArea - 开始获取编辑器上下文')
      
      uni.createSelectorQuery()
        .in(this)
        .select('#editor')
        .context((res) => {
          if (res && res.context) {
            this.editorCtx = res.context
            console.log('EditorArea - 成功获取编辑器上下文')
            this.$emit('editor-context-ready', res.context)
          } else {
            console.error('EditorArea - 获取编辑器上下文失败')
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