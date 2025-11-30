<template>
  <view class="login-container">
    <!-- Logo区域 -->
    <view class="logo-section">
      <view class="logo-icon">📝</view>
      <text class="app-name">云日记</text>
      <text class="app-slogan">记录生活，留住美好时光</text>
    </view>
    
    <!-- 登录表单 -->
    <view class="form-section">
      <!-- 手机验证码登录 -->
      <view class="form-container">
        <view class="input-group">
          <view class="input-wrapper">
            <text class="input-icon">📱</text>
            <input 
              class="input-field" 
              v-model="phone" 
              type="number"
              maxlength="11"
              placeholder="请输入手机号"
              placeholder-class="input-placeholder"
            />
          </view>
        </view>
        
        <view class="input-group">
          <view class="input-wrapper">
            <text class="input-icon">🔑</text>
            <input 
              class="input-field" 
              v-model="verifyCode" 
              type="number"
              maxlength="6"
              placeholder="请输入验证码"
              placeholder-class="input-placeholder"
            />
            <button 
              class="code-btn" 
              :disabled="codeCountdown > 0"
              @click="sendVerifyCode"
            >
              {{ codeCountdown > 0 ? `${codeCountdown}s` : '获取验证码' }}
            </button>
          </view>
        </view>
        
        <button class="login-btn" @click="handlePhoneLogin">登录</button>
        <view class="login-tip">未注册手机号验证后自动创建账号</view>
      </view>
      
      <!-- 微信登录 -->
      <view class="wechat-section">
        <view class="divider">
          <view class="divider-line"></view>
          <text class="divider-text">或</text>
          <view class="divider-line"></view>
        </view>
        
        <!-- #ifdef MP-WEIXIN -->
        <button 
          class="wechat-login-btn" 
          open-type="getUserInfo"
          @getuserinfo="handleWechatLogin"
        >
          <text class="wechat-icon">💬</text>
          <text>微信一键登录</text>
        </button>
        <!-- #endif -->
        
        <!-- #ifndef MP-WEIXIN -->
        <view class="wechat-tip">
          <text>请在微信小程序中使用微信登录</text>
        </view>
        <!-- #endif -->
      </view>
    </view>
    
    <!-- 底部提示 -->
    <view class="footer-section">
      <text class="footer-text">登录即表示同意</text>
      <text class="footer-link">《用户协议》</text>
      <text class="footer-text">和</text>
      <text class="footer-link">《隐私政策》</text>
    </view>
  </view>
</template>

<script>
import { sendSmsCode, phoneLogin, wechatLogin } from '@/utils/api.js'
import { setToken, setUserInfo } from '@/utils/auth.js'

export default {
  data() {
    return {
      phone: '',
      verifyCode: '',
      codeCountdown: 0,
      countdownTimer: null
    }
  },
  
  onUnload() {
    // 页面卸载时清除定时器
    if (this.countdownTimer) {
      clearInterval(this.countdownTimer)
    }
  },
  
  methods: {
    /**
     * 发送验证码
     */
    async sendVerifyCode() {
      // 验证手机号
      if (!this.phone) {
        uni.showToast({
          title: '请输入手机号',
          icon: 'none'
        })
        return
      }
      
      if (!/^1[3-9]\d{9}$/.test(this.phone)) {
        uni.showToast({
          title: '请输入正确的手机号',
          icon: 'none'
        })
        return
      }
      
      try {
        await sendSmsCode({ phone: this.phone })
        
        uni.showToast({
          title: '验证码已发送',
          icon: 'success'
        })
        
        // 开始倒计时
        this.codeCountdown = 60
        this.countdownTimer = setInterval(() => {
          this.codeCountdown--
          if (this.codeCountdown <= 0) {
            clearInterval(this.countdownTimer)
            this.countdownTimer = null
          }
        }, 1000)
      } catch (error) {
        console.error('发送验证码失败', error)
      }
    },
    
    /**
     * 手机验证码登录
     */
    async handlePhoneLogin() {
      // 验证输入
      if (!this.phone) {
        uni.showToast({
          title: '请输入手机号',
          icon: 'none'
        })
        return
      }
      
      if (!/^1[3-9]\d{9}$/.test(this.phone)) {
        uni.showToast({
          title: '请输入正确的手机号',
          icon: 'none'
        })
        return
      }
      
      if (!this.verifyCode) {
        uni.showToast({
          title: '请输入验证码',
          icon: 'none'
        })
        return
      }
      
      if (!/^\d{6}$/.test(this.verifyCode)) {
        uni.showToast({
          title: '请输入6位验证码',
          icon: 'none'
        })
        return
      }
      
      try {
        const res = await phoneLogin({
          phone: this.phone,
          code: this.verifyCode
        })
        
        // 保存token和用户信息
        setToken(res.token)
        setUserInfo(res.userInfo)
        
        uni.showToast({
          title: '登录成功',
          icon: 'success',
          duration: 1500
        })
        
        // 返回上一页或跳转到首页
        setTimeout(() => {
          const pages = getCurrentPages()
          if (pages.length > 1) {
            uni.navigateBack()
          } else {
            uni.switchTab({
              url: '/pages/index/index'
            })
          }
        }, 1500)
      } catch (error) {
        console.error('登录失败', error)
      }
    },
    
    /**
     * 微信登录
     */
    async handleWechatLogin(e) {
      // 检查用户是否授权
      if (!e.detail.userInfo) {
        uni.showToast({
          title: '您拒绝了授权',
          icon: 'none'
        })
        return
      }
      
      try {
        // 获取微信登录code
        const loginRes = await uni.login()
        
        if (!loginRes.code) {
          throw new Error('获取微信登录code失败')
        }
        
        // 调用后端微信登录接口
        const res = await wechatLogin({
          code: loginRes.code
        })
        
        // 保存token和用户信息
        setToken(res.token)
        setUserInfo(res.userInfo)
        
        uni.showToast({
          title: '登录成功',
          icon: 'success',
          duration: 1500
        })
        
        // 返回上一页或跳转到首页
        setTimeout(() => {
          const pages = getCurrentPages()
          if (pages.length > 1) {
            uni.navigateBack()
          } else {
            uni.switchTab({
              url: '/pages/index/index'
            })
          }
        }, 1500)
      } catch (error) {
        console.error('微信登录失败', error)
        uni.showToast({
          title: '微信登录失败',
          icon: 'none'
        })
      }
    }
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #FF9A76 0%, #FF7E5F 100%);
  display: flex;
  flex-direction: column;
  padding: 100rpx 60rpx 60rpx;
}

/* Logo区域 */
.logo-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 80rpx;
}

.logo-icon {
  width: 160rpx;
  height: 160rpx;
  border-radius: 32rpx;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 88rpx;
  margin-bottom: 32rpx;
}

.app-name {
  font-size: 48rpx;
  font-weight: bold;
  color: #fff;
  margin-bottom: 16rpx;
}

.app-slogan {
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.9);
}

/* 表单区域 */
.form-section {
  flex: 1;
}

.form-container {
  background: #fff;
  border-radius: 24rpx;
  padding: 60rpx 40rpx;
  box-shadow: 0 8rpx 32rpx rgba(255, 126, 95, 0.2);
}

.input-group {
  margin-bottom: 32rpx;
}

.input-wrapper {
  display: flex;
  align-items: center;
  background: #FFF5F0;
  border-radius: 16rpx;
  padding: 24rpx 32rpx;
  position: relative;
}

.input-icon {
  font-size: 40rpx;
  margin-right: 24rpx;
}

.input-field {
  flex: 1;
  font-size: 32rpx;
  color: #333;
}

.input-placeholder {
  color: #999;
}

.code-btn {
  position: absolute;
  right: 16rpx;
  padding: 12rpx 24rpx;
  background: linear-gradient(135deg, #FF9A76 0%, #FF7E5F 100%);
  color: #fff;
  border-radius: 32rpx;
  font-size: 24rpx;
  border: none;
  line-height: 1;
}

.code-btn[disabled] {
  background: #E0E0E0;
  color: #999;
}

.login-btn {
  width: 100%;
  height: 96rpx;
  background: linear-gradient(135deg, #FF9A76 0%, #FF7E5F 100%);
  border-radius: 48rpx;
  font-size: 32rpx;
  font-weight: bold;
  color: #fff;
  border: none;
  margin-top: 40rpx;
  box-shadow: 0 8rpx 24rpx rgba(255, 126, 95, 0.4);
}

.login-btn:active {
  opacity: 0.8;
}

.login-tip {
  text-align: center;
  font-size: 24rpx;
  color: #999;
  margin-top: 24rpx;
}

/* 微信登录区域 */
.wechat-section {
  margin-top: 60rpx;
}

.divider {
  display: flex;
  align-items: center;
  margin-bottom: 40rpx;
}

.divider-line {
  flex: 1;
  height: 2rpx;
  background: rgba(255, 255, 255, 0.3);
}

.divider-text {
  margin: 0 24rpx;
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.9);
}

.wechat-login-btn {
  width: 100%;
  height: 96rpx;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 48rpx;
  font-size: 32rpx;
  font-weight: bold;
  color: #07c160;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 24rpx rgba(255, 255, 255, 0.2);
}

.wechat-login-btn:active {
  opacity: 0.8;
}

.wechat-icon {
  font-size: 40rpx;
  margin-right: 16rpx;
}

.wechat-tip {
  text-align: center;
  padding: 32rpx;
  color: rgba(255, 255, 255, 0.7);
  font-size: 28rpx;
}

/* 底部提示 */
.footer-section {
  text-align: center;
  padding-top: 60rpx;
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.7);
}

.footer-link {
  color: rgba(255, 255, 255, 0.95);
  text-decoration: underline;
}
</style>
