/**
 * 认证工具类 - 处理token存储和用户信息
 */

const TOKEN_KEY = 'yunlog_token';
const USER_INFO_KEY = 'yunlog_user_info';

/**
 * 保存token
 */
export function setToken(token) {
  try {
    uni.setStorageSync(TOKEN_KEY, token);
  } catch (e) {
    console.error('保存token失败', e);
  }
}

/**
 * 获取token
 */
export function getToken() {
  try {
    return uni.getStorageSync(TOKEN_KEY);
  } catch (e) {
    console.error('获取token失败', e);
    return null;
  }
}

/**
 * 移除token
 */
export function removeToken() {
  try {
    uni.removeStorageSync(TOKEN_KEY);
  } catch (e) {
    console.error('移除token失败', e);
  }
}

/**
 * 保存用户信息
 */
export function setUserInfo(userInfo) {
  try {
    uni.setStorageSync(USER_INFO_KEY, JSON.stringify(userInfo));
  } catch (e) {
    console.error('保存用户信息失败', e);
  }
}

/**
 * 获取用户信息
 */
export function getUserInfo() {
  try {
    const userInfo = uni.getStorageSync(USER_INFO_KEY);
    return userInfo ? JSON.parse(userInfo) : null;
  } catch (e) {
    console.error('获取用户信息失败', e);
    return null;
  }
}

/**
 * 移除用户信息
 */
export function removeUserInfo() {
  try {
    uni.removeStorageSync(USER_INFO_KEY);
  } catch (e) {
    console.error('移除用户信息失败', e);
  }
}

/**
 * 检查是否已登录
 */
export function isLoggedIn() {
  const token = getToken();
  return !!token;
}

/**
 * 登出（清除所有认证信息）
 */
export function logout() {
  removeToken();
  removeUserInfo();
}

/**
 * 跳转到登录页
 */
export function navigateToLogin() {
  uni.navigateTo({
    url: '/pages/login/login'
  });
}

/**
 * 检查是否登录，未登录则跳转到登录页
 * @returns {boolean} 是否已登录
 */
export function requireLogin() {
  if (!isLoggedIn()) {
    uni.showToast({
      title: '请先登录',
      icon: 'none',
      duration: 1500
    });
    setTimeout(() => {
      navigateToLogin();
    }, 1500);
    return false;
  }
  return true;
}
