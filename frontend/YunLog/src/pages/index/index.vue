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
        :class="{ 
          'dragging': isDragging && draggingIndex === index,
          'placeholder': isDragging && placeholderIndex === index && index !== draggingIndex
        }"
        :style="getCategoryItemStyle(item, index)"
        v-for="(item, index) in categoryList"
        :key="item.id"
        @touchstart="OnTouchStart($event, item, index)"
        @touchmove="OnTouchMove($event)"
        @touchend="OnTouchEnd"
        @click="OnCategoryClick(item, index)"
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

    <!-- 空状态 -->
    <view class="empty-state" v-else>
      <text class="empty-icon">📝</text>
      <text class="empty-text" v-if="!isUserLoggedIn()">登录后开始记录生活</text>
      <text class="empty-text" v-else>还没有分类</text>
      <text class="empty-hint" v-if="!isUserLoggedIn()">前往个人中心登录</text>
      <text class="empty-hint" v-else>前往个人中心创建分类</text>
    </view>
  </view>
</template>

<script>
import { getCategoryList, getCategoryStats, getGlobalStats, updateCategorySort } from '@/utils/api.js'
import { isLoggedIn } from '@/utils/auth.js'

export default {
  data() {
    return {
      categoryList: [],
      globalStats: {
        totalCount: 0,
        continueDays: 0,
      },
      categoryStatsMap: {},
      
      // 拖动状态
      isDragging: false,
      draggingIndex: null,
      draggingItem: null,
      placeholderIndex: null,
      
      // 触摸信息
      touchStartTime: 0,
      touchStartY: 0,
      touchStartX: 0,
      currentTouchY: 0,
      dragOffsetY: 0,
      
      // 配置
      longPressDelay: 500,
      longPressTimer: null,
      itemHeight: 0,
      itemStartY: 0,
    }
  },
  onShow() {
    this.LoadData()
  },
  methods: {
    async LoadData() {
      // 未登录时不加载数据
      if (!isLoggedIn()) {
        this.categoryList = []
        this.globalStats = {
          totalCount: 0,
          continueDays: 0,
        }
        this.categoryStatsMap = {}
        return
      }
      
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
    
    isUserLoggedIn() {
      return isLoggedIn()
    },

    GoToCategory(categoryId) {
      uni.navigateTo({
        url: `/pages/category/category?id=${categoryId}`,
      })
    },
    
    // ==================== 拖动排序相关方法 ====================
    
    /**
     * 触摸开始
     */
    OnTouchStart(e, item, index) {
      this.touchStartTime = Date.now()
      this.touchStartY = e.touches[0].pageY
      this.touchStartX = e.touches[0].pageX
      
      // 启动长按定时器
      this.longPressTimer = setTimeout(() => {
        this.StartDragging(item, index, e.touches[0].pageY)
      }, this.longPressDelay)
    },
    
    /**
     * 开始拖动
     */
    StartDragging(item, index, touchY) {
      this.isDragging = true
      this.draggingIndex = index
      this.draggingItem = item
      this.placeholderIndex = index
      this.currentTouchY = touchY
      this.itemStartY = touchY
      
      // 计算分类项高度
      const query = uni.createSelectorQuery().in(this)
      query.select('.category-item').boundingClientRect(data => {
        if (data) {
          this.itemHeight = data.height + 24 // 24rpx 是间距，转换为 px 约 12px
        }
      }).exec()
      
      // 震动反馈
      uni.vibrateShort({ type: 'light' })
      
    },
    
    /**
     * 触摸移动
     */
    OnTouchMove(e) {
      if (!this.isDragging) {
        // 移动超过阈值则取消长按
        const moveDistanceY = Math.abs(e.touches[0].pageY - this.touchStartY)
        const moveDistanceX = Math.abs(e.touches[0].pageX - this.touchStartX)
        if (moveDistanceY > 10 || moveDistanceX > 10) {
          this.CancelLongPress()
        }
        return
      }
      
      e.preventDefault()
      
      // 更新拖动位置
      this.currentTouchY = e.touches[0].pageY
      this.dragOffsetY = this.currentTouchY - this.itemStartY
      
      // 计算新的占位符位置
      const newPlaceholderIndex = this.CalculateNewIndex()
      
      if (newPlaceholderIndex !== this.placeholderIndex) {
        this.placeholderIndex = newPlaceholderIndex
        this.ReorderCategoryList()
      }
    },
    
    /**
     * 触摸结束
     */
    OnTouchEnd() {
      this.CancelLongPress()
      
      if (!this.isDragging) return
      
      // 保存新的排序
      this.SaveNewOrder()
      
      // 重置状态
      setTimeout(() => {
        this.isDragging = false
        this.draggingIndex = null
        this.draggingItem = null
        this.placeholderIndex = null
        this.dragOffsetY = 0
      }, 300)
    },
    
    /**
     * 取消长按
     */
    CancelLongPress() {
      if (this.longPressTimer) {
        clearTimeout(this.longPressTimer)
        this.longPressTimer = null
      }
    },
    
    /**
     * 计算新的索引位置
     */
    CalculateNewIndex() {
      if (!this.itemHeight) return this.draggingIndex
      
      const moveCount = Math.round(this.dragOffsetY / this.itemHeight)
      let newIndex = this.draggingIndex + moveCount
      
      // 限制范围
      newIndex = Math.max(0, Math.min(this.categoryList.length - 1, newIndex))
      
      return newIndex
    },
    
    /**
     * 重新排列分类列表
     */
    ReorderCategoryList() {
      const list = [...this.categoryList]
      const draggedItem = list.splice(this.draggingIndex, 1)[0]
      list.splice(this.placeholderIndex, 0, draggedItem)
      
      this.categoryList = list
      this.draggingIndex = this.placeholderIndex
      this.itemStartY = this.currentTouchY
      this.dragOffsetY = 0
    },
    
    /**
     * 保存新的排序
     */
    async SaveNewOrder() {
      const categorySortList = this.categoryList.map((item, index) => ({
        id: item.id,
        sortOrder: index
      }))
      
      try {
        await updateCategorySort(categorySortList)
      } catch (error) {
        console.error('保存排序失败:', error)
        uni.showToast({
          title: '保存失败，请重试',
          icon: 'none'
        })
        // 恢复原始顺序
        this.LoadData()
      }
    },
    
    /**
     * 获取分类项样式
     */
    getCategoryItemStyle(item, index) {
      const style = {}
      
      // 正在拖动的项
      if (this.isDragging && index === this.draggingIndex) {
        style.transform = `translateY(${this.dragOffsetY}px)`
        style.opacity = '0.9'
        style.zIndex = '1000'
        style.boxShadow = '0 8rpx 32rpx rgba(255, 154, 118, 0.3)'
        style.transition = 'none'
      }
      
      // 其他项的过渡动画
      if (this.isDragging && index !== this.draggingIndex) {
        style.transition = 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)'
      }
      
      return style
    },
    
    /**
     * 点击分类（区分拖动和点击）
     */
    OnCategoryClick(item, index) {
      // 如果刚结束拖动，不触发点击
      const touchDuration = Date.now() - this.touchStartTime
      if (touchDuration > this.longPressDelay) {
        return
      }
      
      // 正常点击跳转
      this.GoToCategory(item.id)
    },
  },
}
</script>

<style scoped>
.container {
  min-height: 100vh;
  padding-bottom: 150rpx; /* 为 TabBar 留出空间 */
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
  touch-action: none;
  position: relative;
}

.category-item:active {
  transform: scale(0.98);
  box-shadow: 0 2rpx 8rpx rgba(255, 154, 118, 0.15);
}

.category-item.dragging {
  transform: scale(1.02);
  box-shadow: 0 8rpx 32rpx rgba(255, 154, 118, 0.3);
  opacity: 0.9;
  z-index: 1000;
}

.category-item.placeholder {
  background: rgba(255, 154, 118, 0.08);
  border: 2rpx dashed #ff9a76;
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
}

.empty-text {
  font-size: 32rpx;
  color: #666666;
  margin-bottom: 16rpx;
}

.empty-hint {
  font-size: 26rpx;
  color: #999999;
}
</style>

