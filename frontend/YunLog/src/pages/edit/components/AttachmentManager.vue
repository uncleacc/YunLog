<template>
  <view class="attachments-card" v-if="attachments.length > 0 || showAttachmentBar">
    <view class="attachments-label">附件</view>
    <view class="attachments-grid">
      <!-- 已上传的附件 -->
      <view
        class="attachment-item"
        v-for="(item, index) in attachments"
        :key="index"
        @longpress="onShowAttachmentMenu(index)"
      >
        <template v-if="item && item.url">
          <image
            v-if="isImageFile(item.url)"
            class="attachment-preview"
            :src="item.url"
            mode="aspectFill"
            @click.stop="onPreviewImage(item.url, index)"
          />
          <view v-else class="attachment-video">
            <text class="video-icon">🎬</text>
            <text class="video-text">视频</text>
          </view>
          
          <!-- 上传状态遮罩 -->
          <view v-if="item.uploading" class="attachment-uploading">
            <view class="uploading-spinner"></view>
            <text class="uploading-text">上传中</text>
          </view>
        </template>
        <view class="attachment-delete" @click.stop="onRemoveAttachment(index)" v-if="!item.uploading">
          <text class="delete-icon">×</text>
        </view>
      </view>
      <!-- 添加附件按钮 -->
      <view class="attachment-add" @click="onAddAttachment">
        <text class="add-icon">+</text>
        <text class="add-text">添加</text>
      </view>
    </view>
  </view>
</template>

<script>
import { isImageFile, isVideoFile } from '@/utils/textUtils.js'

export default {
  name: 'AttachmentManager',
  props: {
    attachments: {
      type: Array,
      default: () => []
    },
    showAttachmentBar: {
      type: Boolean,
      default: false
    }
  },
  emits: ['add-attachment', 'remove-attachment', 'preview-image', 'show-attachment-menu'],
  methods: {
    isImageFile,
    isVideoFile,
    onAddAttachment() {
      this.$emit('add-attachment')
    },
    onRemoveAttachment(index) {
      this.$emit('remove-attachment', index)
    },
    onPreviewImage(url, index) {
      this.$emit('preview-image', url, index)
    },
    onShowAttachmentMenu(index) {
      this.$emit('show-attachment-menu', index)
    }
  }
}
</script>

<style scoped>
/* 附件区域 */
.attachments-card {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 32rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(255, 154, 118, 0.1);
}

.attachments-label {
  font-size: 28rpx;
  color: #666666;
  margin-bottom: 20rpx;
  font-weight: 500;
}

.attachments-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}

.attachment-item {
  width: 200rpx;
  height: 200rpx;
  border-radius: 16rpx;
  overflow: hidden;
  position: relative;
  background: #f5f5f5;
}

.attachment-preview {
  width: 100%;
  height: 100%;
}

.attachment-video {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.video-icon {
  font-size: 64rpx;
  margin-bottom: 8rpx;
}

.video-text {
  font-size: 24rpx;
  color: #ffffff;
}

.attachment-delete {
  position: absolute;
  top: 8rpx;
  right: 8rpx;
  width: 48rpx;
  height: 48rpx;
  background: rgba(0, 0, 0, 0.5);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.delete-icon {
  font-size: 36rpx;
  color: #ffffff;
  font-weight: bold;
}

.attachment-add {
  width: 200rpx;
  height: 200rpx;
  border-radius: 16rpx;
  border: 2rpx dashed #ff9a76;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #fff5f0;
}

.add-icon {
  font-size: 48rpx;
  color: #ff9a76;
  margin-bottom: 8rpx;
}

.add-text {
  font-size: 24rpx;
  color: #ff9a76;
}

/* 上传状态遮罩 */
.attachment-uploading {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  border-radius: 16rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.uploading-spinner {
  width: 48rpx;
  height: 48rpx;
  border: 4rpx solid rgba(255, 255, 255, 0.3);
  border-top: 4rpx solid #ffffff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 12rpx;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.uploading-text {
  font-size: 24rpx;
  color: #ffffff;
  font-weight: 500;
}
</style>