/**
 * API 接口统一管理
 * 对应后端 API 文档
 */
import { get, post, put, del, upload } from './request.js'
import { parseDate } from './textUtils.js'

// ==================== 认证管理 API ====================

/**
 * 发送短信验证码
 * @param {Object} data - { phone }
 */
export function sendSmsCode(data) {
  return post('/api/v1/auth/sms/send', data, { loadingText: '发送中...' })
}

/**
 * 手机验证码登录
 * @param {Object} data - { phone, code }
 */
export function phoneLogin(data) {
  return post('/api/v1/auth/phone/login', data, { loadingText: '登录中...' })
}

/**
 * 微信登录
 * @param {Object} data - { code }
 */
export function wechatLogin(data) {
  return post('/api/v1/auth/wechat/login', data, { loadingText: '登录中...' })
}

/**
 * 登出
 */
export function logout() {
  return post('/api/v1/auth/logout', {}, { loading: false, showError: false })
}

// ==================== 日记管理 API ====================

/**
 * 获取日记列表（分页）
 * @param {Object} params - { page, limit, categoryId, keyword }
 */
export function getDiaryList(params = {}) {
  const { page = 1, limit = 20, categoryId, keyword } = params
  let queryParams = { page, limit }
  
  if (categoryId) {
    queryParams.categoryId = categoryId
  }
  if (keyword) {
    queryParams.keyword = keyword
  }
  
  return get('/api/v1/diaries', queryParams)
}

/**
 * 获取日记详情
 * @param {Number} id - 日记ID
 */
export function getDiaryDetail(id) {
  return get(`/api/v1/diaries/${id}`)
}

/**
 * 创建日记
 * @param {Object} data - { title, content, contentHtml, categoryId }
 */
export function createDiary(data) {
  return post('/api/v1/diaries', data, { loadingText: '保存中...' })
}

/**
 * 更新日记
 * @param {Number} id - 日记ID
 * @param {Object} data - { title, content, contentHtml, categoryId }
 */
export function updateDiary(id, data) {
  return put(`/api/v1/diaries/${id}`, data, { loadingText: '保存中...' })
}

/**
 * 更新日记时间
 * @param {Number} id - 日记ID
 * @param {Object} data - { createTime }
 */
export function updateDiaryTime(id, data) {
  return put(`/api/v1/diaries/${id}/time`, data, { loadingText: '更新时间中...' })
}

/**
 * 删除日记（移至回收站）
 * @param {Number} id - 日记ID
 */
export function deleteDiary(id) {
  return del(`/api/v1/diaries/${id}`, {}, { loadingText: '删除中...' })
}

/**
 * 批量删除日记
 * @param {Array} ids - 日记ID数组
 */
export function batchDeleteDiaries(ids) {
  return del('/api/v1/diaries/batch', { ids }, { loadingText: '删除中...' })
}

// ==================== 分类管理 API ====================

/**
 * 获取分类列表
 */
export function getCategoryList() {
  return get('/api/v1/categories', {}, { loading: false })
}

/**
 * 获取分类详情
 * @param {Number} id - 分类ID
 */
export function getCategoryDetail(id) {
  return get(`/api/v1/categories/${id}`)
}

/**
 * 创建分类
 * @param {Object} data - { name, icon, color }
 */
export function createCategory(data) {
  return post('/api/v1/categories', data, { loadingText: '创建中...' })
}

/**
 * 更新分类
 * @param {Number} id - 分类ID
 * @param {Object} data - { name, icon, color }
 */
export function updateCategory(id, data) {
  return put(`/api/v1/categories/${id}`, data, { loadingText: '保存中...' })
}

/**
 * 删除分类
 * @param {Number} id - 分类ID
 */
export function deleteCategory(id) {
  return del(`/api/v1/categories/${id}`, {}, { loadingText: '删除中...' })
}

/**
 * 批量更新分类排序
 * @param {Array} categorySortList - [{ id, sortOrder }, ...]
 */
export function updateCategorySort(categorySortList) {
  return put('/api/v1/categories/sort', { categorySortList }, { 
    loadingText: '保存排序中...' 
  })
}

// ==================== 回收站管理 API ====================

/**
 * 获取回收站列表
 * @param {Object} params - { page, limit }
 */
export function getTrashList(params = {}) {
  const { page = 1, limit = 20 } = params
  return get('/api/v1/trash', { page, limit })
}

/**
 * 恢复日记
 * @param {Number} id - 日记ID
 */
export function restoreDiary(id) {
  return post(`/api/v1/trash/${id}/restore`, {}, { loadingText: '恢复中...' })
}

/**
 * 永久删除日记
 * @param {Number} id - 日记ID
 */
export function permanentDeleteDiary(id) {
  return del(`/api/v1/trash/${id}`, {}, { loadingText: '删除中...' })
}

/**
 * 清空回收站
 */
export function clearTrash() {
  return del('/api/v1/trash/clear', {}, { loadingText: '清空中...' })
}

/**
 * 批量恢复日记
 * @param {Array} ids - 日记ID数组
 */
export function batchRestoreDiaries(ids) {
  return post('/api/v1/trash/batch-restore', { ids }, { loadingText: '恢复中...' })
}

// ==================== 附件管理 API ====================

/**
 * 获取日记的附件列表
 * @param {Number} diaryId - 日记ID
 */
export function getAttachmentsByDiary(diaryId) {
  return get(`/api/v1/attachments/diary/${diaryId}`, {}, { loading: false })
}

/**
 * 创建附件记录
 * @param {Object} data - { diaryId, url }
 */
export function createAttachment(data) {
  return post('/api/v1/attachments', data, { loading: false })
}

/**
 * 批量创建附件
 * @param {Object} data - { diaryId, urls }
 */
export function batchCreateAttachments(data) {
  return post('/api/v1/attachments/batch', data, { loading: false })
}

/**
 * 删除附件
 * @param {Number} id - 附件ID
 */
export function deleteAttachment(id) {
  return del(`/api/v1/attachments/${id}`, {}, { loading: false })
}

// ==================== 文件上传 API ====================

/**
 * 上传单张图片
 * @param {String} filePath - 图片路径
 * @param {Number} diaryId - 日记ID（可选）
 */
export function uploadImage(filePath, diaryId) {
  const formData = diaryId ? { diaryId } : {}
  return upload('/api/v1/upload/image', filePath, formData, {
    name: 'file',
    loadingText: '上传中...'
  })
}

/**
 * 上传临时图片（不关联日记）
 * @param {String} filePath - 图片路径
 */
export function uploadTempImage(filePath) {
  return upload('/api/v1/upload/temp-image', filePath, {}, {
    name: 'file',
    loadingText: '上传中...'
  })
}

/**
 * 批量上传图片
 * @param {Array} filePaths - 图片路径数组
 * @param {Number} diaryId - 日记ID（可选）
 */
export function uploadImages(filePaths, diaryId) {
  // uni-app 不支持单次请求多文件上传，需要逐个上传
  const promises = filePaths.map(filePath => uploadImage(filePath, diaryId))
  return Promise.all(promises)
}

/**
 * 删除 OSS 文件
 * @param {String} url - 文件URL
 */
export function deleteOssFile(url) {
  return del(`/api/v1/upload/file?url=${encodeURIComponent(url)}`, {}, {
    loading: false
  })
}

// ==================== 统计信息 API（暂时前端计算）====================

/**
 * 获取分类统计
 * @param {Number} categoryId - 分类ID
 */
export function getCategoryStats(categoryId) {
  // TODO: 后端暂未实现此接口，前端临时计算
  return getDiaryList({ categoryId, limit: 1 }).then(res => {
    return {
      totalCount: res.total || 0,
      recentDiary: res.list && res.list.length > 0 ? res.list[0] : null
    }
  })
}

/**
 * 获取全局统计
 */
export function getGlobalStats() {
  // TODO: 后端暂未实现此接口，前端临时计算
  return getDiaryList({ limit: 1000 }).then(res => {
    const allDiaries = res.list || []
    const totalCount = res.total || 0
    
    // 计算连续天数
    let continueDays = 0
    if (allDiaries.length > 0) {
      const today = new Date()
      today.setHours(0, 0, 0, 0)
      
      // 按日期分组
      const dateMap = new Map()
      allDiaries.forEach(diary => {
        const date = parseDate(diary.createTime)
        date.setHours(0, 0, 0, 0)
        const dateStr = date.toISOString()
        if (!dateMap.has(dateStr)) {
          dateMap.set(dateStr, true)
        }
      })
      
      // 计算连续天数
      const dates = Array.from(dateMap.keys())
        .map(dateStr => parseDate(dateStr))
        .sort((a, b) => b - a)
      
      let currentDate = today
      for (let i = 0; i < dates.length; i++) {
        const diaryDate = dates[i]
        const diffTime = currentDate - diaryDate
        const diffDays = diffTime / (1000 * 60 * 60 * 24)
        
        if (diffDays === 0 || diffDays === 1) {
          continueDays++
          currentDate = new Date(currentDate)
          currentDate.setDate(currentDate.getDate() - 1)
          currentDate.setHours(0, 0, 0, 0)
        } else {
          break
        }
      }
    }
    
    return {
      totalCount,
      continueDays
    }
  })
}

export default {
  // 认证
  sendSmsCode,
  phoneLogin,
  wechatLogin,
  logout,
  
  // 日记
  getDiaryList,
  getDiaryDetail,
  createDiary,
  updateDiary,
  updateDiaryTime,
  deleteDiary,
  batchDeleteDiaries,
  
  // 分类
  getCategoryList,
  getCategoryDetail,
  createCategory,
  updateCategory,
  deleteCategory,
  
  // 回收站
  getTrashList,
  restoreDiary,
  permanentDeleteDiary,
  clearTrash,
  batchRestoreDiaries,
  
  // 附件
  getAttachmentsByDiary,
  createAttachment,
  batchCreateAttachments,
  deleteAttachment,
  
  // 上传
  uploadImage,
  uploadTempImage,
  uploadImages,
  deleteOssFile,
  
  // 统计
  getCategoryStats,
  getGlobalStats
}
