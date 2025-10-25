<template>
  <view class="bottom-bar">
    <view class="action-button secondary" @click="onCancel">
      <text class="button-text">取消</text>
    </view>
    <view class="action-button primary" @click="onSave">
      <text class="button-text">保存</text>
    </view>
  </view>
</template>

<script>
export default {
  name: 'ActionBar',
  emits: ['cancel', 'save'],
  methods: {
    onCancel() {
      this.$emit('cancel')
    },
    onSave() {
      this.$emit('save')
    }
  }
}
</script>

<style scoped>
/* 底部操作栏 */
.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #ffffff;
  padding: 24rpx 32rpx;
  padding-bottom: calc(24rpx + env(safe-area-inset-bottom));
  box-shadow: 0 -4rpx 16rpx rgba(0, 0, 0, 0.05);
  display: flex;
  gap: 24rpx;
}

.action-button {
  flex: 1;
  height: 88rpx;
  border-radius: 44rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.action-button.primary {
  background: linear-gradient(135deg, #ff9a76 0%, #ff7e5f 100%);
  box-shadow: 0 4rpx 16rpx rgba(255, 126, 95, 0.3);
}

.action-button.secondary {
  background: #ffffff;
  border: 2rpx solid #ff9a76;
}

.action-button.primary .button-text {
  color: #ffffff;
  font-weight: 500;
}

.action-button.secondary .button-text {
  color: #ff9a76;
  font-weight: 500;
}

.action-button:active {
  transform: scale(0.98);
  opacity: 0.8;
}

.button-text {
  font-size: 32rpx;
}
</style>