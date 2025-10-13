<script>
export default {
  onLaunch: function () {
    console.log('App Launch')
  },
  onShow: function () {
    console.log('App Show')
  },
  onHide: function () {
    console.log('App Hide')
  },
}
</script>

<style>
/* 每个页面公共css - 暖色系主题 */
page {
  background-color: #FFF5F0;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', 'Helvetica', 'Arial', sans-serif;
}

/* 全局按钮样式 */
.btn-primary {
  background: linear-gradient(135deg, #FF9A76 0%, #FF7E5F 100%);
  color: #FFFFFF;
  border: none;
  border-radius: 24rpx;
  padding: 24rpx 48rpx;
  font-size: 32rpx;
  box-shadow: 0 8rpx 24rpx rgba(255, 126, 95, 0.3);
}

.btn-secondary {
  background: #FFFFFF;
  color: #FF9A76;
  border: 2rpx solid #FF9A76;
  border-radius: 24rpx;
  padding: 24rpx 48rpx;
  font-size: 32rpx;
}

/* 卡片样式 */
.card {
  background: #FFFFFF;
  border-radius: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(255, 154, 118, 0.1);
  padding: 32rpx;
  margin: 24rpx;
}

/* 文本颜色 */
.text-primary {
  color: #FF9A76;
}

.text-secondary {
  color: #999999;
}

.text-dark {
  color: #333333;
}

/* 通用工具类 */
.flex-center {
  display: flex;
  align-items: center;
  justify-content: center;
}

.flex-between {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
</style>
