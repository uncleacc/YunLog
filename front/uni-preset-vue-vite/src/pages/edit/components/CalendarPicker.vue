<template>
  <view class="calendar-picker" v-if="visible">
    <!-- 遮罩层 -->
    <view class="calendar-mask" @click="closeCalendar"></view>
    
    <!-- 日历容器 -->
    <view class="calendar-container">
      <!-- 日历头部 -->
      <view class="calendar-header">
        <view class="header-title">选择日记时间</view>
        <view class="header-close" @click="closeCalendar">
          <text class="close-icon">×</text>
        </view>
      </view>
      
      <!-- 年月导航 -->
      <view class="calendar-nav">
        <view class="nav-btn" @click="previousMonth">
          <text class="nav-icon">‹</text>
        </view>
        <view class="nav-title">
          <text class="nav-year">{{ currentYear }}年</text>
          <text class="nav-month">{{ currentMonth }}月</text>
        </view>
        <view class="nav-btn" @click="nextMonth">
          <text class="nav-icon">›</text>
        </view>
      </view>
      
      <!-- 星期标题 -->
      <view class="calendar-weekdays">
        <view 
          class="weekday-item"
          v-for="day in weekDays" 
          :key="day"
        >
          {{ day }}
        </view>
      </view>
      
      <!-- 日期网格 -->
      <view class="calendar-dates">
        <view 
          class="date-item"
          v-for="date in calendarDates" 
          :key="date.key"
          :class="{
            'date-other-month': date.isOtherMonth,
            'date-today': date.isToday,
            'date-selected': date.isSelected,
            'date-disabled': date.isDisabled
          }"
          @click="selectDate(date)"
        >
          <text class="date-text">{{ date.day }}</text>
        </view>
      </view>
      
      <!-- 快捷操作 -->
      <view class="calendar-shortcuts">
        <view class="shortcut-btn" @click="selectToday">
          <text class="shortcut-text">今天</text>
          <text class="shortcut-date">{{ formatShortDate(today) }}</text>
        </view>
        <view class="shortcut-btn" @click="selectYesterday">
          <text class="shortcut-text">昨天</text>
          <text class="shortcut-date">{{ formatShortDate(yesterday) }}</text>
        </view>
        <view class="shortcut-btn" @click="selectDayBeforeYesterday">
          <text class="shortcut-text">前天</text>
          <text class="shortcut-date">{{ formatShortDate(dayBeforeYesterday) }}</text>
        </view>
      </view>
      
      <!-- 选中日期显示 -->
      <view class="selected-date-info" v-if="selectedDate">
        <text class="selected-label">已选择：</text>
        <text class="selected-value">{{ formatSelectedDate(selectedDate) }}</text>
      </view>
      
      <!-- 确认按钮 -->
      <view class="calendar-footer">
        <view class="footer-btn cancel-btn" @click="closeCalendar">取消</view>
        <view 
          class="footer-btn confirm-btn" 
          :class="{ 'confirm-disabled': !selectedDate }"
          @click="confirmSelection"
        >
          确定
        </view>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  name: 'CalendarPicker',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    defaultDate: {
      type: String,
      default: ''
    }
  },
  
  data() {
    return {
      currentYear: new Date().getFullYear(),
      currentMonth: new Date().getMonth() + 1,
      selectedDate: null,
      today: new Date(),
      weekDays: ['日', '一', '二', '三', '四', '五', '六']
    }
  },
  
  computed: {
    // 昨天
    yesterday() {
      return new Date(this.today.getTime() - 24 * 60 * 60 * 1000)
    },
    
    // 前天
    dayBeforeYesterday() {
      return new Date(this.today.getTime() - 2 * 24 * 60 * 60 * 1000)
    },
    
    // 计算日历显示的日期数组
    calendarDates() {
      const dates = []
      const year = this.currentYear
      const month = this.currentMonth - 1 // JavaScript月份从0开始
      
      // 当月第一天
      const firstDay = new Date(year, month, 1)
      const firstDayWeek = firstDay.getDay()
      
      // 当月最后一天
      const lastDay = new Date(year, month + 1, 0)
      const lastDate = lastDay.getDate()
      
      // 上月最后几天（补齐第一行）
      const prevMonth = month === 0 ? 11 : month - 1
      const prevYear = month === 0 ? year - 1 : year
      const prevLastDay = new Date(prevYear, prevMonth + 1, 0).getDate()
      
      for (let i = firstDayWeek - 1; i >= 0; i--) {
        const day = prevLastDay - i
        dates.push({
          key: `prev-${day}`,
          day,
          date: new Date(prevYear, prevMonth, day),
          isOtherMonth: true,
          isToday: false,
          isSelected: false,
          isDisabled: false
        })
      }
      
      // 当月所有天
      for (let day = 1; day <= lastDate; day++) {
        const date = new Date(year, month, day)
        const isToday = this.isSameDay(date, this.today)
        const isSelected = this.selectedDate && this.isSameDay(date, this.selectedDate)
        
        dates.push({
          key: `current-${day}`,
          day,
          date,
          isOtherMonth: false,
          isToday,
          isSelected,
          isDisabled: false
        })
      }
      
      // 下月前几天（补齐最后一行）
      const nextMonth = month === 11 ? 0 : month + 1
      const nextYear = month === 11 ? year + 1 : year
      const remainingDays = 42 - dates.length // 6行7列 = 42个格子
      
      for (let day = 1; day <= remainingDays; day++) {
        dates.push({
          key: `next-${day}`,
          day,
          date: new Date(nextYear, nextMonth, day),
          isOtherMonth: true,
          isToday: false,
          isSelected: false,
          isDisabled: false
        })
      }
      
      return dates
    }
  },
  
  watch: {
    visible(newVal) {
      if (newVal) {
        this.initCalendar()
      }
    },
    
    defaultDate(newVal) {
      if (newVal) {
        this.initCalendar()
      }
    }
  },
  
  methods: {
    // 初始化日历
    initCalendar() {
      if (this.defaultDate) {
        const date = new Date(this.defaultDate)
        this.selectedDate = date
        this.currentYear = date.getFullYear()
        this.currentMonth = date.getMonth() + 1
      } else {
        this.selectedDate = new Date()
        this.currentYear = this.today.getFullYear()
        this.currentMonth = this.today.getMonth() + 1
      }
    },
    
    // 判断是否为同一天
    isSameDay(date1, date2) {
      return date1.getFullYear() === date2.getFullYear() &&
             date1.getMonth() === date2.getMonth() &&
             date1.getDate() === date2.getDate()
    },
    
    // 上一个月
    previousMonth() {
      if (this.currentMonth === 1) {
        this.currentMonth = 12
        this.currentYear -= 1
      } else {
        this.currentMonth -= 1
      }
    },
    
    // 下一个月
    nextMonth() {
      if (this.currentMonth === 12) {
        this.currentMonth = 1
        this.currentYear += 1
      } else {
        this.currentMonth += 1
      }
    },
    
    // 选择日期
    selectDate(dateItem) {
      if (dateItem.isDisabled) return
      
      this.selectedDate = new Date(dateItem.date)
      
      // 如果点击的是其他月份的日期，切换到对应月份
      if (dateItem.isOtherMonth) {
        this.currentYear = dateItem.date.getFullYear()
        this.currentMonth = dateItem.date.getMonth() + 1
      }
      
      // 添加触觉反馈（如果支持）
      try {
        uni.vibrateShort({
          type: 'light'
        })
      } catch (e) {
        // 忽略不支持的平台
      }
    },
    
    // 选择今天
    selectToday() {
      this.selectedDate = new Date(this.today)
      this.currentYear = this.today.getFullYear()
      this.currentMonth = this.today.getMonth() + 1
    },
    
    // 选择昨天
    selectYesterday() {
      this.selectedDate = new Date(this.yesterday)
      this.currentYear = this.yesterday.getFullYear()
      this.currentMonth = this.yesterday.getMonth() + 1
    },
    
    // 选择前天
    selectDayBeforeYesterday() {
      this.selectedDate = new Date(this.dayBeforeYesterday)
      this.currentYear = this.dayBeforeYesterday.getFullYear()
      this.currentMonth = this.dayBeforeYesterday.getMonth() + 1
    },
    
    // 格式化短日期（用于快捷按钮）
    formatShortDate(date) {
      const month = date.getMonth() + 1
      const day = date.getDate()
      return `${month}.${day}`
    },
    
    // 格式化选中的日期显示
    formatSelectedDate(date) {
      if (!date) return ''
      
      const year = date.getFullYear()
      const month = date.getMonth() + 1
      const day = date.getDate()
      const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
      const weekDay = weekDays[date.getDay()]
      
      return `${year}年${month}月${day}日 ${weekDay}`
    },
    
    // 确认选择
    confirmSelection() {
      if (this.selectedDate) {
        this.$emit('confirm', this.selectedDate)
      }
      this.closeCalendar()
    },
    
    // 关闭日历
    closeCalendar() {
      this.$emit('close')
    }
  }
}
</script>

<style scoped>
.calendar-picker {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
}

.calendar-mask {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(4rpx);
}

.calendar-container {
  position: relative;
  width: 680rpx;
  max-width: 90vw;
  background: #fff;
  border-radius: 24rpx;
  box-shadow: 0 16rpx 48rpx rgba(0, 0, 0, 0.15);
  overflow: hidden;
  animation: calendarSlideIn 0.3s ease-out;
}

@keyframes calendarSlideIn {
  from {
    opacity: 0;
    transform: scale(0.9) translateY(-20rpx);
  }
  to {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}

/* 头部样式 */
.calendar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 32rpx 40rpx;
  background: linear-gradient(135deg, #FF9A76 0%, #FFC5A6 100%);
  color: white;
}

.header-title {
  font-size: 36rpx;
  font-weight: 600;
}

.header-close {
  width: 48rpx;
  height: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 24rpx;
  background: rgba(255, 255, 255, 0.2);
}

.close-icon {
  font-size: 40rpx;
  line-height: 1;
  font-weight: 300;
}

/* 导航样式 */
.calendar-nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx 40rpx;
  border-bottom: 1px solid #f0f0f0;
}

.nav-btn {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 32rpx;
  background: #f8f9fa;
  transition: all 0.2s ease;
}

.nav-btn:active {
  background: #e9ecef;
  transform: scale(0.95);
}

.nav-icon {
  font-size: 32rpx;
  color: #666;
  font-weight: 600;
}

.nav-title {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.nav-year, .nav-month {
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
}

/* 星期标题 */
.calendar-weekdays {
  display: flex;
  padding: 16rpx 20rpx;
  background: #fafbfc;
  border-bottom: 1px solid #f0f0f0;
}

.weekday-item {
  flex: 1;
  text-align: center;
  font-size: 24rpx;
  color: #999;
  font-weight: 500;
}

/* 日期网格 */
.calendar-dates {
  display: flex;
  flex-wrap: wrap;
  padding: 20rpx;
}

.date-item {
  width: calc(100% / 7);
  height: 80rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  border-radius: 40rpx;
  margin: 4rpx 0;
  transition: all 0.2s ease;
}

.date-text {
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
  line-height: 1;
}

/* 日期状态样式 */
.date-other-month .date-text {
  color: #ccc;
}

.date-today {
  background: rgba(255, 154, 118, 0.1);
  border: 2rpx solid #FF9A76;
}

.date-today .date-text {
  color: #FF9A76;
  font-weight: 600;
}

.date-selected {
  background: linear-gradient(135deg, #FF9A76 0%, #FFC5A6 100%);
  transform: scale(1.05);
}

.date-selected .date-text {
  color: white;
  font-weight: 600;
}

.date-disabled {
  opacity: 0.3;
  pointer-events: none;
}

/* 快捷操作 */
.calendar-shortcuts {
  display: flex;
  gap: 24rpx;
  padding: 32rpx 40rpx;
  border-top: 1px solid #f0f0f0;
  justify-content: center;
}

.shortcut-btn {
  padding: 16rpx 24rpx;
  background: #f8f9fa;
  border-radius: 20rpx;
  transition: all 0.2s ease;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4rpx;
  min-width: 120rpx;
}

.shortcut-btn:active {
  background: #e9ecef;
  transform: scale(0.95);
}

.shortcut-text {
  font-size: 26rpx;
  color: #333;
  font-weight: 500;
}

.shortcut-date {
  font-size: 20rpx;
  color: #999;
  font-weight: 400;
}

/* 选中日期信息 */
.selected-date-info {
  padding: 24rpx 40rpx;
  border-top: 1px solid #f0f0f0;
  background: #fafbfc;
  text-align: center;
}

.selected-label {
  font-size: 24rpx;
  color: #999;
  margin-right: 8rpx;
}

.selected-value {
  font-size: 28rpx;
  color: #FF9A76;
  font-weight: 600;
}

/* 底部按钮 */
.calendar-footer {
  display: flex;
  gap: 24rpx;
  padding: 32rpx 40rpx;
  border-top: 1px solid #f0f0f0;
}

.footer-btn {
  flex: 1;
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 44rpx;
  font-size: 32rpx;
  font-weight: 600;
  transition: all 0.2s ease;
}

.cancel-btn {
  background: #f8f9fa;
  color: #666;
}

.cancel-btn:active {
  background: #e9ecef;
}

.confirm-btn {
  background: linear-gradient(135deg, #FF9A76 0%, #FFC5A6 100%);
  color: white;
  box-shadow: 0 4rpx 16rpx rgba(255, 154, 118, 0.3);
}

.confirm-btn:active {
  transform: scale(0.98);
  box-shadow: 0 2rpx 8rpx rgba(255, 154, 118, 0.4);
}

.confirm-disabled {
  opacity: 0.6;
  pointer-events: none;
}

/* 响应式适配 */
@media screen and (max-width: 750px) {
  .calendar-container {
    width: 95vw;
    border-radius: 20rpx;
  }
  
  .calendar-header {
    padding: 28rpx 32rpx;
  }
  
  .header-title {
    font-size: 32rpx;
  }
  
  .calendar-nav {
    padding: 20rpx 32rpx;
  }
  
  .nav-year, .nav-month {
    font-size: 28rpx;
  }
  
  .date-item {
    height: 72rpx;
  }
  
  .date-text {
    font-size: 26rpx;
  }
}
</style>