// 表情符号处理工具函数
// 用于确保表情符号在编辑、保存、显示过程中的正确性

/**
 * 检测文本中是否包含表情符号
 * @param {string} text - 要检测的文本
 * @returns {boolean} 是否包含表情符号
 */
export function hasEmoji(text) {
  if (!text || typeof text !== 'string') return false
  
  // Unicode 表情符号范围
  const emojiRegex = /[\u{1F600}-\u{1F64F}]|[\u{1F300}-\u{1F5FF}]|[\u{1F680}-\u{1F6FF}]|[\u{1F1E0}-\u{1F1FF}]|[\u{2600}-\u{26FF}]|[\u{2700}-\u{27BF}]|[\u{1F900}-\u{1F9FF}]|[\u{1F018}-\u{1F0FF}]|[\u{1F000}-\u{1F02F}]|[\u{1F0A0}-\u{1F0FF}]/gu
  
  return emojiRegex.test(text)
}

/**
 * 统计文本中表情符号的数量
 * @param {string} text - 要统计的文本
 * @returns {number} 表情符号数量
 */
export function countEmoji(text) {
  if (!text || typeof text !== 'string') return 0
  
  const emojiRegex = /[\u{1F600}-\u{1F64F}]|[\u{1F300}-\u{1F5FF}]|[\u{1F680}-\u{1F6FF}]|[\u{1F1E0}-\u{1F1FF}]|[\u{2600}-\u{26FF}]|[\u{2700}-\u{27BF}]|[\u{1F900}-\u{1F9FF}]|[\u{1F018}-\u{1F0FF}]|[\u{1F000}-\u{1F02F}]|[\u{1F0A0}-\u{1F0FF}]/gu
  
  const matches = text.match(emojiRegex)
  return matches ? matches.length : 0
}

/**
 * 获取文本中的所有表情符号
 * @param {string} text - 要提取的文本
 * @returns {Array<string>} 表情符号数组
 */
export function extractEmojis(text) {
  if (!text || typeof text !== 'string') return []
  
  const emojiRegex = /[\u{1F600}-\u{1F64F}]|[\u{1F300}-\u{1F5FF}]|[\u{1F680}-\u{1F6FF}]|[\u{1F1E0}-\u{1F1FF}]|[\u{2600}-\u{26FF}]|[\u{2700}-\u{27BF}]|[\u{1F900}-\u{1F9FF}]|[\u{1F018}-\u{1F0FF}]|[\u{1F000}-\u{1F02F}]|[\u{1F0A0}-\u{1F0FF}]/gu
  
  return text.match(emojiRegex) || []
}

/**
 * 验证编辑器内容中表情符号的完整性
 * @param {string} htmlContent - HTML内容
 * @param {string} textContent - 纯文本内容
 * @returns {Object} 验证结果
 */
export function validateEmojiContent(htmlContent, textContent) {
  const htmlEmojis = extractEmojis(htmlContent || '')
  const textEmojis = extractEmojis(textContent || '')
  
  const result = {
    isValid: htmlEmojis.length === textEmojis.length,
    htmlEmojiCount: htmlEmojis.length,
    textEmojiCount: textEmojis.length,
    htmlEmojis,
    textEmojis,
    missingInHtml: textEmojis.filter(emoji => !htmlEmojis.includes(emoji)),
    missingInText: htmlEmojis.filter(emoji => !textEmojis.includes(emoji))
  }
  
  return result
}

/**
 * 确保文本正确编码以支持表情符号
 * @param {string} text - 要编码的文本
 * @returns {string} 编码后的文本
 */
export function encodeEmojiText(text) {
  if (!text || typeof text !== 'string') return text
  
  // 确保使用UTF-8编码
  try {
    return decodeURIComponent(encodeURIComponent(text))
  } catch (e) {
    console.warn('表情符号编码处理失败:', e)
    return text
  }
}

/**
 * 为富文本显示准备表情符号内容
 * @param {string} content - 原始内容
 * @returns {string} 处理后的内容
 */
export function prepareEmojiForRichText(content) {
  if (!content || typeof content !== 'string') return content
  
  // 确保表情符号在HTML中正确显示
  return content
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#x27;')
}

/**
 * 常用表情符号列表（用于测试）
 */
export const commonEmojis = [
  '😀', '😃', '😄', '😁', '😆', '😅', '🤣', '😂', '🙂', '🙃',
  '😉', '😊', '😇', '🥰', '😍', '🤩', '😘', '😗', '☺️', '😚',
  '😙', '🥲', '😋', '😛', '😜', '🤪', '😝', '🤑', '🤗', '🤭',
  '🤫', '🤔', '🤐', '🤨', '😐', '😑', '😶', '😏', '😒', '🙄',
  '😬', '🤥', '😔', '😪', '🤤', '😴', '😷', '🤒', '🤕', '🤢',
  '🤮', '🤧', '🥵', '🥶', '🥴', '😵', '🤯', '🤠', '🥳', '🥸',
  '😎', '🤓', '🧐', '😕', '😟', '🙁', '☹️', '😮', '😯', '😲',
  '😳', '🥺', '😦', '😧', '😨', '😰', '😥', '😢', '😭', '😱',
  '😖', '😣', '😞', '😓', '😩', '😫', '🥱', '😤', '😡', '😠',
  '🤬', '😈', '👿', '💀', '☠️', '💩', '🤡', '👹', '👺', '👻'
]

/**
 * 表情符号分类
 */
export const emojiCategories = {
  smileys: ['😀', '😃', '😄', '😁', '😆', '😅', '🤣', '😂'],
  hearts: ['❤️', '🧡', '💛', '💚', '💙', '💜', '🖤', '🤍'],
  animals: ['🐶', '🐱', '🐭', '🐹', '🐰', '🦊', '🐻', '🐼'],
  nature: ['🌱', '🌿', '🍀', '🌺', '🌸', '🌼', '🌻', '🌹'],
  food: ['🍎', '🍌', '🍇', '🍓', '🍒', '🥑', '🍕', '🍔'],
  activities: ['⚽', '🏀', '🏈', '⚾', '🎾', '🏐', '🏉', '🎱']
}

export default {
  hasEmoji,
  countEmoji,
  extractEmojis,
  validateEmojiContent,
  encodeEmojiText,
  prepareEmojiForRichText,
  commonEmojis,
  emojiCategories
}