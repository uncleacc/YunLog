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

    <!-- 编辑器工具栏组件 -->
    <EditorToolbar 
      :format-states="formatStates"
      @toggle-format="handleToggleFormat"
      @toggle-list="handleToggleList"
      @show-emoji-picker="handleShowEmojiPicker"
      @toggle-attachment-bar="handleToggleAttachmentBar"
    />

    <!-- 表情选择器组件 -->
    <EmojiPicker 
      v-if="showEmojiPicker"
      :visible="showEmojiPicker"
      @insert-emoji="handleEmojiSelect"
      @close="handleCloseEmojiPicker"
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

    <!-- 底部操作栏组件 -->
    <ActionBar 
      :is-saving="isSaving"
      @save="SaveDiary"
      @cancel="Cancel"
    />
  </view>
</template>

<script>
import TitleInput from './components/TitleInput.vue'
import AttachmentManager from './components/AttachmentManager.vue'
import EditorToolbar from './components/EditorToolbar.vue'
import EmojiPicker from './components/EmojiPicker.vue'
import EditorArea from './components/EditorArea.vue'
import ActionBar from './components/ActionBar.vue'

import { useEditorFormat } from './hooks/useEditorFormat.js'
import { useEditorContent } from './hooks/useEditorContent.js'
import { useCategories } from './hooks/useCategories.js'
import { useDiarySave } from './hooks/useDiarySave.js'
import storage from '../../utils/storage.js'

export default {
  components: {
    TitleInput,
    AttachmentManager,
    EditorToolbar,
    EmojiPicker,
    EditorArea,
    ActionBar
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
      showEmojiPicker: false,
      showAttachmentBar: false,
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
        this.editorCtx.format(editorFormatName)
        console.log('handleToggleFormat - 已调用编辑器格式方法:', editorFormatName)
      }
    },

    // 处理列表切换
    handleToggleList(listType) {
      this.toggleList(this.editorCtx, listType)
    },

    // 处理显示表情选择器
    handleShowEmojiPicker() {
      this.showEmojiPicker = true
    },

    // 处理关闭表情选择器
    handleCloseEmojiPicker() {
      this.showEmojiPicker = false
    },

    // 处理表情选择
    handleEmojiSelect(emoji) {
      this.insertContent(emoji)
      this.showEmojiPicker = false
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
                res.tempFilePaths.forEach(tempFilePath => {
                  this.addImageAttachment(tempFilePath)
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

    // 添加图片附件
    addImageAttachment(tempFilePath) {
      const attachment = {
        id: Date.now() + Math.random(),
        type: 'image',
        url: tempFilePath,
        name: `image_${Date.now()}.jpg`,
        size: 0, // 这里可以获取文件大小
        createTime: new Date().toISOString()
      }
      this.formData.attachments.push(attachment)
    },

    // 处理移除附件
    handleRemoveAttachment(index) {
      this.formData.attachments.splice(index, 1)
    },

    // 处理图片预览
    handlePreviewImage(url, index) {
      uni.previewImage({
        urls: this.formData.attachments.map(item => item.url),
        current: index
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
        console.log('handleEditorContextReady - 编辑模式，设置内容')
        this.setEditorContent(this.formData.contentHtml)
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
    LoadDiary() {
      const diary = storage.GetDiaryById(this.diaryId)
      if (!diary) return

      // 设置表单数据
      this.formData = {
        title: diary.title || '',
        content: diary.content || '',
        contentHtml: diary.contentHtml || '',
        attachments: diary.attachments || [],
        categoryId: diary.categoryId || 'default',
      }
      
      this.contentLength = diary.content ? diary.content.length : 0
      
      // 如果编辑器已准备好且有内容，则设置内容
      if (this.editorCtx && diary.contentHtml) {
        this.setEditorContent(diary.contentHtml)
      }
    },

    // 简化后的保存方法，直接使用 storage
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
        console.log('SaveDiary - 获取到编辑器内容:', {
          text: editorContent.text ? editorContent.text.substring(0, 50) + '...' : 'null',
          html: editorContent.html ? editorContent.html.substring(0, 50) + '...' : 'null',
          textLength: editorContent.text ? editorContent.text.length : 0,
          htmlLength: editorContent.html ? editorContent.html.length : 0
        })
        
        // 更新 formData 的内容
        this.formData.content = editorContent.text || ''
        this.formData.contentHtml = editorContent.html || ''
        
        console.log('SaveDiary - 准备保存的数据:', {
          isEditing: this.isEditing,
          title: this.formData.title,
          contentLength: this.formData.content.length,
          hasHtml: !!this.formData.contentHtml,
          attachmentsCount: this.formData.attachments.length
        })
        
        let result
        if (this.isEditing) {
          // 编辑模式 - 更新现有日记
          result = storage.UpdateDiary(this.diaryId, this.formData)
        } else {
          // 新建模式 - 创建新日记
          result = storage.AddDiary(this.formData)
        }
        
        uni.hideLoading()
        
        if (result) {
          console.log('SaveDiary - 保存成功')
          uni.showToast({ title: '保存成功', icon: 'success' })
          uni.navigateBack()
        } else {
          console.error('SaveDiary - 保存失败')
          uni.showToast({ title: '保存失败', icon: 'none' })
        }
      } catch (error) {
        console.error('SaveDiary - 保存异常:', error)
        uni.hideLoading()
        uni.showToast({ title: '获取内容失败', icon: 'none' })
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
  },
}
</script>

<style scoped>
.edit-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #FFF5F0 0%, #FFE5D8 100%);
  padding: 24rpx;
  padding-bottom: 200rpx;
}


</style>
