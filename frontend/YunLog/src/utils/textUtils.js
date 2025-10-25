/**
 * 文本处理工具函数
 */

/**
 * 从HTML中提取纯文本，移除所有HTML标签和实体
 * @param {string} html - HTML字符串
 * @returns {string} - 纯文本字符串
 */
export function stripHtmlTags(html) {
  if (!html) return ''
  
  // 移除HTML标签，但保留文本内容
  return html
    .replace(/<[^>]+>/g, '') // 移除所有HTML标签
    .replace(/&nbsp;/g, ' ') // 替换空格实体
    .replace(/&amp;/g, '&')  // 替换&符号实体
    .replace(/&lt;/g, '<')   // 替换小于号实体
    .replace(/&gt;/g, '>')   // 替换大于号实体
    .replace(/&quot;/g, '"') // 替换引号实体
    .replace(/&#39;/g, "'")  // 替换单引号实体
    .replace(/\s+/g, ' ')    // 将多个空白字符合并为单个空格
    .trim()                   // 去除首尾空白
}

/**
 * 获取日记内容的纯文本预览
 * @param {object} diaryItem - 日记对象，包含content和contentHtml字段
 * @param {number} maxLength - 最大预览长度，默认100
 * @returns {string} - 预览文本
 */
export function getPlainTextPreview(diaryItem, maxLength = 100) {
  let text = ''
  
  // 优先使用 contentHtml，如果没有则使用 content
  if (diaryItem.contentHtml) {
    // 从HTML中提取纯文本
    text = stripHtmlTags(diaryItem.contentHtml)
  } else if (diaryItem.content) {
    text = diaryItem.content
  }
  
  // 限制预览长度
  if (text.length > maxLength) {
    return text.substring(0, maxLength) + '...'
  }
  
  return text || '(无内容)'
}

/**
 * 截取文本到指定长度，超出部分用省略号表示
 * @param {string} text - 原始文本
 * @param {number} maxLength - 最大长度
 * @returns {string} - 截取后的文本
 */
export function truncateText(text, maxLength = 100) {
  if (!text) return ''
  
  if (text.length <= maxLength) {
    return text
  }
  
  return text.substring(0, maxLength) + '...'
}

/**
 * 清理和格式化文本，移除多余空白字符
 * @param {string} text - 原始文本
 * @returns {string} - 清理后的文本
 */
export function cleanText(text) {
  if (!text) return ''
  
  return text
    .replace(/\s+/g, ' ') // 将多个空白字符合并为单个空格
    .trim()               // 去除首尾空白
}

/**
 * 根据文件URL或扩展名判断文件类型
 * @param {string} url - 文件URL
 * @returns {string} - 文件类型：'image', 'video', 'audio', 'document', 'unknown'
 */
export function getFileType(url) {
  if (!url) return 'unknown'
  
  // 提取文件扩展名
  const extension = url.split('.').pop().toLowerCase()
  
  // 图片类型
  const imageExtensions = ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp', 'svg', 'ico']
  if (imageExtensions.includes(extension)) {
    return 'image'
  }
  
  // 视频类型
  const videoExtensions = ['mp4', 'avi', 'mov', 'wmv', 'flv', 'webm', 'mkv', '3gp']
  if (videoExtensions.includes(extension)) {
    return 'video'
  }
  
  // 音频类型
  const audioExtensions = ['mp3', 'wav', 'ogg', 'aac', 'flac', 'm4a']
  if (audioExtensions.includes(extension)) {
    return 'audio'
  }
  
  // 文档类型
  const documentExtensions = ['pdf', 'doc', 'docx', 'xls', 'xlsx', 'ppt', 'pptx', 'txt']
  if (documentExtensions.includes(extension)) {
    return 'document'
  }
  
  return 'unknown'
}

/**
 * 判断是否为图片文件
 * @param {string} url - 文件URL
 * @returns {boolean} - 是否为图片
 */
export function isImageFile(url) {
  return getFileType(url) === 'image'
}

/**
 * 判断是否为视频文件
 * @param {string} url - 文件URL
 * @returns {boolean} - 是否为视频
 */
export function isVideoFile(url) {
  return getFileType(url) === 'video'
}

/**
 * 解析日期字符串为 Date 对象，兼容 iOS
 * 支持格式：
 * - "2025-10-20 17:52:51" (后端返回格式)
 * - "2025-10-20T17:52:51" (ISO格式)
 * - "2025-10-20T17:52:51.000Z" (完整ISO格式)
 * - "2025/10/20 17:52:51" (iOS兼容格式)
 * @param {string} dateString - 日期字符串
 * @returns {Date} - Date 对象
 */
export function parseDate(dateString) {
  if (!dateString) return new Date()
  
  // 如果已经是 Date 对象，直接返回
  if (dateString instanceof Date) return dateString
  
  // 将 "YYYY-MM-DD HH:mm:ss" 格式转换为 iOS 兼容的格式
  let isoString = dateString
  
  // 检查是否为后端返回的格式 "YYYY-MM-DD HH:mm:ss"
  if (/^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}$/.test(dateString)) {
    // 转换为 ISO 格式 "YYYY-MM-DDTHH:mm:ss"
    isoString = dateString.replace(' ', 'T')
  }
  
  // 尝试直接解析
  const date = new Date(isoString)
  
  // 检查是否解析成功
  if (!isNaN(date.getTime())) {
    return date
  }
  
  // 如果直接解析失败，尝试手动解析
  const match = dateString.match(/^(\d{4})-(\d{2})-(\d{2})[\sT](\d{2}):(\d{2}):(\d{2})/)
  if (match) {
    const [, year, month, day, hour, minute, second] = match
    return new Date(
      parseInt(year),
      parseInt(month) - 1, // 月份从0开始
      parseInt(day),
      parseInt(hour),
      parseInt(minute),
      parseInt(second)
    )
  }
  
  // 如果所有解析都失败，返回当前时间
  return new Date()
}

/**
 * 格式化日期为后端兼容的字符串格式
 * @param {Date} date - Date 对象
 * @returns {string} - "YYYY-MM-DD HH:mm:ss" 格式的字符串
 */
export function formatDateForBackend(date) {
  if (!date || !(date instanceof Date)) return ''
  
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hour = String(date.getHours()).padStart(2, '0')
  const minute = String(date.getMinutes()).padStart(2, '0')
  const second = String(date.getSeconds()).padStart(2, '0')
  
  return `${year}-${month}-${day} ${hour}:${minute}:${second}`
}

export default {
  stripHtmlTags,
  getPlainTextPreview,
  truncateText,
  cleanText,
  getFileType,
  isImageFile,
  isVideoFile,
  parseDate,
  formatDateForBackend
}