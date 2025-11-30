<template>
  <view class="settings-container">
    <!-- 设置项列表 -->
    <view class="settings-section">
      <view class="section-title">显示设置</view>
      
      <!-- 日记卡片对齐方式 -->
      <view class="settings-item" @click="ShowAlignmentPicker">
        <view class="settings-item-left">
          <text class="settings-icon">📐</text>
          <text class="settings-label">日记卡片对齐</text>
        </view>
        <view class="settings-item-right">
          <text class="settings-value">{{ alignmentOptions[currentAlignment].label }}</text>
          <text class="settings-arrow">›</text>
        </view>
      </view>
    </view>
    
    <!-- 选项说明 -->
    <view class="settings-tip">
      <text class="tip-text">{{ alignmentOptions[currentAlignment].description }}</text>
    </view>
  </view>
</template>

<script>
import { requireLogin } from '@/utils/auth.js'

export default {
  data() {
    return {
      currentAlignment: 'alternate', // 默认是奇偶交替
      alignmentOptions: {
        alternate: {
          label: '奇偶交替',
          description: '奇数次序的日记封面在右侧，偶数次序的日记封面在左侧，文字对齐方式跟随封面位置变化'
        },
        default: {
          label: '默认对齐',
          description: '所有日记封面都在右侧，文字在左侧左对齐'
        }
      }
    }
  },
  
  onLoad() {
    // 检查登录状态
    if (!requireLogin()) {
      return
    }
    this.LoadSettings()
  },
  
  methods: {
    /**
     * 加载设置
     */
    LoadSettings() {
      try {
        const alignment = uni.getStorageSync('diary_card_alignment')
        if (alignment) {
          this.currentAlignment = alignment
        }
      } catch (e) {
        console.error('加载设置失败:', e)
      }
    },
    
    /**
     * 显示对齐方式选择器
     */
    ShowAlignmentPicker() {
      const options = [
        this.alignmentOptions.alternate.label,
        this.alignmentOptions.default.label
      ]
      
      uni.showActionSheet({
        itemList: options,
        success: (res) => {
          const alignmentKeys = ['alternate', 'default']
          const selectedAlignment = alignmentKeys[res.tapIndex]
          
          if (selectedAlignment !== this.currentAlignment) {
            this.currentAlignment = selectedAlignment
            this.SaveAlignment()
          }
        }
      })
    },
    
    /**
     * 保存对齐方式设置
     */
    SaveAlignment() {
      try {
        uni.setStorageSync('diary_card_alignment', this.currentAlignment)
        uni.showToast({
          title: '设置已保存',
          icon: 'success',
          duration: 1500
        })
        
        // 通知其他页面刷新
        uni.$emit('alignmentChanged', this.currentAlignment)
      } catch (e) {
        console.error('保存设置失败:', e)
        uni.showToast({
          title: '保存失败',
          icon: 'none'
        })
      }
    }
  }
}
</script>

<style scoped>
.settings-container {
  min-height: 100vh;
  background: #fff5f0;
  padding-bottom: 100rpx;
}

/* 设置区块 */
.settings-section {
  margin: 32rpx 32rpx 24rpx;
}

.section-title {
  font-size: 28rpx;
  color: #999999;
  padding: 0 16rpx 16rpx;
}

/* 设置项 */
.settings-item {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 32rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 4rpx 16rpx rgba(255, 154, 118, 0.1);
}

.settings-item:active {
  background: #fafafa;
}

.settings-item-left {
  display: flex;
  align-items: center;
  gap: 24rpx;
}

.settings-icon {
  font-size: 44rpx;
  width: 60rpx;
  text-align: center;
}

.settings-label {
  font-size: 32rpx;
  color: #333333;
}

.settings-item-right {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.settings-value {
  font-size: 28rpx;
  color: #999999;
}

.settings-arrow {
  font-size: 48rpx;
  color: #cccccc;
  font-weight: 300;
}

/* 提示信息 */
.settings-tip {
  margin: 0 32rpx;
  padding: 24rpx 32rpx;
  background: rgba(255, 154, 118, 0.1);
  border-radius: 16rpx;
  border-left: 4rpx solid #ff9a76;
}

.tip-text {
  font-size: 26rpx;
  color: #666666;
  line-height: 1.6;
}
</style>
