/**
 * 认证工具类 - 处理token存储和用户信息
 */

import { wechatLogin } from './api.js'

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
 * 显示微信登录弹窗
 */
export function showWechatLoginModal(callback) {
  // #ifdef MP-WEIXIN
  uni.showModal({
    title: '登录提示',
    content: '是否使用微信登录？',
    confirmText: '微信登录',
    cancelText: '暂不登录',
    success: (res) => {
      if (res.confirm) {
        // 用户点击确认，执行微信登录
        if (callback) {
          callback();
        }
      }
      // 用户点击取消，不做任何操作
    }
  });
  // #endif
  
  // #ifndef MP-WEIXIN
  uni.showToast({
    title: '请在微信小程序中登录',
    icon: 'none'
  });
  // #endif
}

/**
 * 执行微信登录
 */
export async function performWechatLogin() {
  try {
    // 获取微信登录 code
    const loginRes = await uni.login();
    
    if (!loginRes.code) {
      throw new Error('获取微信登录code失败');
    }
    
    // 调用后端微信登录接口
    const res = await wechatLogin({ code: loginRes.code });
    
    // 保存 token 和用户信息
    setToken(res.token);
    setUserInfo(res.userInfo);
    
    uni.showToast({
      title: '登录成功',
      icon: 'success',
      duration: 1500
    });
    
    // 刷新当前页面 - 使用 $forceUpdate 强制刷新
    setTimeout(() => {
      const pages = getCurrentPages();
      const currentPage = pages[pages.length - 1];
      if (currentPage && currentPage.$vm) {
        // 强制更新视图
        if (currentPage.$vm.$forceUpdate) {
          currentPage.$vm.$forceUpdate();
        }
        // 调用 onShow 生命周期
        if (currentPage.$vm.onShow) {
          currentPage.$vm.onShow();
        }
        // 调用 loadUserInfo 方法（如果存在）
        if (currentPage.$vm.loadUserInfo) {
          currentPage.$vm.loadUserInfo();
        }
      }
    }, 1500);
    
    return true;
  } catch (error) {
    console.error('微信登录失败', error);
    uni.showToast({
      title: '登录失败',
      icon: 'none'
    });
    return false;
  }
}

/**
 * 检查是否登录，未登录则显示登录弹窗
 * @returns {boolean} 是否已登录
 */
export function requireLogin() {
  if (!isLoggedIn()) {
    showWechatLoginModal(performWechatLogin);
    return false;
  }
  return true;
}
