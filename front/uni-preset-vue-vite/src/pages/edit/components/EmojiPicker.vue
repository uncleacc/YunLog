<template>
  <view class="emoji-picker" v-if="visible">
    <view class="emoji-header">
      <text class="emoji-title">选择表情</text>
      <view class="emoji-close" @click="onClose">
        <text class="close-icon">×</text>
      </view>
    </view>
    <scroll-view class="emoji-list" scroll-y>
      <view
        class="emoji-item"
        v-for="emoji in emojiList"
        :key="emoji"
        @click="onInsertEmoji(emoji)"
      >
        <text class="emoji-char">{{ emoji }}</text>
      </view>
    </scroll-view>
  </view>
</template>

<script>
export default {
  name: 'EmojiPicker',
  props: {
    visible: {
      type: Boolean,
      default: false
    }
  },
  emits: ['close', 'insert-emoji'],
  data() {
    return {
      emojiList: [
        '😀', '😃', '😄', '😁', '😆', '😅', '🤣', '😂', '🙂', '🙃',
        '😉', '😊', '😇', '🥰', '😍', '🤩', '😘', '😗', '😚', '😙',
        '😋', '😛', '😜', '🤪', '😝', '🤑', '🤗', '🤭', '🤫', '🤔',
        '🤐', '🤨', '😐', '😑', '😶', '😏', '😒', '🙄', '😬', '🤥',
        '😌', '😔', '😪', '🤤', '😴', '😷', '🤒', '🤕', '🤢', '🤮',
        '🤧', '🥵', '🥶', '🥴', '😵', '🤯', '🤠', '🥳', '😎', '🤓',
        '🧐', '😕', '😟', '🙁', '☹️', '😮', '😯', '😲', '😳', '🥺',
        '😦', '😧', '😨', '😰', '😥', '😢', '😭', '😱', '😖', '😣',
        '😞', '😓', '😩', '😫', '🥱', '😤', '😡', '😠', '🤬', '😈',
        '👿', '💀', '☠️', '💩', '🤡', '👹', '👺', '👻', '👽', '👾',
        '❤️', '🧡', '💛', '💚', '💙', '💜', '🖤', '🤍', '🤎', '💔',
        '❣️', '💕', '💞', '💓', '💗', '💖', '💘', '💝', '💟', '☮️',
        '✨', '⭐', '🌟', '💫', '⚡', '🔥', '💥', '☀️', '🌙', '⭐',
        '🎉', '🎊', '🎈', '🎁', '🏆', '🥇', '🥈', '🥉', '⚽', '🏀',
        '👍', '👎', '👏', '🙌', '👋', '🤝', '✊', '👊', '🤛', '🤜',
        '✌️', '🤞', '🤟', '🤘', '👌', '🤌', '🤏', '👈', '👉', '👆',
      ]
    }
  },
  methods: {
    onClose() {
      this.$emit('close')
    },
    onInsertEmoji(emoji) {
      this.$emit('insert-emoji', emoji)
    }
  }
}
</script>

<style scoped>
/* 表情选择器 */
.emoji-picker {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 24rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(255, 154, 118, 0.1);
  max-height: 500rpx;
}

.emoji-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.emoji-title {
  font-size: 28rpx;
  color: #666666;
  font-weight: 500;
}

.emoji-close {
  width: 48rpx;
  height: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff5f0;
  border-radius: 50%;
}

.close-icon {
  font-size: 36rpx;
  color: #ff9a76;
  font-weight: bold;
}

.emoji-list {
  height: 400rpx;
}

.emoji-item {
  display: inline-block;
  width: 80rpx;
  height: 80rpx;
  text-align: center;
  line-height: 80rpx;
  border-radius: 12rpx;
  transition: all 0.3s ease;
}

.emoji-item:active {
  background: #fff5f0;
  transform: scale(1.2);
}

.emoji-char {
  font-size: 48rpx;
}
</style>