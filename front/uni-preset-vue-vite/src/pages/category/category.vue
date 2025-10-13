<template>
  <view class="container">
    <!-- 分类信息卡片 -->
    <view class="category-header" v-if="category" :style="{ background: `linear-gradient(135deg, ${category.color}CC 0%, ${category.color}FF 100%)` }">
      <text class="category-icon-large">{{ category.icon }}</text>
      <text class="category-name-large">{{ category.name }}</text>
      <text class="category-stats">{{ diaryList.length }} 篇日记</text>
    </view>

    <!-- 日记列表 -->
    <view class="diary-list" v-if="diaryList.length > 0">
      <view
        class="diary-item"
        v-for="item in diaryList"
        :key="item.id"
        @click="ViewDiary(item.id)"
      >
        <!-- 附件缩略图 -->
        <view class="diary-attachments" v-if="item.attachments && item.attachments.length > 0">
          <image
            class="attachment-thumb"
            v-for="(att, index) in item.attachments.slice(0, 3)"
            :key="index"
            :src="att.url"
            mode="aspectFill"
          />
          <view class="attachment-more" v-if="item.attachments.length > 3">
            <text class="more-text">+{{ item.attachments.length - 3 }}</text>
          </view>
        </view>
        <view class="diary-header">
          <text class="diary-date">{{ FormatDate(item.createTime) }}</text>
        </view>
        <view class="diary-title">{{ item.title }}</view>
        <view class="diary-content-preview">{{ item.content }}</view>
        <view class="diary-footer">
          <text class="diary-time">{{ FormatTime(item.createTime) }}</text>
          <view class="diary-actions">
            <view class="action-btn" @click.stop="EditDiary(item.id)">
              <text class="action-icon">✏️</text>
            </view>
            <view class="action-btn" @click.stop="ConfirmDelete(item.id)">
              <text class="action-icon">🗑️</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 空状态 -->
    <view class="empty-state" v-else>
      <text class="empty-icon">📝</text>
      <text class="empty-text">还没有日记哦</text>
      <text class="empty-hint">点击下方按钮开始记录</text>
    </view>

    <!-- 添加按钮 -->
    <view class="add-btn" @click="AddNewDiary">
      <text class="add-icon">+</text>
    </view>
  </view>
</template>

<script>
import storage from '@/utils/storage.js'

export default {
  data() {
    return {
      categoryId: '',
      category: null,
      diaryList: [],
    }
  },
  onLoad(options) {
    if (options.id) {
      this.categoryId = options.id
      this.LoadData()
    }
  },
  onShow() {
    if (this.categoryId) {
      this.LoadData()
    }
  },
  methods: {
    LoadData() {
      this.category = storage.GetCategoryById(this.categoryId)
      this.diaryList = storage.GetDiaryListByCategory(this.categoryId)
      
      if (!this.category) {
        uni.showToast({
          title: '分类不存在',
          icon: 'none',
        })
        setTimeout(() => {
          uni.navigateBack()
        }, 1500)
      }
    },

    FormatDate(dateString) {
      const date = new Date(dateString)
      const month = date.getMonth() + 1
      const day = date.getDate()
      const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
      const weekDay = weekDays[date.getDay()]
      return `${month}月${day}日 ${weekDay}`
    },

    FormatTime(dateString) {
      const date = new Date(dateString)
      const hours = String(date.getHours()).padStart(2, '0')
      const minutes = String(date.getMinutes()).padStart(2, '0')
      return `${hours}:${minutes}`
    },

    AddNewDiary() {
      uni.navigateTo({
        url: `/pages/edit/edit?categoryId=${this.categoryId}`,
      })
    },

    ViewDiary(id) {
      uni.navigateTo({
        url: `/pages/detail/detail?id=${id}`,
      })
    },

    EditDiary(id) {
      uni.navigateTo({
        url: `/pages/edit/edit?id=${id}`,
      })
    },

    ConfirmDelete(id) {
      uni.showModal({
        title: '提示',
        content: '确定要删除这篇日记吗？',
        confirmColor: '#FF9A76',
        success: (res) => {
          if (res.confirm) {
            this.DeleteDiary(id)
          }
        },
      })
    },

    DeleteDiary(id) {
      const success = storage.DeleteDiary(id)
      if (success) {
        uni.showToast({
          title: '已移到回收站',
          icon: 'success',
        })
        this.LoadData()
      } else {
        uni.showToast({
          title: '删除失败',
          icon: 'none',
        })
      }
    },
  },
}
</script>

<style scoped>
.container {
  min-height: 100vh;
  padding-bottom: 160rpx;
  background-color: #fff5f0;
}

/* 分类信息卡片 */
.category-header {
  margin: 32rpx;
  padding: 48rpx 40rpx;
  border-radius: 32rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  box-shadow: 0 8rpx 24rpx rgba(255, 126, 95, 0.3);
}

.category-icon-large {
  font-size: 96rpx;
  margin-bottom: 16rpx;
}

.category-name-large {
  font-size: 44rpx;
  font-weight: bold;
  color: #ffffff;
  margin-bottom: 12rpx;
}

.category-stats {
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.9);
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
  transition: all 0.3s ease;
}

.diary-item:active {
  transform: scale(0.98);
  box-shadow: 0 2rpx 8rpx rgba(255, 154, 118, 0.15);
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
  justify-content: space-between;
  align-items: center;
  padding-top: 20rpx;
  border-top: 1rpx solid #f5f5f5;
}

.diary-time {
  font-size: 24rpx;
  color: #999999;
}

.diary-actions {
  display: flex;
  gap: 24rpx;
}

.action-btn {
  width: 60rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff5f0;
  border-radius: 50%;
  transition: all 0.3s ease;
}

.action-btn:active {
  transform: scale(0.9);
  background: #ffe5d8;
}

.action-icon {
  font-size: 32rpx;
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

/* 添加按钮 */
.add-btn {
  position: fixed;
  right: 48rpx;
  bottom: 48rpx;
  width: 120rpx;
  height: 120rpx;
  background: linear-gradient(135deg, #ff9a76 0%, #ff7e5f 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 24rpx rgba(255, 126, 95, 0.4);
  transition: all 0.3s ease;
}

.add-btn:active {
  transform: scale(0.95);
  box-shadow: 0 4rpx 16rpx rgba(255, 126, 95, 0.5);
}

.add-icon {
  font-size: 64rpx;
  color: #ffffff;
  font-weight: 300;
  line-height: 1;
}
</style>

