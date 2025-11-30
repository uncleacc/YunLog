<template>
  <view class="profile-container">
    <!-- 头部信息 -->
    <view class="profile-header">
      <view class="avatar-container">
        <text class="avatar-icon">👤</text>
      </view>
      <view class="user-info">
        <text class="username">{{ isLoggedIn && userInfo ? userInfo.username : '未登录' }}</text>
        <text class="user-desc">{{ isLoggedIn ? '记录生活，留住美好时光' : '登录后开始记录' }}</text>
      </view>
    </view>

    <!-- 功能列表 -->
    <view class="menu-list">
      <!-- 未登录时显示登录入口 -->
      <view v-if="!isLoggedIn" class="menu-item" @click="handleLogin">
        <view class="menu-item-left">
          <text class="menu-icon">🔑</text>
          <text class="menu-label">登录/注册</text>
        </view>
        <text class="menu-arrow">›</text>
      </view>
      
      <!-- 已登录时显示功能菜单 -->
      <template v-else>
        <!-- 分类管理 -->
        <view class="menu-item" @click="GoToCategoryManage">
          <view class="menu-item-left">
            <text class="menu-icon">📁</text>
            <text class="menu-label">分类管理</text>
          </view>
          <text class="menu-arrow">›</text>
        </view>

        <!-- 回收站 -->
        <view class="menu-item" @click="GoToTrash">
          <view class="menu-item-left">
            <text class="menu-icon">🗑️</text>
            <text class="menu-label">回收站</text>
          </view>
          <text class="menu-arrow">›</text>
        </view>

        <!-- 设置 -->
        <view class="menu-item" @click="GoToSettings">
          <view class="menu-item-left">
            <text class="menu-icon">⚙️</text>
            <text class="menu-label">设置</text>
          </view>
          <text class="menu-arrow">›</text>
        </view>
        
        <!-- 登出 -->
        <view class="menu-item" @click="handleLogout">
          <view class="menu-item-left">
            <text class="menu-icon">🚪</text>
            <text class="menu-label">退出登录</text>
          </view>
          <text class="menu-arrow">›</text>
        </view>
      </template>
    </view>

    <!-- 版本信息 -->
    <view class="version-info">
      <text class="version-text">云日记 v1.0.0</text>
    </view>
  </view>
</template>

<script>
import { getUserInfo, logout, isLoggedIn } from '@/utils/auth.js'
import api from '@/utils/api.js'

export default {
  data() {
    return {
      userInfo: null,
      isLoggedIn: false
    }
  },
  
  onShow() {
    // 每次显示页面时刷新用户信息
    this.loadUserInfo()
  },
  
  methods: {
    // 加载用户信息
    loadUserInfo() {
      this.isLoggedIn = isLoggedIn()
      if (this.isLoggedIn) {
        this.userInfo = getUserInfo()
      } else {
        this.userInfo = null
      }
    },
    
    // 跳转到登录页
    handleLogin() {
      uni.navigateTo({
        url: '/pages/login/login'
      })
    },
    
    // 跳转到分类管理
    GoToCategoryManage() {
      uni.navigateTo({
        url: '/pages/category-manage/category-manage'
      })
    },

    // 跳转到回收站
    GoToTrash() {
      uni.navigateTo({
        url: '/pages/trash/trash'
      })
    },

    // 跳转到设置
    GoToSettings() {
      uni.navigateTo({
        url: '/pages/settings/settings'
      })
    },
    
    // 退出登录
    handleLogout() {
      uni.showModal({
        title: '确认退出',
        content: '确定要退出登录吗？',
        success: async (res) => {
          if (res.confirm) {
            try {
              // 调用登出接口（可选）
              await api.logout()
            } catch (error) {
              console.error('登出接口调用失败', error)
            } finally {
              // 清除本地认证信息
              logout()
              
              uni.showToast({
                title: '已退出登录',
                icon: 'success',
                duration: 1500
              })
              
              // 跳转到登录页
              setTimeout(() => {
                uni.reLaunch({
                  url: '/pages/login/login'
                })
              }, 1500)
            }
          }
        }
      })
    }
  }
}
</script>

<style scoped>
.profile-container {
  min-height: 100vh;
  background: #fff5f0;
}

/* 头部信息 */
.profile-header {
  background: linear-gradient(135deg, #ff9a76 0%, #ff7e5f 100%);
  padding: 80rpx 40rpx 60rpx;
  display: flex;
  align-items: center;
  gap: 32rpx;
}

.avatar-container {
  width: 140rpx;
  height: 140rpx;
  background: rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 4rpx solid rgba(255, 255, 255, 0.5);
}

.avatar-icon {
  font-size: 80rpx;
}

.user-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.username {
  font-size: 40rpx;
  font-weight: bold;
  color: #ffffff;
}

.user-desc {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.9);
}

/* 功能列表 */
.menu-list {
  margin: 32rpx 32rpx 0;
  background: #ffffff;
  border-radius: 24rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 16rpx rgba(255, 154, 118, 0.1);
}

.menu-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 36rpx 32rpx;
  border-bottom: 1rpx solid #f5f5f5;
}

.menu-item:last-child {
  border-bottom: none;
}

.menu-item:active {
  background: #fafafa;
}

.menu-item-left {
  display: flex;
  align-items: center;
  gap: 24rpx;
}

.menu-icon {
  font-size: 44rpx;
  width: 60rpx;
  text-align: center;
}

.menu-label {
  font-size: 32rpx;
  color: #333333;
}

.menu-arrow {
  font-size: 48rpx;
  color: #cccccc;
  font-weight: 300;
}

/* 版本信息 */
.version-info {
  text-align: center;
  padding: 80rpx 0 40rpx;
}

.version-text {
  font-size: 24rpx;
  color: #999999;
}
</style>
