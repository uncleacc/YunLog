<template>
  <view class="container">
    <scroll-view class="content" scroll-y>
      <!-- 附件区域 -->
      <view class="attachments-card" v-if="formData.attachments.length > 0 || showAttachmentBar">
        <view class="attachments-label">附件</view>
        <view class="attachments-grid">
          <!-- 已上传的附件 -->
          <view
            class="attachment-item"
            v-for="(item, index) in formData.attachments"
            :key="index"
            @longpress="ShowAttachmentMenu(index)"
          >
            <image
              v-if="item.type === 'image'"
              class="attachment-preview"
              :src="item.url"
              mode="aspectFill"
            />
            <view v-else class="attachment-video">
              <text class="video-icon">🎬</text>
              <text class="video-text">视频</text>
            </view>
            <view class="attachment-delete" @click="RemoveAttachment(index)">
              <text class="delete-icon">×</text>
            </view>
          </view>
          <!-- 添加附件按钮 -->
          <view class="attachment-add" @click="AddAttachment">
            <text class="add-icon">+</text>
            <text class="add-text">添加</text>
          </view>
        </view>
      </view>

      <!-- 标题输入 -->
      <view class="input-card">
        <input
          class="title-input"
          type="text"
          placeholder="给这一天起个标题吧~"
          v-model="formData.title"
          maxlength="50"
        />
        <view class="char-count">{{ formData.title.length }}/50</view>
      </view>

      <!-- 富文本工具栏 -->
      <view class="toolbar-card">
        <view class="toolbar-row">
          <view class="tool-btn" @click="ToggleFormat('bold')">
            <text class="tool-text" style="font-weight: bold">B</text>
          </view>
          <view class="tool-btn" @click="ToggleFormat('italic')">
            <text class="tool-text" style="font-style: italic">I</text>
          </view>
          <view class="tool-btn" @click="ToggleFormat('underline')">
            <text class="tool-text" style="text-decoration: underline">U</text>
          </view>
          <view class="tool-btn" @click="ToggleFormat('strikethrough')">
            <text class="tool-text" style="text-decoration: line-through">S</text>
          </view>
          <view class="tool-btn" @click="ShowEmojiPicker">
            <text class="tool-icon">😊</text>
          </view>
          <view class="tool-btn" @click="ToggleAttachmentBar">
            <text class="tool-icon">📎</text>
          </view>
        </view>
      </view>

      <!-- 表情选择器 -->
      <view class="emoji-picker" v-if="showEmojiPicker">
        <view class="emoji-header">
          <text class="emoji-title">选择表情</text>
          <view class="emoji-close" @click="CloseEmojiPicker">
            <text class="close-icon">×</text>
          </view>
        </view>
        <scroll-view class="emoji-list" scroll-y>
          <view
            class="emoji-item"
            v-for="emoji in emojiList"
            :key="emoji"
            @click="InsertEmoji(emoji)"
          >
            <text class="emoji-char">{{ emoji }}</text>
          </view>
        </scroll-view>
      </view>

      <!-- 富文本编辑器 -->
      <view class="editor-card">
        <editor
          id="editor"
          class="editor-content"
          placeholder="记录此刻的心情和故事..."
          :show-img-size="false"
          :show-img-toolbar="false"
          :show-img-resize="false"
          @ready="OnEditorReady"
          @input="OnEditorInput"
        />
        <view class="char-count">{{ contentLength }}/5000</view>
      </view>
    </scroll-view>

    <!-- 底部操作栏 -->
    <view class="bottom-bar">
      <view class="action-button secondary" @click="Cancel">
        <text class="button-text">取消</text>
      </view>
      <view class="action-button primary" @click="SaveDiary">
        <text class="button-text">保存</text>
      </view>
    </view>
  </view>
</template>

<script>
import storage from '@/utils/storage.js'

export default {
  data() {
    return {
      isEdit: false,
      diaryId: '',
      categoryId: '',
      formData: {
        title: '',
        content: '',
        contentHtml: '',
        attachments: [],
        categoryId: '',
      },
      latestDiary: null,
      editorCtx: null,
      contentLength: 0,
      hasEdited: false,
      didInitialFill: false,
      showEmojiPicker: false,
      showAttachmentBar: false,
      emojiList: [
        '😀', '😃', '😄', '😁', '😆', '😅', '🤣', '😂', '🙂', '🙃',
        '😉', '😊', '😇', '🥰', '😍', '🤩', '😘', '😗', '😚', '😙',
        '😋', '😛', '😜', '🤪', '😝', '🤑', '🤗', '🤭', '🤫', '🤔',
        '🤐', '🤨', '😐', '😑', '😶', '😏', '😒', '🙄', '😬', '🤥',
        '😌', '😔', '😪', '🤤', '😴', '😷', '🤒', '🤕', '🤢', '🤮',
        '🤧', '🥵', '🥶', '🥴', '😵', '🤯', '🤠', '🥳', '😎', '🤓',
        '🧐', '😕', '😟', '🙁', '☹️', '😮', '😯', '😲', '😳', '🥺',
        '😦', '😧', '😨', '😰', '😥', '😢', '😭', '😱', '😖', '😣',
        '😞', '😓', '😩', '😫', '🥱', '😤', '😡', '😠', '🤬', '😈',
        '👿', '💀', '☠️', '💩', '🤡', '👹', '👺', '👻', '👽', '👾',
        '❤️', '🧡', '💛', '💚', '💙', '💜', '🖤', '🤍', '🤎', '💔',
        '❣️', '💕', '💞', '💓', '💗', '💖', '💘', '💝', '💟', '☮️',
        '✨', '⭐', '🌟', '💫', '⚡', '🔥', '💥', '☀️', '🌙', '⭐',
        '🎉', '🎊', '🎈', '🎁', '🏆', '🥇', '🥈', '🥉', '⚽', '🏀',
        '👍', '👎', '👏', '🙌', '👋', '🤝', '✊', '👊', '🤛', '🤜',
        '✌️', '🤞', '🤟', '🤘', '👌', '🤌', '🤏', '👈', '👉', '👆',
      ],
    }
  },
  watch: {
    // 监听编辑器上下文变化，当编辑器就绪时自动填充
    editorCtx(newVal, oldVal) {
      console.log('watch editorCtx - 编辑器状态变化:', {
        wasNull: !oldVal,
        isNowReady: !!newVal,
        isEdit: this.isEdit,
        hasLatestDiary: !!this.latestDiary,
        didInitialFill: this.didInitialFill
      })
      
      // 当编辑器从未就绪变为就绪时，且是编辑模式，且有数据，且未填充过
      if (!oldVal && newVal && this.isEdit && this.latestDiary && !this.didInitialFill && !this.hasEdited) {
        console.log('watch editorCtx - 编辑器刚就绪，触发填充')
        this.TryFillEditor()
      }
    },
    
    // 监听日记数据变化（适用于编辑器先就绪但数据后到的情况）
    latestDiary(newVal) {
      console.log('watch latestDiary - 数据变化:', {
        hasNewVal: !!newVal,
        hasEditorCtx: !!this.editorCtx,
        didInitialFill: this.didInitialFill
      })
      
      if (newVal && this.editorCtx && !this.didInitialFill && !this.hasEdited && this.isEdit) {
        console.log('watch latestDiary - 数据刚加载，触发填充')
        this.TryFillEditor()
      }
    }
  },
  onLoad(options) {
    console.log('onLoad - 页面加载', options)
    // 如果有id参数，表示是编辑模式
    if (options.id) {
      this.isEdit = true
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
    console.log('onReady - 页面初次渲染完成')
    // 强制初始化编辑器上下文（解决@ready事件不触发的问题）
    this.ForceInitEditor()
  },
  methods: {
    // 强制初始化编辑器（解决@ready事件不触发的问题）
    ForceInitEditor() {
      console.log('ForceInitEditor - 强制初始化编辑器')
      
      // 延迟一点时间，确保DOM完全渲染
      setTimeout(() => {
        this.GetEditorContextWithRetry()
      }, 200)
    },
    
    // 重试获取编辑器上下文
    GetEditorContextWithRetry(attempt = 1) {
      console.log(`GetEditorContextWithRetry - 第${attempt}次尝试获取编辑器上下文`)
      
      uni.createSelectorQuery()
        .in(this)
        .select('#editor')
        .context((res) => {
          console.log(`GetEditorContextWithRetry - 第${attempt}次查询结果:`, {
            hasRes: !!res,
            hasContext: !!(res && res.context)
          })
          
          if (!res || !res.context) {
            if (attempt < 5) {
              console.warn(`GetEditorContextWithRetry - 第${attempt}次失败，${300}ms后重试`)
              setTimeout(() => {
                this.GetEditorContextWithRetry(attempt + 1)
              }, 300)
            } else {
              console.error('GetEditorContextWithRetry - 5次尝试后仍无法获取编辑器上下文')
            }
            return
          }
          
          // 成功获取编辑器上下文
          this.editorCtx = res.context
          console.log('GetEditorContextWithRetry - 成功设置editorCtx')
          
          // 如果数据已加载，立即尝试填充
          if (this.isEdit && this.latestDiary && !this.didInitialFill) {
            console.log('GetEditorContextWithRetry - 数据已就绪，立即填充')
            this.TryFillEditor()
          }
        })
        .exec()
    },
    
    ManualGetEditorContext() {
      console.log('ManualGetEditorContext - 手动获取编辑器上下文')
      this.GetEditorContextWithRetry()
    },
    
    TryFillEditor() {
      console.log('TryFillEditor - 尝试填充编辑器:', {
        hasEditorCtx: !!this.editorCtx,
        hasLatestDiary: !!this.latestDiary,
        didInitialFill: this.didInitialFill
      })
      
      // 快速检查必要条件
      if (!this.editorCtx || !this.latestDiary || this.didInitialFill) {
        console.log('TryFillEditor - 条件不满足，跳过')
        return
      }
      
      const html = this.GetHtmlFromDiary(this.latestDiary)
      console.log('TryFillEditor - 生成HTML:', {
        hasHtml: !!html,
        htmlLength: html ? html.length : 0,
        htmlPreview: html ? html.substring(0, 50) : ''
      })
      
      if (!html) {
        console.error('TryFillEditor - HTML为空')
        return
      }
      
      // 标记已填充
      this.didInitialFill = true
      console.log('TryFillEditor - 开始设置内容')
      
      // 直接设置，不需要重试（editorCtx已确认存在）
      this.SetEditorContent(html)
    },
    GetHtmlFromDiary(diary) {
      if (!diary) return ''
      if (diary.contentHtml && diary.contentHtml.trim()) return diary.contentHtml
      if (diary.content) return this.BuildHtmlFromText(diary.content)
      return ''
    },
    BuildHtmlFromText(text) {
      if (!text) return ''
      const escaped = String(text)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
      const withBr = escaped.replace(/\n/g, '<br/>')
      return `<p>${withBr}</p>`
    },

    SetEditorContent(html) {
      if (!this.editorCtx) {
        console.error('SetEditorContent - editorCtx为空')
        return
      }
      if (!html) {
        console.error('SetEditorContent - html为空')
        return
      }
      
      console.log('SetEditorContent - 准备设置内容:', {
        htmlLength: html.length,
        htmlPreview: html.substring(0, 100),
        hasEditorCtx: !!this.editorCtx,
        contentLength: this.formData.content ? this.formData.content.length : 0
      })
      
      this.editorCtx.setContents({
        html,
        success: () => {
          console.log('SetEditorContent - 设置成功')
          // 设置成功后，更新内容长度
          this.contentLength = this.formData.content ? this.formData.content.length : 0
        },
        fail: (err) => {
          console.error('SetEditorContent - 设置失败:', err)
        }
      })
    },

    SetEditorContentWithRetry(html, attempt = 1) {
      console.log(`SetEditorContentWithRetry - 尝试第${attempt}次`, {
        hasHtml: !!html,
        hasEditorCtx: !!this.editorCtx,
        htmlLength: html ? html.length : 0
      })
      
      if (!html) {
        console.error('SetEditorContentWithRetry - html为空，停止重试')
        return
      }
      
      if (this.editorCtx) {
        console.log('SetEditorContentWithRetry - editorCtx已就绪，开始设置')
        this.SetEditorContent(html)
        return
      }
      
      if (attempt > 10) {
        console.error('SetEditorContentWithRetry - 超过重试次数，放弃设置', {
          finalAttempt: attempt,
          hasEditorCtx: !!this.editorCtx
        })
        return
      }
      
      setTimeout(() => {
        this.SetEditorContentWithRetry(html, attempt + 1)
      }, 300)
    },
    LoadDiary() {
      const diary = storage.GetDiaryById(this.diaryId)
      if (diary) {
        this.latestDiary = diary
        // 保存完整的日记数据
        this.formData = {
          title: diary.title,
          content: diary.content,
          contentHtml: diary.contentHtml || '',
          attachments: diary.attachments || [],
          categoryId: diary.categoryId || 'default',
        }
        // 初始化内容长度
        this.contentLength = diary.content ? diary.content.length : 0
        this.hasEdited = false
        
        console.log('LoadDiary - 加载日记数据:', {
          id: this.diaryId,
          title: diary.title,
          content: diary.content,
          contentLength: this.contentLength,
          hasHtml: !!diary.contentHtml,
          attachmentsCount: diary.attachments ? diary.attachments.length : 0
        })

        // 若编辑器已就绪，立即尝试设置内容（总是以存储中的最新数据为准）
        if (this.isEdit && this.editorCtx) {
          const html = this.GetHtmlFromDiary(this.latestDiary)
          if (html) {
            this.$nextTick(() => this.SetEditorContentWithRetry(html))
          }
        }
      }
    },

    OnEditorReady() {
      console.log('OnEditorReady - 编辑器@ready事件触发（备用方案）')
      // 如果@ready事件触发了，直接使用重试机制获取上下文
      this.GetEditorContextWithRetry()
    },

    OnEditorInput(e) {
      // 更新字数统计
      if (e.detail && e.detail.text !== undefined) {
        this.contentLength = e.detail.text.length
      }
      this.hasEdited = true
    },

    ToggleFormat(format) {
      if (!this.editorCtx) return

      const formatMap = {
        bold: 'bold',
        italic: 'italic',
        underline: 'underline',
        strikethrough: 'strikeThrough',
      }

      this.editorCtx.format(formatMap[format])
    },

    ShowEmojiPicker() {
      this.showEmojiPicker = true
    },

    CloseEmojiPicker() {
      this.showEmojiPicker = false
    },

    InsertEmoji(emoji) {
      if (this.editorCtx) {
        this.editorCtx.insertText({
          text: emoji,
        })
      }
      this.CloseEmojiPicker()
    },

    ToggleAttachmentBar() {
      this.showAttachmentBar = !this.showAttachmentBar
    },

    AddAttachment() {
      uni.showActionSheet({
        itemList: ['拍照', '从相册选择图片', '选择视频'],
        success: (res) => {
          if (res.tapIndex === 0) {
            this.ChooseImageFromCamera()
          } else if (res.tapIndex === 1) {
            this.ChooseImageFromAlbum()
          } else if (res.tapIndex === 2) {
            this.ChooseVideo()
          }
        },
      })
    },

    ChooseImageFromCamera() {
      uni.chooseImage({
        count: 1,
        sourceType: ['camera'],
        success: (res) => {
          this.formData.attachments.push({
            type: 'image',
            url: res.tempFilePaths[0],
          })
        },
      })
    },

    ChooseImageFromAlbum() {
      uni.chooseImage({
        count: 9 - this.formData.attachments.length,
        sourceType: ['album'],
        success: (res) => {
          res.tempFilePaths.forEach((path) => {
            this.formData.attachments.push({
              type: 'image',
              url: path,
            })
          })
        },
      })
    },

    ChooseVideo() {
      uni.chooseVideo({
        sourceType: ['camera', 'album'],
        maxDuration: 60,
        success: (res) => {
          this.formData.attachments.push({
            type: 'video',
            url: res.tempFilePath,
          })
        },
      })
    },

    RemoveAttachment(index) {
      this.formData.attachments.splice(index, 1)
    },

    ShowAttachmentMenu(index) {
      uni.showActionSheet({
        itemList: ['删除'],
        success: (res) => {
          if (res.tapIndex === 0) {
            this.RemoveAttachment(index)
          }
        },
      })
    },

    Validate() {
      if (!this.formData.title.trim()) {
        uni.showToast({
          title: '请输入标题',
          icon: 'none',
        })
        return false
      }

      // 修复Bug 1：在编辑模式下，即使contentLength为0，也检查formData.content
      if (this.contentLength === 0 && !this.formData.content) {
        uni.showToast({
          title: '请输入内容',
          icon: 'none',
        })
        return false
      }

      return true
    },

    SaveDiary() {
      if (!this.Validate()) {
        return
      }

      // 修复Bug 2：改进编辑器就绪检查
      if (!this.editorCtx) {
        // 若用户对编辑器进行过编辑，则尝试重新获取上下文并读取内容
        if (this.hasEdited) {
          uni.showLoading({ title: '保存中...' })
          uni.createSelectorQuery()
            .in(this)
            .select('#editor')
            .context((res) => {
              this.editorCtx = res && res.context ? res.context : null
              if (!this.editorCtx) {
                uni.hideLoading()
                uni.showToast({ title: '编辑器未就绪，请稍后重试', icon: 'none' })
                return
              }
              // 重新获取内容后继续保存
              this.editorCtx.getContents({
                success: (r) => {
                  const text = r && typeof r.text === 'string' ? r.text : ''
                  const html = r && typeof r.html === 'string' ? r.html : ''
                  this.formData.content = text
                  this.formData.contentHtml = html
                  this.DoSave()
                },
                fail: () => {
                  uni.hideLoading()
                  uni.showToast({ title: '获取内容失败', icon: 'none' })
                },
              })
            })
            .exec()
          return
        }

        // 未编辑过文本的情况下，可直接保存现有内容（例如只改了附件/分类）
        if (this.isEdit && (this.formData.content || this.formData.contentHtml || this.formData.attachments.length > 0)) {
          this.DoSave()
          return
        }

        uni.showToast({
          title: '编辑器未就绪，请稍后重试',
          icon: 'none',
        })
        return
      }

      uni.showLoading({
        title: '保存中...',
      })

      // 获取编辑器内容
      this.editorCtx.getContents({
        success: (res) => {
          console.log('SaveDiary - 获取编辑器内容成功:', {
            textLength: res && typeof res.text === 'string' ? res.text.length : 0,
            hasHtml: !!(res && res.html)
          })

          // 防止空白内容或空HTML覆盖原有内容
          const text = res && typeof res.text === 'string' ? res.text : ''
          const html = res && typeof res.html === 'string' ? res.html : ''

          // 判断文本是否仅为空白/换行
          const isTextEmpty = text.replace(/\s+/g, '').length === 0

          // 常见空HTML：<p><br></p>、<br>、<p>\n</p> 以及空白字符
          const htmlStrippedBreaks = html
            .replace(/<br\s*\/?>/gi, '')
            .replace(/<p>\s*<\/p>/gi, '')
            .replace(/\s+/g, '')
          const isHtmlEmpty = !html || htmlStrippedBreaks.length === 0

          // 若用户没有对编辑器进行过编辑，则空内容不覆盖原值；
          // 若用户确实编辑并清空，则允许保存为空
          if (isTextEmpty && isHtmlEmpty && !this.hasEdited) {
            this.formData.content = this.formData.content || ''
            this.formData.contentHtml = this.formData.contentHtml || ''
          } else {
            this.formData.content = text
            this.formData.contentHtml = html
          }

          this.DoSave()
        },
        fail: (err) => {
          console.error('SaveDiary - 获取编辑器内容失败:', err)
          uni.hideLoading()
          // 如果获取失败但有原始内容（编辑模式未修改），直接保存
          if (this.isEdit && this.formData.content) {
            console.log('SaveDiary - 使用原始内容保存')
            this.DoSave()
          } else {
            uni.showToast({
              title: '获取内容失败',
              icon: 'none',
            })
          }
        },
      })
    },

    DoSave() {
      uni.showLoading({
        title: '保存中...',
      })

      console.log('DoSave - 准备保存:', {
        isEdit: this.isEdit,
        diaryId: this.diaryId,
        title: this.formData.title,
        contentLength: this.formData.content ? this.formData.content.length : 0,
        hasHtml: !!this.formData.contentHtml,
        attachmentsCount: this.formData.attachments ? this.formData.attachments.length : 0,
        categoryId: this.formData.categoryId
      })

      let result
      if (this.isEdit) {
        result = storage.UpdateDiary(this.diaryId, this.formData)
        console.log('DoSave - 更新结果:', result ? '成功' : '失败')
      } else {
        result = storage.AddDiary(this.formData)
        console.log('DoSave - 添加结果:', result ? '成功' : '失败')
      }

      uni.hideLoading()

      if (result) {
        uni.showToast({
          title: '保存成功',
          icon: 'success',
        })
        setTimeout(() => {
          uni.navigateBack()
        }, 1000)
      } else {
        uni.showToast({
          title: '保存失败',
          icon: 'none',
        })
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
.container {
  min-height: 100vh;
  background-color: #fff5f0;
  display: flex;
  flex-direction: column;
}

.content {
  flex: 1;
  padding: 32rpx;
  padding-bottom: 160rpx;
}

/* 附件区域 */
.attachments-card {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 32rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(255, 154, 118, 0.1);
}

.attachments-label {
  font-size: 28rpx;
  color: #666666;
  margin-bottom: 20rpx;
  font-weight: 500;
}

.attachments-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}

.attachment-item {
  width: 200rpx;
  height: 200rpx;
  border-radius: 16rpx;
  overflow: hidden;
  position: relative;
  background: #f5f5f5;
}

.attachment-preview {
  width: 100%;
  height: 100%;
}

.attachment-video {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.video-icon {
  font-size: 64rpx;
  margin-bottom: 8rpx;
}

.video-text {
  font-size: 24rpx;
  color: #ffffff;
}

.attachment-delete {
  position: absolute;
  top: 8rpx;
  right: 8rpx;
  width: 48rpx;
  height: 48rpx;
  background: rgba(0, 0, 0, 0.5);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.delete-icon {
  font-size: 36rpx;
  color: #ffffff;
  font-weight: bold;
}

.attachment-add {
  width: 200rpx;
  height: 200rpx;
  border-radius: 16rpx;
  border: 2rpx dashed #ff9a76;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #fff5f0;
}

.add-icon {
  font-size: 48rpx;
  color: #ff9a76;
  margin-bottom: 8rpx;
}

.add-text {
  font-size: 24rpx;
  color: #ff9a76;
}

/* 标题输入 */
.input-card {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 32rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(255, 154, 118, 0.1);
}

.title-input {
  width: 100%;
  font-size: 36rpx;
  color: #333333;
  font-weight: bold;
  margin-bottom: 16rpx;
}

.char-count {
  font-size: 24rpx;
  color: #cccccc;
  text-align: right;
}

/* 富文本工具栏 */
.toolbar-card {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 24rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(255, 154, 118, 0.1);
}

.toolbar-row {
  display: flex;
  gap: 16rpx;
}

.tool-btn {
  width: 72rpx;
  height: 72rpx;
  border-radius: 12rpx;
  background: #fff5f0;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.tool-btn:active {
  transform: scale(0.95);
  background: #ffe5d8;
}

.tool-text {
  font-size: 32rpx;
  color: #333333;
}

.tool-icon {
  font-size: 40rpx;
}

/* 表情选择器 */
.emoji-picker {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 24rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(255, 154, 118, 0.1);
  max-height: 500rpx;
}

.emoji-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.emoji-title {
  font-size: 28rpx;
  color: #666666;
  font-weight: 500;
}

.emoji-close {
  width: 48rpx;
  height: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff5f0;
  border-radius: 50%;
}

.close-icon {
  font-size: 36rpx;
  color: #ff9a76;
  font-weight: bold;
}

.emoji-list {
  height: 400rpx;
}

.emoji-item {
  display: inline-block;
  width: 80rpx;
  height: 80rpx;
  text-align: center;
  line-height: 80rpx;
  border-radius: 12rpx;
  transition: all 0.3s ease;
}

.emoji-item:active {
  background: #fff5f0;
  transform: scale(1.2);
}

.emoji-char {
  font-size: 48rpx;
}

/* 富文本编辑器 */
.editor-card {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 32rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(255, 154, 118, 0.1);
  min-height: 400rpx;
}

.editor-content {
  width: 100%;
  min-height: 300rpx;
  font-size: 32rpx;
  color: #666666;
  line-height: 1.8;
  margin-bottom: 16rpx;
}

/* 底部操作栏 */
.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #ffffff;
  padding: 24rpx 32rpx;
  padding-bottom: calc(24rpx + env(safe-area-inset-bottom));
  box-shadow: 0 -4rpx 16rpx rgba(0, 0, 0, 0.05);
  display: flex;
  gap: 24rpx;
}

.action-button {
  flex: 1;
  height: 88rpx;
  border-radius: 44rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.action-button.primary {
  background: linear-gradient(135deg, #ff9a76 0%, #ff7e5f 100%);
  box-shadow: 0 4rpx 16rpx rgba(255, 126, 95, 0.3);
}

.action-button.secondary {
  background: #ffffff;
  border: 2rpx solid #ff9a76;
}

.action-button.primary .button-text {
  color: #ffffff;
  font-weight: 500;
}

.action-button.secondary .button-text {
  color: #ff9a76;
  font-weight: 500;
}

.action-button:active {
  transform: scale(0.98);
  opacity: 0.8;
}

.button-text {
  font-size: 32rpx;
}
</style>
