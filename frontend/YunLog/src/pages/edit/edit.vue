<template>
  <view class="edit-page">
    <!-- 标题输入组件 -->
    <TitleInput 
      :title="formData.title"
      @update:title="handleTitleUpdate"
    />

    <!-- 附件管理组件 -->
    <AttachmentManager 
      v-if="formData.attachments.length > 0 || showAttachmentBar"
      :attachments="formData.attachments"
      :showAttachmentBar="showAttachmentBar"
      @add-attachment="handleAddAttachment"
      @remove-attachment="handleRemoveAttachment"
      @preview-image="handlePreviewImage"
    />

    <!-- 富文本编辑器组件 -->
    <EditorArea 
      ref="editorArea"
      :content-length="contentLength"
      @editor-ready="handleEditorReady"
      @editor-input="handleEditorInput"
      @editor-selection-change="handleEditorSelectionChange"
      @editor-context-ready="handleEditorContextReady"
      @editor-status-change="handleEditorStatusChange"
    />

    <!-- 编辑器工具栏组件 -->
    <EditorToolbar 
      :format-states="formatStates"
      @toggle-format="handleToggleFormat"
      @toggle-list="handleToggleList"
      @toggle-attachment-bar="handleToggleAttachmentBar"
    />

    <!-- 日记时间设置模块 -->
    <view class="time-info-section" v-if="isEditing && currentDiaryInfo">
      <view class="time-info-container">
        <view class="time-main-area">
          <view class="time-content">
            <text class="time-label">日记时间</text>
            <text class="time-value">{{ formatDate(currentDiaryInfo.createTime) }}</text>
          </view>
          <view class="time-edit-btn" @click="showDatePicker">
            <text class="edit-icon">📅</text>
          </view>
        </view>
      </view>
      <view class="time-info-footer">
        <text class="time-footer-text">点击日历图标可修改日记时间</text>
      </view>
    </view>

    <!-- 底部操作栏组件 -->
    <ActionBar 
      :is-saving="isSaving"
      @save="SaveDiary"
      @cancel="Cancel"
    />

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
import TitleInput from './components/TitleInput.vue'
import AttachmentManager from './components/AttachmentManager.vue'
import EditorToolbar from './components/EditorToolbar.vue'
import EditorArea from './components/EditorArea.vue'
import ActionBar from './components/ActionBar.vue'
import CalendarPicker from './components/CalendarPicker.vue'

import { useEditorFormat } from './hooks/useEditorFormat.js'
import { useEditorContent } from './hooks/useEditorContent.js'
import { useCategories } from './hooks/useCategories.js'
import { useDiarySave } from './hooks/useDiarySave.js'
import storage from '../../utils/storage.js'
import { validateEmojiContent, hasEmoji } from '../../utils/emojiUtils.js'
import api from '../../utils/api.js'
import { isImageFile, isVideoFile, parseDate, formatDateForBackend } from '../../utils/textUtils.js'

export default {
  components: {
    TitleInput,
    AttachmentManager,
    EditorToolbar,
    EditorArea,
    ActionBar,
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

  data() {
    return {
      diaryId: '',
      categoryId: '',
      formData: {
        title: '',
        content: '',
        contentHtml: '',
        attachments: [],
        categoryId: '',
      },
      contentLength: 0,
      hasEdited: false,
      showAttachmentBar: false,
      currentDiaryInfo: null, // 当前日记的完整信息（包含时间等）
      showCalendar: false, // 是否显示日历选择器
    }
  },

  onLoad(options) {
    console.log('onLoad - 页面加载', options)
    
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
    // 页面就绪，编辑器会自动初始化
  },

  methods: {
    // === 组件事件处理方法 ===
    
    // 处理标题更新
    handleTitleUpdate(title) {
      this.formData.title = title
    },

    // 处理格式切换
    handleToggleFormat(format) {
      if (!this.editorCtx) return
      
      console.log('handleToggleFormat - 切换格式:', format)
      
      // 格式映射：将我们的格式名映射到编辑器的格式名
      const formatMap = {
        bold: 'bold',
        italic: 'italic',
        underline: 'underline',
        strikethrough: 'strike'  // 注意：编辑器使用 'strike'
      }
      
      const editorFormatName = formatMap[format]
      if (editorFormatName) {
        // 直接使用编辑器的 format 方法，这会触发 statuschange 事件
        if (this.editorCtx.format && typeof this.editorCtx.format === 'function') {
          this.editorCtx.format(editorFormatName)
          console.log('handleToggleFormat - 已调用编辑器格式方法:', editorFormatName)
        } else {
          console.warn('handleToggleFormat - format 方法不可用')
        }
      }
    },

    // 处理列表切换
    handleToggleList(listType) {
      this.toggleList(this.editorCtx, listType)
    },



    // 处理切换附件栏
    handleToggleAttachmentBar() {
      this.showAttachmentBar = !this.showAttachmentBar
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
        console.log('addImageAttachment - 开始上传图片:', tempFilePath)
        
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
        console.log('addImageAttachment - 上传成功:', uploadResult)
        
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
          console.log('handleRemoveAttachment - 删除数据库附件记录 ID:', attachment.id)
          await api.deleteAttachment(attachment.id)
          console.log('handleRemoveAttachment - 数据库记录删除成功')
        }
        
        // 如果是已上传到OSS的文件，尝试删除OSS文件
        if (attachment.url && !attachment.url.startsWith('wxfile://') && !attachment.url.startsWith('blob:')) {
          console.log('handleRemoveAttachment - 删除OSS文件:', attachment.url)
          await api.deleteOssFile(attachment.url)
          console.log('handleRemoveAttachment - OSS文件删除成功')
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

    // 处理编辑器就绪
    handleEditorReady(e) {
      console.log('handleEditorReady - 编辑器就绪事件:', e)
      this.onEditorReady(e)
    },

    // 处理编辑器上下文就绪
    handleEditorContextReady(context) {
      console.log('handleEditorContextReady - 编辑器上下文就绪:', !!context)
      this.setEditorContext(context)
      
      // 如果是编辑模式且有内容HTML，设置编辑器内容
      if (this.isEditing && this.formData.contentHtml) {
        console.log('handleEditorContextReady - 编辑模式，设置内容:', this.formData.contentHtml.substring(0, 100))
        this.setEditorContent(this.formData.contentHtml)
      } else if (this.isEditing && !this.formData.contentHtml && this.formData.content) {
        // 如果没有HTML内容但有纯文本内容，设置纯文本
        console.log('handleEditorContextReady - 编辑模式，设置纯文本内容')
        this.setEditorContent(this.formData.content)
      }
    },

    // 处理编辑器输入
    handleEditorInput(e) {
      console.log('handleEditorInput - 编辑器输入事件:', {
        hasDetail: !!e.detail,
        hasText: !!(e.detail && e.detail.text),
        textLength: e.detail && e.detail.text ? e.detail.text.length : 0
      })
      this.onEditorInput(e)
      this.hasEdited = true
      this.contentLength = e.detail.text ? e.detail.text.length : 0
      
      // 同步更新 formData 的纯文本内容（用于备份）
      if (e.detail && e.detail.text) {
        this.formData.content = e.detail.text
      }
    },

    // 处理编辑器选择变化
    handleEditorSelectionChange(e) {
      console.log("handleEditorSelectionChange：选择发生变化");
      this.onEditorSelectionChange(e, {
        updateToolbarFromSelection: this.updateToolbarFromSelection,
        syncGlobalFormatsWithCursor: this.syncGlobalFormatsWithCursor,
        hasActiveGlobalFormats: this.hasActiveGlobalFormats
      })
    },

    // 处理编辑器状态变化
    handleEditorStatusChange(e) {
      console.log('handleEditorStatusChange - 编辑器状态变化:', e.detail)
      // 直接从编辑器的状态事件中获取格式状态
      const formats = e.detail
      
      // 更新格式状态，这里使用编辑器原生的状态
      if (formats) {
        this.formatStates.bold = !!formats.bold
        this.formatStates.italic = !!formats.italic
        this.formatStates.underline = !!formats.underline
        this.formatStates.strikethrough = !!(formats.strike || formats.strikeThrough)
        this.formatStates.listOrdered = formats.list === 'ordered'
        this.formatStates.listBullet = formats.list === 'bullet'
        
        console.log('handleEditorStatusChange - 格式状态已更新:', this.formatStates)
      }
    },

    // 加载日记数据（编辑模式）
    async LoadDiary() {
      if (!this.diaryId) {
        console.error('LoadDiary - 缺少日记ID')
        return
      }

      try {
        console.log(`🚀 编辑页面加载日记 ID: ${this.diaryId}`)
        
        // 确保 diaryId 是数字类型
        const numericId = parseInt(this.diaryId, 10)
        if (isNaN(numericId)) {
          console.error('LoadDiary - 日记ID格式错误:', this.diaryId)
          uni.showToast({ title: '日记ID格式错误', icon: 'none' })
          return
        }

        // 从后端获取日记详情
        const diary = await api.getDiaryDetail(numericId)
        console.log('✅ 编辑页面获取日记成功:', diary)

        // 获取附件列表
        let attachments = []
        try {
          attachments = await api.getAttachmentsByDiary(numericId)
          console.log('✅ 编辑页面获取附件成功:', attachments)
          
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
          title: diary.title || '',
          content: diary.content || '',
          contentHtml: diary.contentHtml || '',
          attachments: attachments || [],
          categoryId: diary.categoryId || 'default',
        }
        
        console.log('LoadDiary - 已存在附件详情:', this.formData.attachments.map(att => ({
          id: att.id,
          url: att.url ? att.url.substring(att.url.length - 20) : 'no-url',
          existsInDb: att.existsInDb
        })))
        
        this.contentLength = diary.content ? diary.content.length : 0
        
        console.log('LoadDiary - 表单数据设置完成:', {
          title: this.formData.title,
          contentLength: this.contentLength,
          hasContentHtml: !!this.formData.contentHtml,
          attachmentsCount: this.formData.attachments.length,
          categoryId: this.formData.categoryId
        })
        
        // 如果编辑器已准备好且有内容，则设置内容
        if (this.editorCtx && diary.contentHtml) {
          console.log('LoadDiary - 设置编辑器内容')
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
      if (!this.formData.title.trim()) {
        uni.showToast({ title: '请输入标题', icon: 'none' })
        return
      }
      
      if (this.contentLength === 0 && !this.formData.content) {
        uni.showToast({ title: '请输入内容', icon: 'none' })
        return
      }

      try {
        uni.showLoading({ title: '保存中...' })
        
        // 检查编辑器上下文
        console.log('SaveDiary - 检查编辑器状态:', {
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
          console.log('SaveDiary - 表情符号验证通过:', {
            emojiCount: emojiValidation.textEmojiCount,
            emojis: emojiValidation.textEmojis
          })
        }
        
        console.log('SaveDiary - 获取到编辑器内容:', {
          text: editorContent.text ? editorContent.text.substring(0, 50) + '...' : 'null',
          html: editorContent.html ? editorContent.html.substring(0, 50) + '...' : 'null',
          textLength: editorContent.text ? editorContent.text.length : 0,
          htmlLength: editorContent.html ? editorContent.html.length : 0,
          hasEmojis: hasEmoji(editorContent.text || '')
        })
        
        // 更新 formData 的内容
        this.formData.content = editorContent.text || ''
        this.formData.contentHtml = editorContent.html || ''
        
        console.log('SaveDiary - 准备保存的数据:', {
          isEditing: this.isEditing,
          title: this.formData.title,
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
          console.log('SaveDiary - 更新日记 ID:', numericId)
          result = await api.updateDiary(numericId, saveData)
        } else {
          // 新建模式 - 创建新日记
          console.log('SaveDiary - 创建新日记')
          result = await api.createDiary(saveData)
        }
        
        // 日记保存成功后，处理附件关联
        if (result && this.formData.attachments.length > 0) {
          console.log('SaveDiary - 处理附件关联:', this.formData.attachments.length)
          
          try {
            // 过滤出需要创建数据库记录的新附件
            const newAttachments = this.formData.attachments.filter(att => 
              !att.uploading && // 不是上传中的
              !att.existsInDb && // 不是已存在于数据库的
              att.url && 
              !att.url.startsWith('wxfile://') && 
              !att.url.startsWith('blob:') &&
              !att.url.startsWith('http://tmp/'))
            
            console.log('SaveDiary - 需要创建记录的新附件数量:', newAttachments.length)
            
            if (newAttachments.length > 0) {
              // 获取日记ID
              const diaryId = this.isEditing ? parseInt(this.diaryId, 10) : result.id
              console.log('SaveDiary - 关联到日记ID:', diaryId)
              
              if (diaryId) {
                // 批量创建新附件记录
                const newAttachmentUrls = newAttachments.map(att => att.url)
                console.log('SaveDiary - 新附件URLs:', newAttachmentUrls)
                
                await api.batchCreateAttachments({
                  diaryId: diaryId,
                  urls: newAttachmentUrls
                })
                console.log('SaveDiary - 新附件关联成功')
              }
            } else {
              console.log('SaveDiary - 没有需要创建记录的新附件')
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
          console.log('SaveDiary - 保存成功:', result)
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
      if (this.formData.title || this.contentLength > 0 || this.formData.attachments.length > 0) {
        uni.showModal({
          title: '提示',
          content: '确定要放弃编辑吗？',
          confirmColor: '#FF9A76',
          success: (res) => {
            if (res.confirm) {
              uni.navigateBack()
            }
          },
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
        console.log('updateDiaryTime - 原始时间字符串:', this.currentDiaryInfo.createTime)
        
        // 使用兼容的日期解析
        const originalDate = parseDate(this.currentDiaryInfo.createTime)
        console.log('updateDiaryTime - 解析后的原始时间:', originalDate)
        
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
        
        console.log('updateDiaryTime - 更新后的时间:', updatedDate)
        
        // 格式化为后端兼容的格式
        const newTimeString = formatDateForBackend(updatedDate)
        console.log('updateDiaryTime - 格式化后的时间字符串:', newTimeString)
        
        // 保存到后端 - 使用专门的更新时间API
        const numericId = parseInt(this.diaryId, 10)
        console.log('updateDiaryTime - 更新日记ID:', numericId)
        
        const timeData = {
          createTime: newTimeString // 只发送时间数据
        }
        
        const result = await api.updateDiaryTime(numericId, timeData)
        console.log('updateDiaryTime - 更新结果:', result)
        
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
  min-height: 100vh;
  background: linear-gradient(135deg, #FFF5F0 0%, #FFE5D8 100%);
  padding: 24rpx;
  padding-bottom: 200rpx;
  display: flex;
  flex-direction: column;
}

/* 时间信息模块样式 */
.time-info-section {
  margin: 32rpx 0 24rpx 0;
  padding: 0 8rpx;
}

.time-info-container {
  background: rgba(255, 255, 255, 0.9);
  border-radius: 20rpx;
  padding: 24rpx 32rpx;
  box-shadow: 0 8rpx 24rpx rgba(255, 154, 118, 0.08);
  border: 1px solid rgba(255, 154, 118, 0.1);
  backdrop-filter: blur(10rpx);
  position: relative;
  overflow: hidden;
}

.time-info-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3rpx;
  background: linear-gradient(90deg, #FF9A76 0%, #FFC5A6 50%, #FF9A76 100%);
  border-radius: 20rpx 20rpx 0 0;
}

.time-main-area {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.time-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.time-label {
  font-size: 24rpx;
  color: #999;
  font-weight: 500;
  letter-spacing: 0.5rpx;
}

.time-value {
  font-size: 32rpx;
  color: #333;
  font-weight: 600;
  font-family: 'SF Pro Display', -apple-system, BlinkMacSystemFont, sans-serif;
}

.time-edit-btn {
  padding: 16rpx 20rpx;
  background: linear-gradient(135deg, #FF9A76 0%, #FFC5A6 100%);
  border-radius: 16rpx;
  box-shadow: 0 4rpx 12rpx rgba(255, 154, 118, 0.3);
  transition: all 0.3s ease;
}

.time-edit-btn:active {
  transform: scale(0.95);
  box-shadow: 0 2rpx 8rpx rgba(255, 154, 118, 0.4);
}

.edit-icon {
  font-size: 28rpx;
  line-height: 1;
}

.time-info-footer {
  margin-top: 16rpx;
  text-align: center;
}

.time-footer-text {
  font-size: 22rpx;
  color: #666;
  opacity: 0.8;
  font-style: italic;
  letter-spacing: 0.3rpx;
}

/* 时间信息模块响应式适配 */
@media screen and (max-width: 750px) {
  .time-info-container {
    padding: 20rpx 24rpx;
  }
  
  .time-label {
    font-size: 22rpx;
  }
  
  .time-value {
    font-size: 26rpx;
  }
  
  .time-footer-text {
    font-size: 20rpx;
  }
  
  .time-divider {
    height: 50rpx;
    margin: 0 16rpx;
  }
}

/* 深色模式适配（如果需要的话） */
@media (prefers-color-scheme: dark) {
  .time-info-container {
    background: rgba(40, 40, 40, 0.95);
    border: 1px solid rgba(255, 154, 118, 0.2);
  }
  
  .time-label {
    color: #aaa;
  }
  
  .time-value {
    color: #fff;
  }
  
  .time-footer-text {
    color: #999;
  }
}

</style>
