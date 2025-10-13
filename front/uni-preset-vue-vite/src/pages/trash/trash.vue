<template>
  <view class="container">
    <!-- 提示信息 -->
    <view class="tips-card">
      <text class="tips-icon">🗑️</text>
      <text class="tips-text">回收站中的日记将在30天后自动清除</text>
    </view>

    <!-- 日记列表 -->
    <view class="diary-list" v-if="trashList.length > 0">
      <view class="diary-item" v-for="item in trashList" :key="item.id">
        <!-- 附件缩略图 -->
        <view class="diary-attachments" v-if="item.attachments && item.attachments.length > 0">
          <image
            class="attachment-thumb"
            v-for="(att, index) in item.attachments.slice(0, 3)"
            :key="index"
            :src="att.url"
            mode="aspectFill"
            @click.stop="PreviewAttachment(att, index, item)"
          />
          <view class="attachment-more" v-if="item.attachments.length > 3">
            <text class="more-text">+{{ item.attachments.length - 3 }}</text>
          </view>
        </view>
        <view class="diary-header">
          <view class="diary-date">{{ FormatDate(item.createTime) }}</view>
          <view class="delete-info">
            <text class="delete-text">{{ GetDaysLeft(item.deletedTime) }}天后删除</text>
          </view>
        </view>
        <view class="diary-title">{{ item.title }}</view>
        <view class="diary-content-preview">{{ getPlainTextPreview(item) }}</view>
        <view class="diary-footer">
          <view class="action-button restore" @click="RestoreDiary(item.id)">
            <text class="button-icon">↩️</text>
            <text class="button-text">恢复</text>
          </view>
          <view class="action-button delete" @click="ConfirmPermanentDelete(item.id)">
            <text class="button-icon">🗑️</text>
            <text class="button-text">永久删除</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 空状态 -->
    <view class="empty-state" v-else>
      <text class="empty-icon">🎉</text>
      <text class="empty-text">回收站是空的</text>
      <text class="empty-hint">删除的日记会出现在这里</text>
    </view>

    <!-- 底部操作栏 -->
    <view class="bottom-bar" v-if="trashList.length > 0">
      <view class="action-button-large secondary" @click="GoBack">
        <text class="button-text">返回</text>
      </view>
      <view class="action-button-large danger" @click="ConfirmClearAll">
        <text class="button-text">清空回收站</text>
      </view>
    </view>
  </view>
</template>

<script>
import storage from '@/utils/storage.js'
import { getPlainTextPreview } from '@/utils/textUtils.js'

export default {
  data() {
    return {
      trashList: [],
    }
  },
  onShow() {
    this.LoadTrashList()
  },
  methods: {
    LoadTrashList() {
      this.trashList = storage.GetTrashList()
    },

    FormatDate(dateString) {
      const date = new Date(dateString)
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      return `${year}年${month}月${day}日`
    },

    GetDaysLeft(deletedTime) {
      const now = new Date()
      const deletedDate = new Date(deletedTime)
      const diffDays = Math.ceil((now - deletedDate) / (1000 * 60 * 60 * 24))
      return Math.max(0, 30 - diffDays)
    },

    // 获取纯文本预览（使用工具函数）
    getPlainTextPreview(item) {
      return getPlainTextPreview(item, 100)
    },

    RestoreDiary(id) {
      uni.showModal({
        title: '恢复日记',
        content: '确定要恢复这篇日记吗？',
        confirmColor: '#FF9A76',
        success: (res) => {
          if (res.confirm) {
            const success = storage.RestoreFromTrash(id)
            if (success) {
              uni.showToast({
                title: '恢复成功',
                icon: 'success',
              })
              this.LoadTrashList()
            } else {
              uni.showToast({
                title: '恢复失败',
                icon: 'none',
              })
            }
          }
        },
      })
    },

    ConfirmPermanentDelete(id) {
      uni.showModal({
        title: '永久删除',
        content: '确定要永久删除这篇日记吗？此操作不可恢复！',
        confirmText: '删除',
        confirmColor: '#FF6B6B',
        success: (res) => {
          if (res.confirm) {
            this.PermanentDelete(id)
          }
        },
      })
    },

    PermanentDelete(id) {
      const success = storage.PermanentDeleteDiary(id)
      if (success) {
        uni.showToast({
          title: '已永久删除',
          icon: 'success',
        })
        this.LoadTrashList()
      } else {
        uni.showToast({
          title: '删除失败',
          icon: 'none',
        })
      }
    },

    ConfirmClearAll() {
      uni.showModal({
        title: '清空回收站',
        content: '确定要清空回收站吗？所有日记将被永久删除，此操作不可恢复！',
        confirmText: '清空',
        confirmColor: '#FF6B6B',
        success: (res) => {
          if (res.confirm) {
            this.ClearAll()
          }
        },
      })
    },

    ClearAll() {
      const success = storage.ClearTrash()
      if (success) {
        uni.showToast({
          title: '已清空回收站',
          icon: 'success',
        })
        this.LoadTrashList()
      } else {
        uni.showToast({
          title: '操作失败',
          icon: 'none',
        })
      }
    },

    // 预览附件
    PreviewAttachment(attachment, index, item) {
      if (attachment.type === 'image') {
        // 图片预览
        uni.previewImage({
          urls: item.attachments.map(att => att.url),
          current: index
        })
      } else {
        // 其他类型附件（如视频）的处理
        uni.showToast({
          title: '暂不支持预览此类型文件',
          icon: 'none'
        })
      }
    },

    GoBack() {
      uni.navigateBack()
    },
  },
}
</script>

<style scoped>
.container {
  min-height: 100vh;
  background-color: #fff5f0;
  padding-bottom: 160rpx;
}

/* 提示卡片 */
.tips-card {
  background: linear-gradient(135deg, #ffd5a0 0%, #ffb76b 100%);
  margin: 32rpx;
  padding: 32rpx;
  border-radius: 24rpx;
  display: flex;
  align-items: center;
  gap: 16rpx;
  box-shadow: 0 4rpx 16rpx rgba(255, 183, 107, 0.2);
}

.tips-icon {
  font-size: 48rpx;
}

.tips-text {
  flex: 1;
  font-size: 28rpx;
  color: #8b4513;
  line-height: 1.4;
}

/* 日记列表 */
.diary-list {
  padding: 0 32rpx;
}

.diary-item {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 32rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(255, 154, 118, 0.1);
}

/* 附件缩略图 */
.diary-attachments {
  display: flex;
  gap: 8rpx;
  margin-bottom: 16rpx;
  overflow: hidden;
}

.attachment-thumb {
  width: 120rpx;
  height: 120rpx;
  border-radius: 12rpx;
  flex-shrink: 0;
}

.attachment-more {
  width: 120rpx;
  height: 120rpx;
  border-radius: 12rpx;
  background: rgba(255, 154, 118, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.more-text {
  font-size: 28rpx;
  color: #ff9a76;
  font-weight: 500;
}

.diary-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.diary-date {
  font-size: 24rpx;
  color: #999999;
}

.delete-info {
  background: #fff5f0;
  padding: 8rpx 16rpx;
  border-radius: 12rpx;
}

.delete-text {
  font-size: 24rpx;
  color: #ff9a76;
}

.diary-title {
  font-size: 36rpx;
  font-weight: bold;
  color: #333333;
  margin-bottom: 16rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.diary-content-preview {
  font-size: 28rpx;
  color: #666666;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 3;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 20rpx;
}

.diary-footer {
  display: flex;
  gap: 16rpx;
  padding-top: 20rpx;
  border-top: 1rpx solid #f5f5f5;
}

.action-button {
  flex: 1;
  height: 72rpx;
  border-radius: 36rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  transition: all 0.3s ease;
}

.action-button.restore {
  background: linear-gradient(135deg, #4caf50 0%, #45a049 100%);
  box-shadow: 0 4rpx 12rpx rgba(76, 175, 80, 0.3);
}

.action-button.delete {
  background: #ffffff;
  border: 2rpx solid #ff6b6b;
}

.action-button.restore .button-icon,
.action-button.restore .button-text {
  color: #ffffff;
}

.action-button.delete .button-icon,
.action-button.delete .button-text {
  color: #ff6b6b;
}

.action-button:active {
  transform: scale(0.98);
  opacity: 0.8;
}

.button-icon {
  font-size: 28rpx;
}

.button-text {
  font-size: 28rpx;
  font-weight: 500;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 200rpx 0;
}

.empty-icon {
  font-size: 120rpx;
  margin-bottom: 32rpx;
  opacity: 0.8;
}

.empty-text {
  font-size: 32rpx;
  color: #999999;
  margin-bottom: 16rpx;
}

.empty-hint {
  font-size: 28rpx;
  color: #cccccc;
}

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

.action-button-large {
  flex: 1;
  height: 88rpx;
  border-radius: 44rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  font-size: 32rpx;
}

.action-button-large.secondary {
  background: #ffffff;
  border: 2rpx solid #ff9a76;
}

.action-button-large.secondary .button-text {
  color: #ff9a76;
  font-weight: 500;
}

.action-button-large.danger {
  background: #ff6b6b;
  box-shadow: 0 4rpx 16rpx rgba(255, 107, 107, 0.3);
}

.action-button-large.danger .button-text {
  color: #ffffff;
  font-weight: 500;
}

.action-button-large:active {
  transform: scale(0.98);
  opacity: 0.8;
}
</style>

