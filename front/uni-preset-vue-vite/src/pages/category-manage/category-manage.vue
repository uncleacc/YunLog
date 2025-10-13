<template>
  <view class="container">
    <view class="tip-card">
      <text class="tip-text">长按分类可以编辑或删除</text>
    </view>

    <!-- 分类列表 -->
    <view class="category-list">
      <view
        class="category-item"
        v-for="item in categoryList"
        :key="item.id"
        @longpress="ShowCategoryMenu(item)"
      >
        <view class="category-content">
          <input
            v-if="editingId === item.id"
            class="category-name-input"
            v-model="editingName"
            @confirm="SaveCategoryName(item.id)"
            @blur="CancelEdit"
            :focus="true"
          />
          <view v-else class="category-display">
            <view class="category-icon-wrapper" :style="{ backgroundColor: item.color }">
              <text class="category-icon">{{ item.icon }}</text>
            </view>
            <text class="category-name">{{ item.name }}</text>
            <text v-if="item.id === 'default'" class="default-badge">默认</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 添加分类按钮 -->
    <view class="add-category-btn" @click="ShowAddDialog">
      <text class="add-icon">+</text>
      <text class="add-text">添加新分类</text>
    </view>

    <!-- 添加/编辑分类弹窗 -->
    <view class="dialog-mask" v-if="showDialog" @click="CloseDialog">
      <view class="dialog-content" @click.stop>
        <view class="dialog-header">
          <text class="dialog-title">{{ dialogMode === 'add' ? '添加分类' : '编辑分类' }}</text>
        </view>
        
        <view class="dialog-body">
          <!-- 分类名称 -->
          <view class="form-item">
            <text class="form-label">分类名称</text>
            <input
              class="form-input"
              v-model="formData.name"
              placeholder="请输入分类名称"
              maxlength="10"
            />
          </view>

          <!-- 选择图标 -->
          <view class="form-item">
            <text class="form-label">选择图标</text>
            <view class="icon-grid">
              <view
                class="icon-option"
                :class="{ active: formData.icon === icon }"
                v-for="icon in iconList"
                :key="icon"
                @click="SelectIcon(icon)"
              >
                <text class="icon-char">{{ icon }}</text>
              </view>
            </view>
          </view>

          <!-- 选择颜色 -->
          <view class="form-item">
            <text class="form-label">选择颜色</text>
            <view class="color-grid">
              <view
                class="color-option"
                :class="{ active: formData.color === color }"
                v-for="color in colorList"
                :key="color"
                :style="{ backgroundColor: color }"
                @click="SelectColor(color)"
              >
                <text v-if="formData.color === color" class="check-mark">✓</text>
              </view>
            </view>
          </view>
        </view>

        <view class="dialog-footer">
          <view class="dialog-btn cancel" @click="CloseDialog">
            <text class="btn-text">取消</text>
          </view>
          <view class="dialog-btn confirm" @click="SubmitForm">
            <text class="btn-text">确定</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import storage from '@/utils/storage.js'

export default {
  data() {
    return {
      categoryList: [],
      editingId: '',
      editingName: '',
      showDialog: false,
      dialogMode: 'add', // 'add' 或 'edit'
      currentCategory: null,
      formData: {
        name: '',
        icon: '📁',
        color: '#FF9A76',
      },
      iconList: ['📁', '📝', '💼', '🎨', '🎵', '🏃', '💡', '🌟', '❤️', '🎯', '📚', '✈️', '🍕', '☕', '🎮', '📷'],
      colorList: ['#FF9A76', '#FF6B6B', '#4ECDC4', '#45B7D1', '#FFA07A', '#98D8C8', '#F7DC6F', '#BB8FCE', '#85C1E2', '#F8B88B'],
    }
  },
  onShow() {
    this.LoadCategories()
  },
  methods: {
    LoadCategories() {
      this.categoryList = storage.GetCategoryList()
    },

    ShowCategoryMenu(category) {
      const itemList = category.id === 'default' 
        ? ['编辑名称'] 
        : ['编辑名称', '修改图标和颜色', '删除分类']
      
      uni.showActionSheet({
        itemList,
        success: (res) => {
          if (res.tapIndex === 0) {
            // 编辑名称
            this.editingId = category.id
            this.editingName = category.name
          } else if (res.tapIndex === 1) {
            // 修改图标和颜色
            this.ShowEditDialog(category)
          } else if (res.tapIndex === 2) {
            // 删除分类
            this.ConfirmDelete(category)
          }
        },
      })
    },

    SaveCategoryName(id) {
      if (!this.editingName.trim()) {
        uni.showToast({
          title: '名称不能为空',
          icon: 'none',
        })
        return
      }

      const result = storage.UpdateCategory(id, { name: this.editingName })
      if (result) {
        uni.showToast({
          title: '修改成功',
          icon: 'success',
        })
        this.LoadCategories()
      }
      this.CancelEdit()
    },

    CancelEdit() {
      this.editingId = ''
      this.editingName = ''
    },

    ShowAddDialog() {
      this.dialogMode = 'add'
      this.formData = {
        name: '',
        icon: '📁',
        color: '#FF9A76',
      }
      this.showDialog = true
    },

    ShowEditDialog(category) {
      this.dialogMode = 'edit'
      this.currentCategory = category
      this.formData = {
        name: category.name,
        icon: category.icon,
        color: category.color,
      }
      this.showDialog = true
    },

    CloseDialog() {
      this.showDialog = false
      this.currentCategory = null
    },

    SelectIcon(icon) {
      this.formData.icon = icon
    },

    SelectColor(color) {
      this.formData.color = color
    },

    SubmitForm() {
      if (!this.formData.name.trim()) {
        uni.showToast({
          title: '请输入分类名称',
          icon: 'none',
        })
        return
      }

      if (this.dialogMode === 'add') {
        const result = storage.AddCategory(this.formData)
        if (result) {
          uni.showToast({
            title: '添加成功',
            icon: 'success',
          })
          this.LoadCategories()
          this.CloseDialog()
        } else {
          uni.showToast({
            title: '添加失败',
            icon: 'none',
          })
        }
      } else {
        const result = storage.UpdateCategory(this.currentCategory.id, this.formData)
        if (result) {
          uni.showToast({
            title: '修改成功',
            icon: 'success',
          })
          this.LoadCategories()
          this.CloseDialog()
        } else {
          uni.showToast({
            title: '修改失败',
            icon: 'none',
          })
        }
      }
    },

    ConfirmDelete(category) {
      uni.showModal({
        title: '删除分类',
        content: `确定要删除"${category.name}"吗？该分类下的日记将移到默认分类。`,
        confirmColor: '#FF6B6B',
        success: (res) => {
          if (res.confirm) {
            this.DeleteCategory(category.id)
          }
        },
      })
    },

    DeleteCategory(id) {
      const success = storage.DeleteCategory(id)
      if (success) {
        uni.showToast({
          title: '删除成功',
          icon: 'success',
        })
        this.LoadCategories()
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
  background-color: #fff5f0;
  padding: 32rpx;
  padding-bottom: 160rpx;
}

.tip-card {
  background: linear-gradient(135deg, #ffd5a0 0%, #ffb76b 100%);
  padding: 24rpx 32rpx;
  border-radius: 24rpx;
  margin-bottom: 32rpx;
}

.tip-text {
  font-size: 28rpx;
  color: #8b4513;
  text-align: center;
  display: block;
}

.category-list {
  margin-bottom: 32rpx;
}

.category-item {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 32rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(255, 154, 118, 0.1);
}

.category-content {
  display: flex;
  align-items: center;
}

.category-name-input {
  flex: 1;
  font-size: 36rpx;
  color: #333333;
  font-weight: bold;
}

.category-display {
  display: flex;
  align-items: center;
  gap: 24rpx;
  width: 100%;
}

.category-icon-wrapper {
  width: 80rpx;
  height: 80rpx;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.category-icon {
  font-size: 48rpx;
}

.category-name {
  flex: 1;
  font-size: 36rpx;
  font-weight: bold;
  color: #333333;
}

.default-badge {
  padding: 8rpx 16rpx;
  background: #ffe5d8;
  color: #ff9a76;
  font-size: 24rpx;
  border-radius: 12rpx;
}

.add-category-btn {
  background: linear-gradient(135deg, #ff9a76 0%, #ff7e5f 100%);
  padding: 32rpx;
  border-radius: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16rpx;
  box-shadow: 0 4rpx 16rpx rgba(255, 126, 95, 0.3);
}

.add-category-btn:active {
  transform: scale(0.98);
  opacity: 0.9;
}

.add-icon {
  font-size: 48rpx;
  color: #ffffff;
  font-weight: 300;
}

.add-text {
  font-size: 32rpx;
  color: #ffffff;
  font-weight: 500;
}

/* 弹窗样式 */
.dialog-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.dialog-content {
  width: 600rpx;
  background: #ffffff;
  border-radius: 24rpx;
  overflow: hidden;
}

.dialog-header {
  padding: 40rpx 32rpx 24rpx;
  border-bottom: 1rpx solid #f5f5f5;
}

.dialog-title {
  font-size: 36rpx;
  font-weight: bold;
  color: #333333;
  text-align: center;
  display: block;
}

.dialog-body {
  padding: 32rpx;
  max-height: 800rpx;
  overflow-y: scroll;
}

.form-item {
  margin-bottom: 32rpx;
}

.form-item:last-child {
  margin-bottom: 0;
}

.form-label {
  font-size: 28rpx;
  color: #666666;
  margin-bottom: 16rpx;
  display: block;
}

.form-input {
  width: 100%;
  height: 80rpx;
  background: #f5f5f5;
  border-radius: 12rpx;
  padding: 0 24rpx;
  font-size: 32rpx;
  color: #333333;
}

.icon-grid,
.color-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}

.icon-option {
  width: 80rpx;
  height: 80rpx;
  background: #f5f5f5;
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2rpx solid transparent;
}

.icon-option.active {
  background: #ffe5d8;
  border-color: #ff9a76;
}

.icon-char {
  font-size: 48rpx;
}

.color-option {
  width: 80rpx;
  height: 80rpx;
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2rpx solid transparent;
}

.color-option.active {
  border-color: #333333;
}

.check-mark {
  font-size: 40rpx;
  color: #ffffff;
  font-weight: bold;
}

.dialog-footer {
  display: flex;
  border-top: 1rpx solid #f5f5f5;
}

.dialog-btn {
  flex: 1;
  height: 96rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.dialog-btn.cancel {
  border-right: 1rpx solid #f5f5f5;
}

.dialog-btn.confirm .btn-text {
  color: #ff9a76;
  font-weight: 500;
}

.dialog-btn.cancel .btn-text {
  color: #999999;
}

.btn-text {
  font-size: 32rpx;
}
</style>

