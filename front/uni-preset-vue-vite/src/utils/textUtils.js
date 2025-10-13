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

export default {
  stripHtmlTags,
  getPlainTextPreview,
  truncateText,
  cleanText
}