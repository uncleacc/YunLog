<template>
  <view class="container">
    <!-- 顶部统计卡片 -->
    <view class="header-card">
      <view class="header-content">
        <view class="stat-item">
          <text class="stat-number">{{ globalStats.totalCount }}</text>
          <text class="stat-label">篇日记</text>
        </view>
        <view class="divider"></view>
        <view class="stat-item">
          <text class="stat-number">{{ globalStats.continueDays }}</text>
          <text class="stat-label">天连续</text>
        </view>
      </view>
      <view class="welcome-text">记录生活，留住美好时光 ✨</view>
    </view>

    <!-- 分类列表 -->
    <view class="category-list" v-if="categoryList.length > 0">
      <view
        class="category-item"
        v-for="item in categoryList"
        :key="item.id"
        @click="GoToCategory(item.id)"
      >
        <view class="category-left">
          <view class="category-icon-wrapper" :style="{ backgroundColor: item.color }">
            <text class="category-icon">{{ item.icon }}</text>
          </view>
          <view class="category-info">
            <text class="category-name">{{ item.name }}</text>
            <text class="category-count">{{ GetCategoryCount(item.id) }} 篇日记</text>
          </view>
        </view>
        <view class="category-arrow">
          <text class="arrow-icon">›</text>
        </view>
      </view>
    </view>

    <!-- 底部按钮区 -->
    <view class="bottom-actions">
      <!-- 垃圾桶按钮 -->
      <view class="action-btn trash-btn" @click="GoToTrash">
        <text class="btn-icon">🗑️</text>
        <text class="btn-label">回收站</text>
      </view>

      <!-- 管理分类按钮 -->
      <view class="action-btn manage-btn" @click="ManageCategories">
        <text class="btn-icon">⚙️</text>
        <text class="btn-label">管理分类</text>
      </view>
    </view>
  </view>
</template>

<script>
import { getCategoryList, getCategoryStats, getGlobalStats } from '@/utils/api.js'

export default {
  data() {
    return {
      categoryList: [],
      globalStats: {
        totalCount: 0,
        continueDays: 0,
      },
      categoryStatsMap: {},
    }
  },
  onShow() {
    this.LoadData()
  },
  methods: {
    async LoadData() {
      try {
        // 加载分类列表
        this.categoryList = await getCategoryList()
        
        // 加载全局统计
        this.globalStats = await getGlobalStats()
        
        // 加载每个分类的统计信息
        this.categoryStatsMap = {}
        for (const category of this.categoryList) {
          const stats = await getCategoryStats(category.id)
          this.categoryStatsMap[category.id] = stats
        }
      } catch (error) {
        console.error('加载数据失败:', error)
        uni.showToast({
          title: '加载失败，请重试',
          icon: 'none'
        })
      }
    },

    GetCategoryCount(categoryId) {
      return this.categoryStatsMap[categoryId]?.totalCount || 0
    },

    GoToCategory(categoryId) {
      uni.navigateTo({
        url: `/pages/category/category?id=${categoryId}`,
      })
    },

    GoToTrash() {
      uni.navigateTo({
        url: '/pages/trash/trash',
      })
    },

    ManageCategories() {
      uni.navigateTo({
        url: '/pages/category-manage/category-manage',
      })
    },
  },
}
</script>

<style scoped>
.container {
  min-height: 100vh;
  padding-bottom: 200rpx;
  background-color: #fff5f0;
}

/* 顶部统计卡片 */
.header-card {
  background: linear-gradient(135deg, #ff9a76 0%, #ff7e5f 100%);
  margin: 32rpx;
  padding: 48rpx 40rpx;
  border-radius: 32rpx;
  box-shadow: 0 8rpx 24rpx rgba(255, 126, 95, 0.3);
}

.header-content {
  display: flex;
  justify-content: space-around;
  align-items: center;
  margin-bottom: 32rpx;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-number {
  font-size: 56rpx;
  font-weight: bold;
  color: #ffffff;
  line-height: 1.2;
}

.stat-label {
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.9);
  margin-top: 8rpx;
}

.divider {
  width: 2rpx;
  height: 80rpx;
  background-color: rgba(255, 255, 255, 0.3);
}

.welcome-text {
  text-align: center;
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.95);
  letter-spacing: 1rpx;
}

/* 分类列表 */
.category-list {
  padding: 0 32rpx;
}

.category-item {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 32rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(255, 154, 118, 0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: all 0.3s ease;
}

.category-item:active {
  transform: scale(0.98);
  box-shadow: 0 2rpx 8rpx rgba(255, 154, 118, 0.15);
}

.category-left {
  display: flex;
  align-items: center;
  gap: 24rpx;
}

.category-icon-wrapper {
  width: 96rpx;
  height: 96rpx;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.category-icon {
  font-size: 56rpx;
}

.category-info {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.category-name {
  font-size: 36rpx;
  font-weight: bold;
  color: #333333;
}

.category-count {
  font-size: 28rpx;
  color: #999999;
}

.category-arrow {
  width: 48rpx;
  height: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.arrow-icon {
  font-size: 56rpx;
  color: #cccccc;
  font-weight: 300;
}

/* 底部按钮区 */
.bottom-actions {
  position: fixed;
  bottom: 48rpx;
  left: 48rpx;
  right: 48rpx;
  display: flex;
  gap: 24rpx;
}

.action-btn {
  flex: 1;
  height: 120rpx;
  border-radius: 24rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  box-shadow: 0 4rpx 16rpx rgba(255, 154, 118, 0.2);
  transition: all 0.3s ease;
}

.action-btn:active {
  transform: scale(0.95);
}

.trash-btn {
  background: #ffffff;
  border: 2rpx solid #ff9a76;
}

.manage-btn {
  background: linear-gradient(135deg, #ff9a76 0%, #ff7e5f 100%);
}

.btn-icon {
  font-size: 48rpx;
}

.trash-btn .btn-label {
  font-size: 24rpx;
  color: #ff9a76;
  font-weight: 500;
}

.manage-btn .btn-label {
  font-size: 24rpx;
  color: #ffffff;
  font-weight: 500;
}
</style>
