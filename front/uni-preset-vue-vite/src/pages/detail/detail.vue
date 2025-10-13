<template>
  <view class="container">
    <scroll-view class="content" scroll-y v-if="diary">
      <!-- 顶部信息卡片 -->
      <view class="header-card">
        <view class="date-info">
          <text class="date-main">{{ FormatDate(diary.createTime) }}</text>
          <text class="date-time">{{ FormatTime(diary.createTime) }}</text>
        </view>
      </view>

      <!-- 附件展示 -->
      <view class="attachments-section" v-if="diary.attachments && diary.attachments.length > 0">
        <scroll-view class="attachments-scroll" scroll-x>
          <view class="attachments-list">
            <view
              class="attachment-item"
              v-for="(item, index) in diary.attachments"
              :key="index"
              @click="PreviewAttachment(item, index)"
            >
              <image
                v-if="item.type === 'image'"
                class="attachment-image"
                :src="item.url"
                mode="aspectFill"
              />
              <view v-else class="attachment-video-preview">
                <text class="video-icon">🎬</text>
                <text class="video-text">视频</text>
              </view>
            </view>
          </view>
        </scroll-view>
      </view>

      <!-- 标题 -->
      <view class="title-section">
        <text class="title">{{ diary.title }}</text>
      </view>

      <!-- 内容 -->
      <view class="content-section">
        <rich-text 
          v-if="diary.contentHtml" 
          class="content-richtext" 
          :nodes="diary.contentHtml"
        />
        <text v-else class="content-text">{{ diary.content }}</text>
      </view>

      <!-- 底部信息 -->
      <view class="footer-info">
        <text class="update-time" v-if="diary.updateTime !== diary.createTime">
          最后编辑：{{ FormatFullTime(diary.updateTime) }}
        </text>
      </view>
    </scroll-view>

    <!-- 底部操作栏 -->
    <view class="bottom-bar">
      <view class="action-button secondary" @click="GoBack">
        <text class="button-text">返回</text>
      </view>
      <view class="action-button primary" @click="EditDiary">
        <text class="button-text">编辑</text>
      </view>
    </view>
  </view>
</template>

<script>
import storage from '@/utils/storage.js'

export default {
  data() {
    return {
      diary: null,
      diaryId: '',
    }
  },
  onLoad(options) {
    if (options.id) {
      this.diaryId = options.id
      this.LoadDiary()
    }
  },
  onShow() {
    if (this.diaryId) {
      this.LoadDiary()
    }
  },
  methods: {
    LoadDiary() {
      this.diary = storage.GetDiaryById(this.diaryId)
      if (!this.diary) {
        uni.showToast({
          title: '日记不存在',
          icon: 'none',
        })
        setTimeout(() => {
          uni.navigateBack()
        }, 1500)
      }
    },

    FormatDate(dateString) {
      const date = new Date(dateString)
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
      const weekDay = weekDays[date.getDay()]
      return `${year}年${month}月${day}日 ${weekDay}`
    },

    FormatTime(dateString) {
      const date = new Date(dateString)
      const hours = String(date.getHours()).padStart(2, '0')
      const minutes = String(date.getMinutes()).padStart(2, '0')
      return `${hours}:${minutes}`
    },

    FormatFullTime(dateString) {
      const date = new Date(dateString)
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      const hours = String(date.getHours()).padStart(2, '0')
      const minutes = String(date.getMinutes()).padStart(2, '0')
      return `${year}-${month}-${day} ${hours}:${minutes}`
    },

    PreviewAttachment(item, index) {
      if (item.type === 'image') {
        // 预览图片
        const imageUrls = this.diary.attachments
          .filter((a) => a.type === 'image')
          .map((a) => a.url)
        const current = imageUrls.indexOf(item.url)
        uni.previewImage({
          urls: imageUrls,
          current: current >= 0 ? current : 0,
        })
      } else if (item.type === 'video') {
        // 播放视频
        uni.showToast({
          title: '视频播放功能待实现',
          icon: 'none',
        })
      }
    },

    GoBack() {
      uni.navigateBack()
    },

    EditDiary() {
      uni.redirectTo({
        url: `/pages/edit/edit?id=${this.diaryId}`,
      })
    },
  },
}
</script>

<style scoped>
.container {
  min-height: 100vh;
  background-color: #fff5f0;
  display: flex;
  flex-direction: column;
  width: 100%;
  box-sizing: border-box;
}

.content {
  flex: 1;
  padding: 24rpx;
  padding-bottom: 160rpx;
  width: 100%;
  box-sizing: border-box;
}

/* 顶部信息卡片 */
.header-card {
  background: linear-gradient(135deg, #ff9a76 0%, #ff7e5f 100%);
  border-radius: 24rpx;
  padding: 32rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 8rpx 24rpx rgba(255, 126, 95, 0.3);
  width: 100%;
  box-sizing: border-box;
}

.date-info {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.date-main {
  font-size: 32rpx;
  color: rgba(255, 255, 255, 0.95);
  margin-bottom: 8rpx;
  font-weight: 500;
}

.date-time {
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.8);
}

/* 附件展示区域 */
.attachments-section {
  margin-bottom: 24rpx;
  width: 100%;
  overflow: hidden;
  box-sizing: border-box;
}

.attachments-scroll {
  width: 100%;
  white-space: nowrap;
}

.attachments-list {
  display: inline-flex;
  gap: 12rpx;
  padding: 8rpx 0;
}

.attachment-item {
  display: inline-block;
  width: 200rpx;
  height: 200rpx;
  border-radius: 16rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 16rpx rgba(255, 154, 118, 0.2);
  flex-shrink: 0;
}

.attachment-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.attachment-video-preview {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.video-icon {
  font-size: 80rpx;
  margin-bottom: 12rpx;
}

.video-text {
  font-size: 28rpx;
  color: #ffffff;
}

/* 标题区域 */
.title-section {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 32rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(255, 154, 118, 0.1);
  width: 100%;
  box-sizing: border-box;
}

.title {
  font-size: 40rpx;
  font-weight: bold;
  color: #333333;
  line-height: 1.4;
  word-break: break-all;
  word-wrap: break-word;
  width: 100%;
}

/* 内容区域 */
.content-section {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 32rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(255, 154, 118, 0.1);
  min-height: 300rpx;
  width: 100%;
  box-sizing: border-box;
  overflow: hidden;
}

.content-text {
  font-size: 30rpx;
  color: #666666;
  line-height: 1.8;
  word-break: break-all;
  word-wrap: break-word;
  white-space: pre-wrap;
  width: 100%;
  overflow-wrap: break-word;
}

.content-richtext {
  font-size: 30rpx;
  color: #666666;
  line-height: 1.8;
  word-break: break-all;
  word-wrap: break-word;
  width: 100%;
  overflow-wrap: break-word;
}

/* 底部信息 */
.footer-info {
  padding: 24rpx 16rpx;
  text-align: center;
}

.update-time {
  font-size: 24rpx;
  color: #999999;
}

/* 底部操作栏 */
.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #ffffff;
  padding: 20rpx 24rpx;
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
  box-shadow: 0 -4rpx 16rpx rgba(0, 0, 0, 0.05);
  display: flex;
  gap: 20rpx;
  width: 100%;
  box-sizing: border-box;
}

.action-button {
  flex: 1;
  height: 80rpx;
  border-radius: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  font-size: 30rpx;
  min-width: 0;
  box-sizing: border-box;
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
  font-size: 30rpx;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
