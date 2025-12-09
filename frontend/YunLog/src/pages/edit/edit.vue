<template>
  <view class="edit-page">
    <!-- 顶部状态栏：左侧日期入口，右侧保存按钮 -->
    <view class="top-status-bar" id="top-status-bar">
      <view class="top-bar-left">
        <view class="top-date-card" @click="showDatePicker">
          <text class="date-day">{{ topDate.day }}</text>
          <view class="date-right">
            <text class="date-line1">{{ topDate.dateLine }}</text>
            <text class="date-line2">{{ topDate.timeLine }}</text>
          </view>
        </view>
      </view>
      <view class="top-bar-right">
        <view class="top-save-btn" @click="SaveDiary" :class="{ 'saving': isSaving }">
          <text class="top-save-text">{{ isSaving ? '保存中' : '保存' }}</text>
        </view>
      </view>
    </view>
    <!-- 内容区包裹：为顶部状态栏预留空间 -->
    <view 
      class="content-wrapper"
      :style="{ paddingTop: (topBarHeight && topBarHeight > 0) ? (topBarHeight + 'px') : '120rpx' }"
    >
      <!-- 附件预览区域（仅在有附件时显示） -->
      <view v-if="formData.attachments.length > 0" class="attachment-preview-area">
      <view 
        v-for="(item, index) in formData.attachments" 
        :key="index" 
        class="attachment-item"
      >
        <image 
          v-if="isImageFile(item.url)"
          :src="item.url" 
          mode="aspectFill"
          class="attachment-image"
          @click="handlePreviewImage(item.url, index)"
        />
        <view class="attachment-remove" @click="handleRemoveAttachment(index)">
          <text class="remove-icon">×</text>
        </view>
      </view>
      </view>

  <!-- 富文本编辑器 - 占满整个页面 -->
      <view 
      class="editor-container" 
      :style="{ 
        paddingBottom: (bottomAreaHeight && bottomAreaHeight > 0) ? (bottomAreaHeight + keyboardHeight) + 'px' : '200rpx'
      }"
    >
      <editor
        id="editor"
        class="editor-content"
        placeholder="记录此刻..."
        :show-img-size="true"
        :show-img-toolbar="true"
        :show-img-resize="true"
        :auto-focus="true"
        @ready="handleEditorReady"
        @input="handleEditorInput"
        @selectionchange="handleEditorSelectionChange"
        @statuschange="handleEditorStatusChange"
      ></editor>
      <!-- 字符数显示 - 编辑器右下角（随底部区域高度动态上移） -->
      <view 
        class="editor-char-count"
        :style="{ bottom: (bottomAreaHeight && bottomAreaHeight > 0) ? (bottomAreaHeight + keyboardHeight + 16) + 'px' : '216rpx' }"
      >
        <text class="char-count-text">{{ contentLength }}/3000</text>
      </view>
      </view>
    </view>

    <!-- 底部区域：仅包含工具栏，整体固定在底部，随键盘上移 -->
  <view class="bottom-area" id="bottom-area" :style="{ bottom: keyboardHeight + 'px', paddingBottom: keyboardHeight > 0 ? '0px' : 'env(safe-area-inset-bottom)' }">
      <!-- 底部工具栏 -->
      <view class="bottom-toolbar">
      <!-- 左侧格式工具 -->
      <view class="toolbar-left">
        <view 
          class="tool-btn" 
          :class="{ 'tool-btn-active': formatStates.bold }"
          @click="handleToggleFormat('bold')"
        >
          <text class="tool-text" style="font-weight: bold">B</text>
        </view>
        <view 
          class="tool-btn" 
          :class="{ 'tool-btn-active': formatStates.italic }"
          @click="handleToggleFormat('italic')"
        >
          <text class="tool-text" style="font-style: italic">I</text>
        </view>
        <view 
          class="tool-btn" 
          :class="{ 'tool-btn-active': formatStates.underline }"
          @click="handleToggleFormat('underline')"
        >
          <text class="tool-text" style="text-decoration: underline">U</text>
        </view>
        <view 
          class="tool-btn" 
          :class="{ 'tool-btn-active': formatStates.strikethrough }"
          @click="handleToggleFormat('strikethrough')"
        >
          <text class="tool-text" style="text-decoration: line-through">S</text>
        </view>
        <view 
          class="tool-btn" 
          :class="{ 'tool-btn-active': formatStates.listOrdered }"
          @click="handleToggleList('ordered')"
        >
          <text class="tool-text">1.</text>
        </view>
        <view 
          class="tool-btn" 
          :class="{ 'tool-btn-active': formatStates.listBullet }"
          @click="handleToggleList('bullet')"
        >
          <text class="tool-text">•</text>
        </view>
      </view>

      <!-- 右侧功能按钮 -->
      <view class="toolbar-right">
        <!-- 附件 -->
        <view class="tool-btn" @click="handleAddAttachment">
          <image src="/static/tabbar/attachment.png" class="tool-icon-img" mode="aspectFit" />
        </view>
      </view>
      </view>
    </view>

    <!-- 日历选择器组件 -->
    <CalendarPicker 
      :visible="showCalendar"
      :default-date="currentDiaryInfo ? currentDiaryInfo.createTime : ''"
      @confirm="handleDateSelected"
      @close="hideCalendar"
    />
  </view>
</template>

<script>
import CalendarPicker from './components/CalendarPicker.vue'

import { useEditorFormat } from './hooks/useEditorFormat.js'
import { useEditorContent } from './hooks/useEditorContent.js'
import { useCategories } from './hooks/useCategories.js'
import { useDiarySave } from './hooks/useDiarySave.js'
import storage from '../../utils/storage.js'
import { validateEmojiContent, hasEmoji } from '../../utils/emojiUtils.js'
import api from '../../utils/api.js'
import { isImageFile, isVideoFile, parseDate, formatDateForBackend } from '../../utils/textUtils.js'
import { requireLogin } from '../../utils/auth.js'

export default {
  components: {
    CalendarPicker
  },
  
  setup() {
    // 使用编辑器格式管理 Hook
    const {
      formatStates,
      globalFormatStates,
      updateToolbarFromSelection,
      syncGlobalFormatsWithCursor,
      toggleFormat,
      toggleList,
      hasActiveGlobalFormats
    } = useEditorFormat()

    // 使用编辑器内容管理 Hook
    const {
      isEditing,
      currentDiary,
      editorCtx,
      onEditorSelectionChange,
      onEditorInput,
      onEditorFocus,
      onEditorBlur,
      onEditorReady,
      getEditorContent,
      setEditorContent,
      clearEditor,
      insertContent,
      setEditorContext,
      initEditor
    } = useEditorContent()

    // 使用分类管理 Hook
    const {
      categories,
      loadCategories,
      getCategoryName,
      getCategoryColor,
      categoryExists
    } = useCategories()

    // 使用日记保存管理 Hook
    const {
      isSaving,
      saveNewDiary,
      updateExistingDiary,
      autoSave,
      hasContentChanged
    } = useDiarySave()

    return {
      // 格式状态
      formatStates,
      globalFormatStates,
      
      // 编辑器状态
      isEditing,
      currentDiary,
      editorCtx,
      
      // 分类状态
      categories,
      
      // 保存状态
      isSaving,
      
      // 方法
      updateToolbarFromSelection,
      syncGlobalFormatsWithCursor,
      toggleFormat,
      toggleList,
      hasActiveGlobalFormats,
      onEditorSelectionChange,
      onEditorInput,
      onEditorFocus,
      onEditorBlur,
      onEditorReady,
      getEditorContent,
      setEditorContent,
      clearEditor,
      insertContent,
      setEditorContext,
      initEditor,
      loadCategories,
      getCategoryName,
      getCategoryColor,
      categoryExists,
      saveNewDiary,
      updateExistingDiary,
      autoSave,
      hasContentChanged
    }
  },

  computed: {
    // 顶部日期卡片展示数据
    topDate() {
      try {
        const src = (this.currentDiaryInfo && this.currentDiaryInfo.createTime)
          ? parseDate(this.currentDiaryInfo.createTime)
          : new Date()
        const dd = String(src.getDate()).padStart(2, '0')
        const yyyy = src.getFullYear()
        const mm = String(src.getMonth() + 1).padStart(2, '0')
        const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
        const week = weekDays[src.getDay()]
        return {
          day: dd,
          dateLine: `${yyyy}年${mm}月`,
          timeLine: `${week}`
        }
      } catch (e) {
        return { day: '--', dateLine: '--/-- 周-', timeLine: '--:--' }
      }
    }
  },

  data() {
    return {
      diaryId: '',
      categoryId: '',
      formData: {
        content: '',
        contentHtml: '',
        attachments: [],
        categoryId: '',
      },
      contentLength: 0,
      hasEdited: false,
      currentDiaryInfo: null,
      showCalendar: false,
      editorCtx: null,
      // 动态计算底部区域高度（工具栏 + 保存按钮 + 安全区）
      bottomAreaHeight: 0,
      keyboardHeight: 0,
      topBarHeight: 0,
    }
  },

  onLoad(options) {
    
    // 检查登录状态
    if (!requireLogin()) {
      return
    }
    
    // 加载分类数据
    this.loadCategories()
    
    // 如果有id参数，表示是编辑模式
    if (options.id) {
      this.isEditing = true
      this.diaryId = options.id
      this.LoadDiary()
    }
    
    // 如果有categoryId参数，表示从分类页面创建新日记
    if (options.categoryId) {
      this.categoryId = options.categoryId
      this.formData.categoryId = options.categoryId
    }
  },
  onReady() {
    // 获取编辑器上下文
    this.editorCtx = uni.createSelectorQuery().select('#editor').context((res) => {
      this.editorCtx = res.context
      
      // 如果是编辑模式且有内容，设置内容
      if (this.isEditing && this.formData.contentHtml) {
        this.editorCtx.setContents({
          html: this.formData.contentHtml
        })
      }
    }).exec()

    // 下一帧测量底部区域高度，确保编辑器底部留白准确
    this.$nextTick(() => {
      setTimeout(() => {
        this.measureBottomArea()
        this.measureTopBar()
      }, 50)
    })

    // 监听键盘高度变化（小程序/App/H5 兼容以平台为准）
    try {
      if (uni.onKeyboardHeightChange) {
        this.__keyboardHandler = (res) => {
          const h = (res && typeof res.height === 'number') ? res.height : 0
          this.keyboardHeight = h
          // 键盘高度变化后，重新测量底部区域高度，确保留白准确
          this.$nextTick(() => setTimeout(() => this.measureBottomArea(), 50))
        }
        uni.onKeyboardHeightChange(this.__keyboardHandler)
      }
    } catch (e) {
      console.warn('注册键盘高度监听失败:', e)
    }
  },

  onShow() {
    // 页面显示时再次测量，避免初始高度为0导致遮挡
    setTimeout(() => {
      this.measureBottomArea()
      this.measureTopBar()
    }, 50)
  },

  onResize() {
    // 屏幕旋转或窗口尺寸变动时重新测量
    this.measureBottomArea()
    this.measureTopBar()
  },

  onHide() {
    // 页面隐藏时，键盘会收起，延时清理高度
    setTimeout(() => { this.keyboardHeight = 0 }, 100)
  },

  onUnload() {
    // 解绑键盘监听
    try {
      if (uni.offKeyboardHeightChange && this.__keyboardHandler) {
        uni.offKeyboardHeightChange(this.__keyboardHandler)
      }
    } catch (e) {}
  },

  methods: {
    // === 工具函数 ===
    isImageFile,
    
    // 测量底部区域高度（工具栏 + 保存按钮 + 安全区），用于为编辑器预留空间
    measureBottomArea() {
      try {
        uni.createSelectorQuery()
          .select('#bottom-area')
          .boundingClientRect((rect) => {
            if (rect && rect.height) {
              this.bottomAreaHeight = rect.height
            }
          })
          .exec()
      } catch (e) {
        console.warn('measureBottomArea 失败:', e)
      }
    },

    // 测量顶部状态栏高度
    measureTopBar() {
      try {
        uni.createSelectorQuery()
          .select('#top-status-bar')
          .boundingClientRect((rect) => {
            if (rect && rect.height) {
              this.topBarHeight = rect.height
            }
          })
          .exec()
      } catch (e) {
        console.warn('measureTopBar 失败:', e)
      }
    },

    // 顶部状态栏日期展示文案（DD/MM 周二）
    getTopBarDateText() {
      try {
        const dateSrc = (this.currentDiaryInfo && this.currentDiaryInfo.createTime)
          ? parseDate(this.currentDiaryInfo.createTime)
          : new Date()
        const dd = String(dateSrc.getDate()).padStart(2, '0')
        const mm = String(dateSrc.getMonth() + 1).padStart(2, '0')
        const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
        const week = weekDays[dateSrc.getDay()]
        return `${dd}/${mm} ${week}`
      } catch (e) {
        return '--/-- 周--'
      }
    },
    
    // === 编辑器事件处理 ===
    
    // 处理编辑器就绪
    handleEditorReady(e) {
      uni.createSelectorQuery()
        .select('#editor')
        .context((res) => {
          this.editorCtx = res.context
          
          // 如果是编辑模式且有内容，设置内容
          if (this.isEditing && this.formData.contentHtml) {
            setTimeout(() => {
              this.editorCtx.setContents({
                html: this.formData.contentHtml
              })
            }, 100)
          }
        })
        .exec()
    },
    
    // 处理编辑器输入
    handleEditorInput(e) {
      this.hasEdited = true
      if (e.detail && e.detail.text) {
        this.contentLength = e.detail.text.length
        this.formData.content = e.detail.text
        
        // 限制字符数
        if (this.contentLength > 3000) {
          uni.showToast({
            title: '已达到字符数上限',
            icon: 'none'
          })
        }
      }
    },
    
    // 处理编辑器选择变化
    handleEditorSelectionChange(e) {
    },
    
    // 处理编辑器状态变化
    handleEditorStatusChange(e) {
      const formats = e.detail
      if (formats) {
        this.formatStates.bold = !!formats.bold
        this.formatStates.italic = !!formats.italic
        this.formatStates.underline = !!formats.underline
        this.formatStates.strikethrough = !!(formats.strike || formats.strikeThrough)
        this.formatStates.listOrdered = formats.list === 'ordered'
        this.formatStates.listBullet = formats.list === 'bullet'
      }
    },
    
    // === 格式工具栏操作 ===
    
    // 处理格式切换
    handleToggleFormat(format) {
      if (!this.editorCtx) return
      
      const formatMap = {
        bold: 'bold',
        italic: 'italic',
        underline: 'underline',
        strikethrough: 'strike'
      }
      
      const editorFormatName = formatMap[format]
      if (editorFormatName && this.editorCtx.format) {
        this.editorCtx.format(editorFormatName)
      }
    },

        // 处理列表切换
    handleToggleList(listType) {
      if (!this.editorCtx || !this.editorCtx.format) return
      this.editorCtx.format('list', listType)
    },

    // === 附件管理 ===
    
    // 获取编辑器内容（Promise 封装）
    getEditorContent() {
      return new Promise((resolve) => {
        if (!this.editorCtx || !this.editorCtx.getContents) {
          resolve({ text: this.formData.content, html: this.formData.contentHtml })
          return
        }
        
        this.editorCtx.getContents({
          success: (res) => {
            resolve({
              text: res.text || '',
              html: res.html || ''
            })
          },
          fail: () => {
            resolve({ text: this.formData.content, html: this.formData.contentHtml })
          }
        })
      })
    },

    // 处理添加附件
    handleAddAttachment() {
      uni.showActionSheet({
        itemList: ['拍照', '从相册选择'],
        success: (res) => {
          if (res.tapIndex === 0) {
            // 拍照
            uni.chooseImage({
              count: 1,
              sourceType: ['camera'],
              success: (res) => {
                const tempFilePath = res.tempFilePaths[0]
                this.addImageAttachment(tempFilePath)
              },
              fail: (err) => {
                console.error('拍照失败:', err)
                uni.showToast({ title: '拍照失败', icon: 'none' })
              }
            })
          } else if (res.tapIndex === 1) {
            // 从相册选择
            uni.chooseImage({
              count: 9,
              sourceType: ['album'],
              success: (res) => {
                // 逐个处理图片上传，避免并发过多
                res.tempFilePaths.forEach((tempFilePath, index) => {
                  // 稍微延迟以避免同时发起太多上传请求
                  setTimeout(() => {
                    this.addImageAttachment(tempFilePath)
                  }, index * 100)
                })
              },
              fail: (err) => {
                console.error('选择图片失败:', err)
                uni.showToast({ title: '选择图片失败', icon: 'none' })
              }
            })
          }
        }
      })
    },

    // 添加图片附件 - 立即上传到OSS
    async addImageAttachment(tempFilePath) {
      try {
        
        // 创建临时附件对象（用于显示）
        const tempAttachment = {
          id: Date.now() + Math.random(),
          type: 'image',
          url: tempFilePath,
          name: `image_${Date.now()}.jpg`,
          size: 0,
          createTime: new Date().toISOString(),
          uploading: true // 标记为上传中
        }
        
        // 先添加到列表显示上传状态
        this.formData.attachments.push(tempAttachment)
        const attachmentIndex = this.formData.attachments.length - 1
        
        // 上传临时图片到OSS
        const uploadResult = await api.uploadTempImage(tempFilePath)
        
        // 更新附件对象
        this.formData.attachments[attachmentIndex] = {
          ...tempAttachment,
          url: uploadResult.url, // 使用OSS返回的URL
          uploading: false
        }
        
        uni.showToast({
          title: '图片上传成功',
          icon: 'success',
          duration: 1500
        })
        
      } catch (error) {
        console.error('addImageAttachment - 上传失败:', error)
        
        // 上传失败，从列表中移除
        const failedIndex = this.formData.attachments.findIndex(item => 
          item.url === tempFilePath && item.uploading)
        if (failedIndex >= 0) {
          this.formData.attachments.splice(failedIndex, 1)
        }
        
        uni.showToast({
          title: error.message || '上传失败',
          icon: 'none',
          duration: 2000
        })
      }
    },

    // 处理移除附件
    async handleRemoveAttachment(index) {
      const attachment = this.formData.attachments[index]
      if (!attachment) return
      
      // 如果正在上传，直接移除
      if (attachment.uploading) {
        this.formData.attachments.splice(index, 1)
        return
      }
      
      try {
        // 如果附件有ID，说明是从数据库加载的，需要删除数据库记录
        if (attachment.id) {
          await api.deleteAttachment(attachment.id)
        }
        
        // 如果是已上传到OSS的文件，尝试删除OSS文件
        if (attachment.url && !attachment.url.startsWith('wxfile://') && !attachment.url.startsWith('blob:')) {
          await api.deleteOssFile(attachment.url)
        }
      } catch (error) {
        console.warn('handleRemoveAttachment - 删除失败:', error)
        // 如果是数据库删除失败，应该提示用户
        if (attachment.id) {
          uni.showToast({
            title: '删除失败，请重试',
            icon: 'none',
            duration: 2000
          })
          return
        }
        // OSS文件删除失败不影响从列表中移除
      }
      
      // 从列表中移除
      this.formData.attachments.splice(index, 1)
      
      uni.showToast({
        title: '附件已移除',
        icon: 'success',
        duration: 1000
      })
    },

    // 处理图片预览
    handlePreviewImage(url, index) {
      // 检查URL是否有效
      if (!url) {
        uni.showToast({
          title: '图片信息无效',
          icon: 'none'
        })
        return
      }

      // 只预览图片类型的附件
      const imageUrls = this.formData.attachments
        .filter(item => item && item.url && isImageFile(item.url))
        .map(item => item.url)
      
      if (imageUrls.length === 0) {
        uni.showToast({
          title: '没有可预览的图片',
          icon: 'none'
        })
        return
      }

      const currentIndex = imageUrls.indexOf(url)
      
      uni.previewImage({
        urls: imageUrls,
        current: currentIndex >= 0 ? currentIndex : 0
      })
    },

    // 加载日记数据（编辑模式）
    async LoadDiary() {
      if (!this.diaryId) {
        console.error('LoadDiary - 缺少日记ID')
        return
      }

      try {
        
        // 确保 diaryId 是数字类型
        const numericId = parseInt(this.diaryId, 10)
        if (isNaN(numericId)) {
          console.error('LoadDiary - 日记ID格式错误:', this.diaryId)
          uni.showToast({ title: '日记ID格式错误', icon: 'none' })
          return
        }

        // 从后端获取日记详情
        const diary = await api.getDiaryDetail(numericId)

        // 获取附件列表
        let attachments = []
        try {
          attachments = await api.getAttachmentsByDiary(numericId)
          
          // 过滤掉无效的附件对象，并标记已存在的附件
          attachments = (attachments || [])
            .filter(att => att && att.url)
            .map(att => ({
              ...att,
              existsInDb: true // 标记为已存在于数据库中
            }))
        } catch (attErr) {
          console.warn('获取附件失败:', attErr)
          attachments = []
        }

        // 保存完整的日记信息用于显示时间
        this.currentDiaryInfo = {
          ...diary
        }

        // 设置表单数据
        this.formData = {
          content: diary.content || '',
          contentHtml: diary.contentHtml || '',
          attachments: attachments || [],
          categoryId: diary.categoryId || 'default',
        }
        
          id: att.id,
          url: att.url ? att.url.substring(att.url.length - 20) : 'no-url',
          existsInDb: att.existsInDb
        })))
        
        this.contentLength = diary.content ? diary.content.length : 0
        
          contentLength: this.contentLength,
          hasContentHtml: !!this.formData.contentHtml,
          attachmentsCount: this.formData.attachments.length,
          categoryId: this.formData.categoryId
        })
        
        // 如果编辑器已准备好且有内容，则设置内容
        if (this.editorCtx && diary.contentHtml) {
          this.setEditorContent(diary.contentHtml)
        }
      } catch (error) {
        console.error('❌ 编辑页面加载日记失败:', error)
        uni.showToast({ 
          title: error.message || '加载日记失败', 
          icon: 'none' 
        })
        // 可选择返回上一页
        setTimeout(() => {
          uni.navigateBack()
        }, 1500)
      }
    },

    // 使用后端API的保存方法 
    async SaveDiary() {
      if (this.contentLength === 0 && !this.formData.content) {
        uni.showToast({ title: '请输入内容', icon: 'none' })
        return
      }

      try {
        uni.showLoading({ title: '保存中...' })
        
        // 检查编辑器上下文
          hasEditorCtx: !!this.editorCtx,
          hasEdited: this.hasEdited,
          contentLength: this.contentLength
        })
        
        // 获取编辑器内容
        const editorContent = await this.getEditorContent()
        
        // 验证表情符号完整性
        const emojiValidation = validateEmojiContent(editorContent.html, editorContent.text)
        if (!emojiValidation.isValid) {
          console.warn('SaveDiary - 表情符号验证警告:', emojiValidation)
        } else if (emojiValidation.textEmojiCount > 0) {
            emojiCount: emojiValidation.textEmojiCount,
            emojis: emojiValidation.textEmojis
          })
        }
        
          text: editorContent.text ? editorContent.text.substring(0, 50) + '...' : 'null',
          html: editorContent.html ? editorContent.html.substring(0, 50) + '...' : 'null',
          textLength: editorContent.text ? editorContent.text.length : 0,
          htmlLength: editorContent.html ? editorContent.html.length : 0,
          hasEmojis: hasEmoji(editorContent.text || '')
        })
        
        // 更新 formData 的内容
        this.formData.content = editorContent.text || ''
        this.formData.contentHtml = editorContent.html || ''
        
          isEditing: this.isEditing,
          contentLength: this.formData.content.length,
          hasHtml: !!this.formData.contentHtml,
          attachmentsCount: this.formData.attachments.length,
          categoryId: this.formData.categoryId
        })
        
        // 准备保存的数据
        const saveData = {
          title: this.formData.title,
          content: this.formData.content,
          contentHtml: this.formData.contentHtml,
          categoryId: this.formData.categoryId || 'default'
        }
        
        let result
        if (this.isEditing) {
          // 编辑模式 - 更新现有日记
          const numericId = parseInt(this.diaryId, 10)
          result = await api.updateDiary(numericId, saveData)
        } else {
          // 新建模式 - 创建新日记
          result = await api.createDiary(saveData)
        }
        
        // 日记保存成功后，处理附件关联
        if (result && this.formData.attachments.length > 0) {
          
          try {
            // 过滤出需要创建数据库记录的新附件
            const newAttachments = this.formData.attachments.filter(att => 
              !att.uploading && // 不是上传中的
              !att.existsInDb && // 不是已存在于数据库的
              att.url && 
              !att.url.startsWith('wxfile://') && 
              !att.url.startsWith('blob:') &&
              !att.url.startsWith('http://tmp/'))
            
            
            if (newAttachments.length > 0) {
              // 获取日记ID
              const diaryId = this.isEditing ? parseInt(this.diaryId, 10) : result.id
              
              if (diaryId) {
                // 批量创建新附件记录
                const newAttachmentUrls = newAttachments.map(att => att.url)
                
                await api.batchCreateAttachments({
                  diaryId: diaryId,
                  urls: newAttachmentUrls
                })
              }
            } else {
            }
          } catch (attachmentError) {
            console.warn('SaveDiary - 附件关联失败:', attachmentError)
            // 附件关联失败不影响日记保存，但给用户提示
            uni.showToast({
              title: '附件关联失败，但日记已保存',
              icon: 'none',
              duration: 2000
            })
          }
        }
        
        uni.hideLoading()
        
        if (result) {
          uni.showToast({ title: '保存成功', icon: 'success' })
          setTimeout(() => {
            uni.navigateBack()
          }, 1000)
        } else {
          console.error('SaveDiary - 保存失败: 返回结果为空')
          uni.showToast({ title: '保存失败', icon: 'none' })
        }
      } catch (error) {
        console.error('SaveDiary - 保存异常:', error)
        uni.hideLoading()
        uni.showToast({ title: error.message || '保存失败', icon: 'none' })
      }
    },

    Cancel() {
      // 如果有未保存的内容，提示用户
      if (this.hasEdited && this.contentLength > 0) {
        uni.showModal({
          title: '提示',
          content: '有未保存的内容，确定要离开吗？',
          confirmColor: '#FF9A76',
          success: (res) => {
            if (res.confirm) {
              uni.navigateBack()
            }
          }
        })
      } else {
        uni.navigateBack()
      }
    },

    // === 时间相关方法 ===
    
    // 格式化日期为 YYYY年MM月DD日 格式
    formatDate(dateString) {
      if (!dateString) return '--'
      
      try {
        const date = parseDate(dateString)
        const year = date.getFullYear()
        const month = date.getMonth() + 1
        const day = date.getDate()
        const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
        const weekDay = weekDays[date.getDay()]
        
        return `${year}年${month}月${day}日 ${weekDay}`
      } catch (error) {
        console.error('formatDate - 日期格式化失败:', error, dateString)
        return '--'
      }
    },

    // 显示日期选择器
    showDatePicker() {
      this.showCalendar = true
    },

    // 隐藏日历选择器
    hideCalendar() {
      this.showCalendar = false
    },

    // 处理日历选择的日期
    handleDateSelected(selectedDate) {
      this.updateDiaryTime(selectedDate)
    },

    // 更新日记时间
    async updateDiaryTime(newDate) {
      try {
        
        // 使用兼容的日期解析
        const originalDate = parseDate(this.currentDiaryInfo.createTime)
        
        // 保持原来的时分秒，只修改年月日
        const updatedDate = new Date(
          newDate.getFullYear(),
          newDate.getMonth(),
          newDate.getDate(),
          originalDate.getHours(),
          originalDate.getMinutes(),
          originalDate.getSeconds(),
          originalDate.getMilliseconds()
        )
        
        
        // 格式化为后端兼容的格式
        const newTimeString = formatDateForBackend(updatedDate)
        
        // 保存到后端 - 使用专门的更新时间API
        const numericId = parseInt(this.diaryId, 10)
        
        const timeData = {
          createTime: newTimeString // 只发送时间数据
        }
        
        const result = await api.updateDiaryTime(numericId, timeData)
        
        // 更新成功后才更新本地显示
        this.currentDiaryInfo.createTime = newTimeString
      
        if (result) {
          uni.showToast({
            title: '时间已更新',
            icon: 'success'
          })
        } else {
          uni.showToast({
            title: '更新失败',
            icon: 'none'
          })
          // 恢复原来的时间
          this.LoadDiary()
        }
      } catch (error) {
        console.error('updateDiaryTime - 更新时间失败:', error)
        uni.showToast({
          title: '时间解析失败',
          icon: 'none'
        })
      }
    },
  },
}
</script>

<style scoped>
.edit-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #FFFFFF;
}

/* 为顶部状态栏预留空间的内容包裹容器 */
.content-wrapper {
  position: relative;
}

/* 顶部状态栏 */
.top-status-bar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12rpx 20rpx; 
  padding-top: calc(12rpx + env(safe-area-inset-top));
  background: #FFFFFF;
  border-bottom: 1rpx solid #F0F0F0;
  box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.04);
  z-index: 1000;
}

.top-bar-left, .top-bar-right { display: flex; align-items: center; }

.top-date-card {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 10rpx 16rpx;
  background: #F5F6F7;
  border-radius: 14rpx;
  border: 1rpx solid #ECECEC;
}

.date-day {
  font-size: 56rpx;
  line-height: 1;
  font-weight: 700;
  color: #333333;
}

.date-right { display: flex; flex-direction: column; justify-content: center; }
.date-line1 { font-size: 30rpx; color: #333; font-weight: 600; }
.date-line2 { font-size: 26rpx; color: #666; margin-top: 6rpx; }

.top-save-btn {
  padding: 10rpx 24rpx;
  background: linear-gradient(135deg, #FF9A76 0%, #FFC5A6 100%);
  border-radius: 16rpx;
  box-shadow: 0 6rpx 16rpx rgba(255, 154, 118, 0.3);
}

.top-save-btn.saving { opacity: 0.7; }
.top-save-text { font-size: 28rpx; color: #fff; font-weight: 600; }

/* 附件预览区域 */
.attachment-preview-area {
  padding: 16rpx;
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  background: #FFF5F0;
  border-bottom: 1rpx solid #FFE5D8;
}

.attachment-item {
  position: relative;
  width: 150rpx;
  height: 150rpx;
  border-radius: 12rpx;
  overflow: hidden;
}

.attachment-image {
  width: 100%;
  height: 100%;
}

.attachment-remove {
  position: absolute;
  top: 4rpx;
  right: 4rpx;
  width: 40rpx;
  height: 40rpx;
  background: rgba(0, 0, 0, 0.6);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.remove-icon {
  color: #FFFFFF;
  font-size: 36rpx;
  line-height: 1;
}

/* 编辑器容器 */
.editor-container {
  flex: 1;
  overflow: hidden;
  position: relative;
}

.editor-content {
  width: 100%;
  height: 100%;
  padding: 20rpx 24rpx;
  padding-bottom: 60rpx; /* 为字符数留出空间 */
  box-sizing: border-box;
  font-size: 32rpx;
  line-height: 1.6;
}

/* 编辑器内字符数显示 - 右下角 */
.editor-char-count {
  position: absolute;
  right: 24rpx;
  bottom: 16rpx;
  padding: 8rpx 16rpx;
  background: rgba(0, 0, 0, 0.05);
  border-radius: 20rpx;
  backdrop-filter: blur(10rpx);
  z-index: 2; /* 确保在编辑器内容之上 */
}

.editor-char-count .char-count-text {
  font-size: 24rpx;
  color: #999999;
  font-family: 'SF Pro Display', -apple-system, BlinkMacSystemFont, sans-serif;
}

/* 底部整体区域（固定） */
.bottom-area {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #FFFFFF;
  border-top: 1rpx solid #F0F0F0;
  box-shadow: 0 -4rpx 16rpx rgba(0, 0, 0, 0.04);
  z-index: 999;
  transition: bottom 0.2s ease;
}

/* 底部工具栏（不再固定，由父容器定位） */
.bottom-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 6rpx 12rpx; /* 更紧凑的一行高度 */
  background: #FFFFFF;
}

/* 工具栏左侧 - 格式按钮 */
.toolbar-left {
  display: flex;
  align-items: center;
  gap: 6rpx; /* 减小按钮间距 */
  flex: 1;
}

/* 工具栏右侧 - 功能按钮 */
.toolbar-right {
  display: flex;
  align-items: center;
  gap: 10rpx;
}

/* 工具按钮 */
.tool-btn {
  width: 48rpx; /* 缩小按钮尺寸以保证单行高度 */
  height: 48rpx;
  border-radius: 8rpx;
  background: #F8F8F8;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}

.tool-btn:active {
  transform: scale(0.95);
  background: #E8E8E8;
}

.tool-btn-active {
  background: #FF9A76 !important;
}

.tool-btn-active .tool-text {
  color: #FFFFFF !important;
}

.tool-text {
  font-size: 28rpx;
  color: #333333;
}

.tool-icon-img {
  width: 32rpx;
  height: 32rpx;
}

/* 删除底部保存按钮样式（已移至顶部状态栏） */

.save-button {
  width: 100%;
  height: 88rpx;
  background: linear-gradient(135deg, #FF9A76 0%, #FFC5A6 100%);
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 24rpx rgba(255, 154, 118, 0.3);
  transition: all 0.3s ease;
}

.save-button:active {
  transform: scale(0.98);
  box-shadow: 0 4rpx 16rpx rgba(255, 154, 118, 0.4);
}

.save-button.saving {
  opacity: 0.7;
  background: linear-gradient(135deg, #CCCCCC 0%, #E0E0E0 100%);
}

.save-button-text {
  font-size: 32rpx;
  color: #FFFFFF;
  font-weight: 600;
  letter-spacing: 2rpx;
}

/* 适配输入法弹起 */
.edit-page {
  padding-bottom: env(safe-area-inset-bottom);
}
</style>
