<template>
  <view class="toolbar-card">
    <view class="toolbar-row">
      <view 
        class="tool-btn" 
        :class="{ 'tool-btn-active': formatStates.bold }"
        @click="onToggleFormat('bold')"
      >
        <text class="tool-text" style="font-weight: bold">B</text>
      </view>
      <view 
        class="tool-btn" 
        :class="{ 'tool-btn-active': formatStates.italic }"
        @click="onToggleFormat('italic')"
      >
        <text class="tool-text" style="font-style: italic">I</text>
      </view>
      <view 
        class="tool-btn" 
        :class="{ 'tool-btn-active': formatStates.underline }"
        @click="onToggleFormat('underline')"
      >
        <text class="tool-text" style="text-decoration: underline">U</text>
      </view>
      <view 
        class="tool-btn" 
        :class="{ 'tool-btn-active': formatStates.strikethrough }"
        @click="onToggleFormat('strikethrough')"
      >
        <text class="tool-text" style="text-decoration: line-through">S</text>
      </view>
      <view 
        class="tool-btn" 
        :class="{ 'tool-btn-active': formatStates.listOrdered }"
        @click="onToggleList('ordered')"
      >
        <text class="tool-text">1.</text>
      </view>
      <view 
        class="tool-btn" 
        :class="{ 'tool-btn-active': formatStates.listBullet }"
        @click="onToggleList('bullet')"
      >
        <text class="tool-text">•</text>
      </view>
      <view class="tool-btn" @click="onToggleAttachmentBar">
        <text class="tool-icon">📎</text>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  name: 'EditorToolbar',
  props: {
    formatStates: {
      type: Object,
      default: () => ({
        bold: false,
        italic: false,
        underline: false,
        strikethrough: false,
        listOrdered: false,
        listBullet: false
      })
    }
  },
  emits: ['toggle-format', 'toggle-list', 'toggle-attachment-bar'],
  watch: {
    formatStates: {
      handler(newStates, oldStates) {
        console.log('EditorToolbar - 格式状态变化:', {
          old: oldStates,
          new: newStates
        })
      },
      deep: true
    }
  },
  methods: {
    onToggleFormat(format) {
      console.log('EditorToolbar - 点击格式按钮:', format)
      this.$emit('toggle-format', format)
    },
    onToggleList(listType) {
      console.log('EditorToolbar - 点击列表按钮:', listType)
      this.$emit('toggle-list', listType)
    },
    onToggleAttachmentBar() {
      this.$emit('toggle-attachment-bar')
    }
  }
}
</script>

<style scoped>
/* 富文本工具栏 */
.toolbar-card {
  background: #ffffff;
  border-radius: 16rpx;
  padding: 16rpx;
  margin-bottom: 16rpx;
  box-shadow: 0 2rpx 8rpx rgba(255, 154, 118, 0.08);
}

.toolbar-row {
  display: flex;
  gap: 12rpx;
}

.tool-btn {
  width: 56rpx;
  height: 56rpx;
  border-radius: 8rpx;
  background: #fff5f0;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.tool-btn:active {
  transform: scale(0.95);
  background: #ffe5d8;
}

.tool-btn-active {
  background: #FF6B35 !important;
  transform: scale(1.1);
  box-shadow: 0 4rpx 12rpx rgba(255, 107, 53, 0.3);
}

.tool-btn-active .tool-text {
  color: #ffffff !important;
  font-weight: bold;
}

.tool-text {
  font-size: 26rpx;
  color: #333333;
}

.tool-icon {
  font-size: 32rpx;
}
</style>