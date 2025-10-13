<template>
  <view class="editor-card">
    <editor
      id="editor"
      class="editor-content"
      placeholder="记录此刻的心情和故事..."
      :show-img-size="false"
      :show-img-toolbar="false"
      :show-img-resize="false"
      @ready="onEditorReady"
      @input="onEditorInput"
      @selectionchange="onEditorSelectionChange"
      @statuschange="onEditorStatusChange"
    />
    <view class="char-count">{{ contentLength }}/5000</view>
  </view>
</template>

<script>
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
      this.$emit('editor-input', e)
    },
    onEditorSelectionChange(e) {
      this.$emit('editor-selection-change', e)
    },
    onEditorStatusChange(e) {
      this.$emit('editor-status-change', e)
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
  min-height: 400rpx;
}

.editor-content {
  width: 100%;
  min-height: 300rpx;
  font-size: 32rpx;
  color: #666666;
  line-height: 1.8;
  margin-bottom: 16rpx;
}

.char-count {
  font-size: 24rpx;
  color: #cccccc;
  text-align: right;
}
</style>