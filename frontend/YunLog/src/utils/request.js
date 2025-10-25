/**
 * HTTP 请求封装 - uni-app 版本
 * 统一处理请求和响应
 */

// 后端 API 基础地址
const BASE_URL = 'http://localhost:8080/api/v1'

/**
 * 发送 HTTP 请求
 * @param {Object} options - 请求配置
 * @returns {Promise}
 */
function request(options) {
  return new Promise((resolve, reject) => {
    // 显示加载提示
    if (options.loading !== false) {
      uni.showLoading({
        title: options.loadingText || '加载中...',
        mask: true
      })
    }

    // 发起请求
    uni.request({
      url: BASE_URL + options.url,
      method: options.method || 'GET',
      data: options.data || {},
      header: {
        'Content-Type': 'application/json',
        ...options.header
      },
      timeout: options.timeout || 60000,
      success: (res) => {
        // 隐藏加载提示
        if (options.loading !== false) {
          uni.hideLoading()
        }

        // 处理响应
        if (res.statusCode === 200) {
          const data = res.data
          
          // 后端统一返回格式: { code, message, data }
          // 200 表示成功，201 表示创建成功
          if (data.code === 200 || data.code === 201) {
            resolve(data.data)
          } else {
            // 业务错误
            const errorMsg = data.message || '操作失败'
            if (options.showError !== false) {
              uni.showToast({
                title: errorMsg,
                icon: 'none',
                duration: 2000
              })
            }
            reject(new Error(errorMsg))
          }
        } else {
          // HTTP 错误
          // 尝试从响应体中获取错误消息
          const data = res.data
          const errorMsg = (data && data.message) || `请求失败(${res.statusCode})`
          
          if (options.showError !== false) {
            uni.showToast({
              title: errorMsg,
              icon: 'none',
              duration: 2000
            })
          }
          reject(new Error(errorMsg))
        }
      },
      fail: (err) => {
        // 隐藏加载提示
        if (options.loading !== false) {
          uni.hideLoading()
        }

        // 网络错误
        const errorMsg = err.errMsg || '网络连接失败'
        if (options.showError !== false) {
          uni.showToast({
            title: errorMsg,
            icon: 'none',
            duration: 2000
          })
        }
        reject(new Error(errorMsg))
      }
    })
  })
}

/**
 * GET 请求
 */
export function get(url, params = {}, options = {}) {
  return request({
    url,
    method: 'GET',
    data: params,
    ...options
  })
}

/**
 * POST 请求
 */
export function post(url, data = {}, options = {}) {
  return request({
    url,
    method: 'POST',
    data,
    ...options
  })
}

/**
 * PUT 请求
 */
export function put(url, data = {}, options = {}) {
  return request({
    url,
    method: 'PUT',
    data,
    ...options
  })
}

/**
 * DELETE 请求
 */
export function del(url, data = {}, options = {}) {
  return request({
    url,
    method: 'DELETE',
    data,
    ...options
  })
}

/**
 * 文件上传
 * @param {String} url - 上传地址
 * @param {String} filePath - 文件路径
 * @param {Object} formData - 额外参数
 * @param {Object} options - 配置项
 */
export function upload(url, filePath, formData = {}, options = {}) {
  return new Promise((resolve, reject) => {
    const fullUrl = BASE_URL + url
    
    // 显示上传进度
    if (options.loading !== false) {
      uni.showLoading({
        title: options.loadingText || '上传中...',
        mask: true
      })
    }

    uni.uploadFile({
      url: fullUrl,
      filePath,
      name: options.name || 'file',
      formData,
      success: (res) => {
        if (options.loading !== false) {
          uni.hideLoading()
        }

        if (res.statusCode === 200) {
          try {
            const data = JSON.parse(res.data)
            if (data.code === 200 || data.code === 201) {
              resolve(data.data)
            } else {
              const errorMsg = data.message || '上传失败'
              if (options.showError !== false) {
                uni.showToast({
                  title: errorMsg,
                  icon: 'none'
                })
              }
              reject(new Error(errorMsg))
            }
          } catch (parseError) {
            const errorMsg = '服务器响应格式错误'
            if (options.showError !== false) {
              uni.showToast({
                title: errorMsg,
                icon: 'none'
              })
            }
            reject(new Error(errorMsg))
          }
        } else {
          const errorMsg = `上传失败(${res.statusCode})`
          if (options.showError !== false) {
            uni.showToast({
              title: errorMsg,
              icon: 'none'
            })
          }
          reject(new Error(errorMsg))
        }
      },
      fail: (err) => {
        if (options.loading !== false) {
          uni.hideLoading()
        }

        const errorMsg = err.errMsg || '上传失败'
        if (options.showError !== false) {
          uni.showToast({
            title: errorMsg,
            icon: 'none'
          })
        }
        reject(new Error(errorMsg))
      }
    })
  })
}

export default {
  get,
  post,
  put,
  del,
  upload,
  BASE_URL
}
