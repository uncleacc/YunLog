<template>
  <view class="container">
    <!-- 分类信息卡片 -->
    <view class="category-header" v-if="category" :style="{ background: `linear-gradient(135deg, ${category.color}CC 0%, ${category.color}FF 100%)` }">
      <view class="category-icon-container">
        <text class="category-icon-large">{{ category.icon }}</text>
      </view>
      <view class="category-info">
        <text class="category-name-large">{{ category.name }}</text>
        <text class="category-stats">{{ diaryList.length }} 篇日记</text>
      </view>
    </view>

    <!-- 日记列表 -->
    <view class="diary-list" v-if="diaryList.length > 0">
      <view
        class="swipe-wrapper"
        v-for="(item, index) in diaryList"
        :key="item.id"
      >
        <view 
          class="swipe-item"
          @touchstart="handleTouchStart($event, item)"
          @touchmove="handleTouchMove($event, item)"
          @touchend="handleTouchEnd($event, item)"
          :style="{ transform: `translateX(${item._translateX || 0}px)`, transition: item._transition ? 'transform 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94)' : 'none' }"
        >
          <view class="diary-item" @click="ViewDiary(item)" :class="getDiaryItemClass(index)">
            <!-- 封面图片（根据对齐方式设置位置） -->
            <view class="diary-cover" v-if="item.attachments && item.attachments.length > 0 && item.attachments[0].url">
              <image
                class="cover-image"
                :src="item.attachments[0].url"
                mode="aspectFill"
              />
            </view>
            
            <!-- 内容区域 -->
            <view class="diary-content" :class="getDiaryContentClass(index)">
              <view class="diary-content-preview">{{ getPlainTextPreview(item) }}</view>
              <view class="diary-footer">
                <view class="diary-datetime">
                  <text class="diary-date">{{ FormatDate(item.createTime) }}</text>
                </view>
              </view>
            </view>
          </view>
        </view>
        
        <!-- 左滑显示的按钮 -->
        <view 
          class="swipe-actions"
          :style="{ 
            width: `${Math.abs(item._translateX || 0)}px`,
            transition: item._transition ? 'width 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94)' : 'none'
          }"
        >
          <view class="swipe-btn swipe-btn-move" @click.stop="ShowMoveDialog(item)">
            <text class="swipe-btn-text">移动</text>
          </view>
          <view class="swipe-btn swipe-btn-delete" @click.stop="ConfirmDelete(item.id)">
            <text class="swipe-btn-text">删除</text>
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
import api from '@/utils/api.js'
import { parseDate } from '@/utils/textUtils.js'
import { getPlainTextPreview } from '@/utils/textUtils.js'
import { requireLogin } from '@/utils/auth.js'

export default {
  data() {
    return {
      categoryId: '',
      category: null,
      diaryList: [],
      page: 1,
      limit: 20,
      total: 0,
      loading: false,
      allCategories: [], // 所有分类列表（用于移动操作）
      // 触摸相关状态
      touchStartX: 0,
      touchStartY: 0,
      currentTranslateX: 0,
      currentSwipeItem: null,
      swipeThreshold: 30, // 滑动触发阈值（px）
      actionWidth: 0, // 按钮总宽度（px），会在 mounted 时计算
      // 对齐方式设置
      cardAlignment: 'alternate', // 默认奇偶交替
    }
  },
  onLoad(options) {
    // 检查登录状态
    if (!requireLogin()) {
      return
    }
    
    if (options.id) {
      this.categoryId = options.id
      this.LoadAlignment()
      this.LoadData()
    }
  },
  onShow() {
    if (this.categoryId) {
      this.LoadAlignment()
      this.LoadData()
    }
    
    // 监听对齐方式变化
    uni.$on('alignmentChanged', this.OnAlignmentChanged)
  },
  onHide() {
    // 移除监听
    uni.$off('alignmentChanged', this.OnAlignmentChanged)
  },
  mounted() {
    // 计算按钮实际宽度（320rpx 转换为 px）
    // uni-app 中 750rpx = 屏幕宽度
    const systemInfo = uni.getSystemInfoSync()
    const screenWidth = systemInfo.screenWidth
    this.actionWidth = (320 / 750) * screenWidth
  },
  methods: {
    /**
     * 加载对齐方式设置
     */
    LoadAlignment() {
      try {
        const alignment = uni.getStorageSync('diary_card_alignment')
        this.cardAlignment = alignment || 'alternate'
      } catch (e) {
        console.error('加载对齐设置失败:', e)
        this.cardAlignment = 'alternate'
      }
    },
    
    /**
     * 监听对齐方式变化
     */
    OnAlignmentChanged(alignment) {
      this.cardAlignment = alignment
    },
    
    /**
     * 获取日记卡片的样式类
     */
    getDiaryItemClass(index) {
      if (this.cardAlignment === 'default') {
        // 默认模式：所有封面都在右侧
        return 'cover-right'
      } else {
        // 奇偶交替模式：奇数在右，偶数在左
        return (index + 1) % 2 === 1 ? 'cover-right' : ''
      }
    },
    
    /**
     * 获取内容区域的样式类
     */
    getDiaryContentClass(index) {
      if (this.cardAlignment === 'default') {
        // 默认模式：所有文字都左对齐
        return ''
      } else {
        // 奇偶交替模式：偶数右对齐
        return (index + 1) % 2 === 0 ? 'align-right' : ''
      }
    },
    
    async LoadData() {
      if (this.loading) return
      this.loading = true
      
      try {
        // 加载分类信息
        this.category = await api.getCategoryDetail(this.categoryId)
        
        // 加载该分类下的日记列表
        const res = await api.getDiaryList({
          categoryId: this.categoryId,
          page: this.page,
          limit: this.limit
        })
        
        const processedList = (res.list || []).map(diary => ({
          ...diary,
          // 过滤掉无效的附件对象
          attachments: (diary.attachments || []).filter(att => att && att.url)
        }))
        
        // 强制重新渲染：先清空列表，下一帧再赋值
        this.diaryList = []
        this.$nextTick(() => {
          this.diaryList = processedList
          this.total = res.total || 0
        })
        
        // 加载所有分类（用于移动操作）
        await this.LoadAllCategories()
      } catch (error) {
        console.error('加载数据失败:', error)
        uni.showToast({
          title: '加载失败，请重试',
          icon: 'none',
        })
      } finally {
        this.loading = false
      }
    },

    async LoadAllCategories() {
      try {
        const categories = await api.getCategoryList()
        // 过滤掉当前分类
        this.allCategories = (categories || []).filter(cat => cat.id != this.categoryId)
      } catch (error) {
        console.error('加载分类列表失败:', error)
      }
    },

    // 触摸开始
    handleTouchStart(e, item) {
      const touch = e.touches[0]
      this.touchStartX = touch.clientX
      this.touchStartY = touch.clientY
      this.currentSwipeItem = item
      
      // 记录当前项的初始位置
      item._startTranslateX = item._translateX || 0
      
      // 禁用过渡动画
      this.$set(item, '_transition', false)
      
      // 关闭其他已打开的项
      this.diaryList.forEach(otherItem => {
        if (otherItem.id !== item.id && otherItem._translateX < 0) {
          this.$set(otherItem, '_translateX', 0)
          this.$set(otherItem, '_transition', true)
        }
      })
    },

    // 触摸移动
    handleTouchMove(e, item) {
      if (!this.currentSwipeItem || this.currentSwipeItem.id !== item.id) return
      
      const touch = e.touches[0]
      const deltaX = touch.clientX - this.touchStartX
      const deltaY = touch.clientY - this.touchStartY
      
      // 如果垂直滑动距离大于水平滑动距离，认为是垂直滚动，不处理
      if (Math.abs(deltaY) > Math.abs(deltaX)) {
        return
      }
      
      // 阻止默认滚动行为
      e.preventDefault()
      
      const startPos = item._startTranslateX || 0
      let newTranslateX = startPos + deltaX
      
      // 向左滑动
      if (newTranslateX < 0) {
        // 允许超出边界滑动，但增加阻力（橡皮筋效果）
        if (newTranslateX < -this.actionWidth) {
          const overScroll = Math.abs(newTranslateX) - this.actionWidth
          // 超出部分应用 0.3 的阻力系数
          newTranslateX = -this.actionWidth - (overScroll * 0.3)
        }
        this.$set(item, '_translateX', newTranslateX)
      } 
      // 向右滑动（关闭）
      else if (newTranslateX > 0) {
        // 未打开状态不允许向右滑
        if (startPos >= 0) {
          this.$set(item, '_translateX', 0)
        } else {
          // 已打开状态允许向右滑动关闭
          this.$set(item, '_translateX', newTranslateX)
        }
      } 
      // 处于中间位置
      else {
        this.$set(item, '_translateX', newTranslateX)
      }
    },

    // 触摸结束
    handleTouchEnd(e, item) {
      if (!this.currentSwipeItem || this.currentSwipeItem.id !== item.id) return
      
      // 启用过渡动画
      this.$set(item, '_transition', true)
      
      const currentTranslate = item._translateX || 0
      const startPos = item._startTranslateX || 0
      
      // 计算滑动距离
      const moveDistance = currentTranslate - startPos
      
      // 判断是向左还是向右滑动
      if (moveDistance < 0) {
        // 向左滑动
        // 如果滑动距离超过阈值，显示按钮
        if (Math.abs(moveDistance) > this.swipeThreshold) {
          this.$set(item, '_translateX', -this.actionWidth)
        } else {
          // 否则回弹到原位
          this.$set(item, '_translateX', startPos)
        }
      } else if (moveDistance > 0) {
        // 向右滑动
        // 如果原来是打开状态，且滑动距离超过阈值，则关闭
        if (startPos < 0 && moveDistance > this.swipeThreshold) {
          this.$set(item, '_translateX', 0)
        } else {
          // 否则回弹到原位（保持打开状态）
          this.$set(item, '_translateX', startPos)
        }
      } else {
        // 没有滑动，保持原位
        this.$set(item, '_translateX', startPos)
      }
      
      this.currentSwipeItem = null
      // 清除起始位置记录
      delete item._startTranslateX
    },

    // 关闭所有滑动项
    closeAllSwipe() {
      this.diaryList.forEach(item => {
        this.$set(item, '_translateX', 0)
        this.$set(item, '_transition', true)
      })
    },

    // 处理左滑点击事件
    handleSwipeClick(e, item) {
      const index = e.index
      if (index === 0) {
        // 移动日记
        this.ShowMoveDialog(item)
      } else if (index === 1) {
        // 删除日记
        this.ConfirmDelete(item.id)
      }
    },

    // 显示移动对话框
    ShowMoveDialog(diary) {
      if (!this.allCategories || this.allCategories.length === 0) {
        uni.showToast({
          title: '没有其他分类可移动',
          icon: 'none',
        })
        return
      }

      // 构建分类选项列表
      const itemList = this.allCategories.map(cat => `${cat.icon} ${cat.name}`)
      
      uni.showActionSheet({
        title: '移动到',
        itemList: itemList,
        success: (res) => {
          const selectedCategory = this.allCategories[res.tapIndex]
          this.MoveDiary(diary, selectedCategory)
        }
      })
    },

    // 移动日记到其他分类
    async MoveDiary(diary, targetCategory) {
      try {
        await api.updateDiary(diary.id, {
          content: diary.content,
          contentHtml: diary.contentHtml,
          categoryId: targetCategory.id
        })
        
        uni.showToast({
          title: `已移动到 ${targetCategory.name}`,
          icon: 'success',
        })
        
        // 重新加载数据
        this.LoadData()
      } catch (error) {
        console.error('移动日记失败:', error)
        uni.showToast({
          title: '移动失败，请重试',
          icon: 'none',
        })
      }
    },

    FormatDate(dateString) {
      try {
        const date = parseDate(dateString)
        const month = date.getMonth() + 1
        const day = date.getDate()
        const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
        const weekDay = weekDays[date.getDay()]
        return `${month}月${day}日 ${weekDay}`
      } catch (error) {
        console.error('FormatDate - 日期格式化失败:', error, dateString)
        return '--'
      }
    },

    // 获取纯文本预览（使用工具函数）
    getPlainTextPreview(item) {
      return getPlainTextPreview(item, 100)
    },

    AddNewDiary() {
      uni.navigateTo({
        url: `/pages/edit/edit?categoryId=${this.categoryId}`,
      })
    },

    ViewDiary(item) {
      // 如果当前项已打开，点击后关闭
      if (item._translateX < 0) {
        this.$set(item, '_translateX', 0)
        this.$set(item, '_transition', true)
        return
      }
      
      // 如果其他项打开，先关闭
      const hasOtherOpenSwipe = this.diaryList.some(
        otherItem => otherItem.id !== item.id && otherItem._translateX < 0
      )
      if (hasOtherOpenSwipe) {
        this.closeAllSwipe()
        return
      }
      
      // 跳转到详情页
      uni.navigateTo({
        url: `/pages/detail/detail?id=${item.id}`,
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

    async DeleteDiary(id) {
      try {
        await api.deleteDiary(id)
        uni.showToast({
          title: '已移到回收站',
          icon: 'success',
        })
        // 重新加载数据
        this.LoadData()
      } catch (error) {
        console.error('删除失败:', error)
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
  width: 100%;
  box-sizing: border-box;
}

/* 分类信息卡片 */
.category-header {
  margin: 32rpx;
  padding: 32rpx 40rpx;
  border-radius: 32rpx;
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 24rpx rgba(255, 126, 95, 0.3);
}

.category-icon-container {
  margin-right: 24rpx;
}

.category-icon-large {
  font-size: 96rpx;
}

.category-info {
  display: flex;
  flex-direction: column;
}

.category-name-large {
  font-size: 44rpx;
  font-weight: bold;
  color: #ffffff;
  margin-bottom: 8rpx;
}

.category-stats {
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.9);
}

/* 日记列表 */
.diary-list {
  padding: 0 24rpx;
  width: 100%;
  box-sizing: border-box;
}

/* 左滑容器 */
.swipe-wrapper {
  position: relative;
  overflow: hidden;
  margin-bottom: 24rpx;
}

.swipe-item {
  position: relative;
  z-index: 2;
  background: #fff5f0;
}

.swipe-actions {
  position: absolute;
  right: 0;
  top: 0;
  bottom: 0;
  display: flex;
  z-index: 1;
  /* 宽度由内联样式动态控制,跟随卡片滑动距离 */
}

.swipe-btn {
  flex: 1; /* 平均分配宽度 */
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  font-size: 28rpx;
}

.swipe-btn-move {
  background: #4CAF50;
}

.swipe-btn-delete {
  background: #FF5252;
}

.swipe-btn-text {
  color: #ffffff;
  font-size: 28rpx;
}

.diary-item {
  background: #ffffff;
  border-radius: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(255, 154, 118, 0.1);
  width: 100%;
  box-sizing: border-box;
  display: flex;
  flex-direction: row;  /* 默认：封面在左，内容在右 */
  min-height: 200rpx;
  overflow: hidden;
}

/* 奇数次序：封面在右边（反转顺序） */
.diary-item.cover-right {
  flex-direction: row-reverse;
}

/* 封面区域：占据三分之一宽度，正方形 */
.diary-cover {
  width: 200rpx;
  height: 200rpx;
  flex-shrink: 0;
  overflow: hidden;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cover-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 内容区域：占据三分之二宽度 */
.diary-content {
  flex: 1;
  padding: 24rpx;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-width: 0;
}

/* 偶数项：封面在左，内容右对齐 */
.diary-content.align-right {
  align-items: flex-end;
}

.diary-content-preview {
  font-size: 26rpx;
  color: #666666;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 12rpx;
  flex: 1;
  word-wrap: break-word;
  word-break: break-all;
  /* 支持表情符号显示 */
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", "Roboto", "Helvetica Neue", Arial, "Noto Color Emoji", "Apple Color Emoji", "Segoe UI Emoji", "Segoe UI Symbol", sans-serif;
}

/* 偶数项：预览文本右对齐 */
.diary-content.align-right .diary-content-preview {
  text-align: right;
}

.diary-footer {
  display: flex;
  justify-content: flex-start;
  align-items: center;
  padding-top: 12rpx;
  border-top: 1rpx solid #f5f5f5;
  width: 100%;
  box-sizing: border-box;
}

/* 偶数项：footer 右对齐 */
.diary-content.align-right .diary-footer {
  justify-content: flex-end;
}

.diary-datetime {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 6rpx;
}

.diary-date {
  font-size: 24rpx;
  color: #999999;
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
  z-index: 100; /* 确保按钮在所有卡片上方 */
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

