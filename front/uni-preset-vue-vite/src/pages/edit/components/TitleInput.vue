<template>
  <view class="input-card">
    <input
      class="title-input"
      type="text"
      placeholder="给这一天起个标题吧~"
      :value="title"
      @input="onTitleInput"
      maxlength="50"
    />
  </view>
</template>

<script>
export default {
  name: 'TitleInput',
  props: {
    title: {
      type: String,
      default: ''
    }
  },
  emits: ['update:title'],
  methods: {
    onTitleInput(e) {
      this.$emit('update:title', e.detail.value)
    }
  }
}
</script>

<style scoped>
/* 标题输入 */
.input-card {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 24rpx 32rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(255, 154, 118, 0.1);
}

.title-input {
  width: 100%;
  font-size: 36rpx;
  color: #333333;
  font-weight: bold;
}
</style>