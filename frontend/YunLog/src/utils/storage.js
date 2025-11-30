const DIARY_STORAGE_KEY = 'cloud_diary_list'
const TRASH_STORAGE_KEY = 'cloud_diary_trash'
const CATEGORY_STORAGE_KEY = 'cloud_diary_categories'

import { parseDate } from './textUtils.js'

export default {
  /**
   * 获取所有日记列表
   */
  GetDiaryList() {
    try {
      const data = uni.getStorageSync(DIARY_STORAGE_KEY)
      return data ? JSON.parse(data) : []
    } catch (e) {
      console.error('获取日记列表失败:', e)
      return []
    }
  },

  /**
   * 保存日记列表
   */
  SaveDiaryList(list) {
    try {
      uni.setStorageSync(DIARY_STORAGE_KEY, JSON.stringify(list))
      return true
    } catch (e) {
      console.error('保存日记列表失败:', e)
      return false
    }
  },

  /**
   * 添加新日记
   */
  AddDiary(diary) {
    try {
      const list = this.GetDiaryList()
      const newDiary = {
        id: Date.now().toString(),
        title: diary.title,
        content: diary.content,
        contentHtml: diary.contentHtml || '',
        attachments: diary.attachments || [],
        categoryId: diary.categoryId || 'default',
        createTime: new Date().toISOString(),
        updateTime: new Date().toISOString(),
      }
      list.unshift(newDiary)
      this.SaveDiaryList(list)
      return newDiary
    } catch (e) {
      console.error('添加日记失败:', e)
      return null
    }
  },

  /**
   * 根据ID获取日记
   */
  GetDiaryById(id) {
    try {
      const list = this.GetDiaryList()
      return list.find((item) => item.id === id)
    } catch (e) {
      console.error('获取日记详情失败:', e)
      return null
    }
  },

  /**
   * 更新日记
   */
  UpdateDiary(id, diary) {
    try {
      const list = this.GetDiaryList()
      const index = list.findIndex((item) => item.id === id)
      
      console.log('UpdateDiary - 查找日记:', {
        id,
        found: index !== -1,
        totalDiaries: list.length
      })
      
      if (index !== -1) {
        const oldDiary = list[index]
        console.log('UpdateDiary - 更新前:', {
          title: oldDiary.title,
          contentLength: oldDiary.content ? oldDiary.content.length : 0,
          hasHtml: !!oldDiary.contentHtml,
          attachmentsCount: oldDiary.attachments ? oldDiary.attachments.length : 0
        })
        
        list[index] = {
          ...list[index],
          ...diary,
          updateTime: new Date().toISOString(),
        }
        
        console.log('UpdateDiary - 更新后:', {
          title: list[index].title,
          contentLength: list[index].content ? list[index].content.length : 0,
          hasHtml: !!list[index].contentHtml,
          attachmentsCount: list[index].attachments ? list[index].attachments.length : 0
        })
        
        this.SaveDiaryList(list)
        return list[index]
      }
      
      console.error('UpdateDiary - 未找到日记:', id)
      return null
    } catch (e) {
      console.error('更新日记失败:', e)
      return null
    }
  },

  /**
   * 删除日记（移到垃圾桶）
   */
  DeleteDiary(id) {
    try {
      const list = this.GetDiaryList()
      const diary = list.find((item) => item.id === id)
      if (!diary) return false

      // 添加删除时间并移到垃圾桶
      const trashedDiary = {
        ...diary,
        deletedTime: new Date().toISOString(),
      }
      this.AddToTrash(trashedDiary)

      // 从日记列表中移除
      const newList = list.filter((item) => item.id !== id)
      this.SaveDiaryList(newList)
      return true
    } catch (e) {
      console.error('删除日记失败:', e)
      return false
    }
  },

  /**
   * 获取垃圾桶列表
   */
  GetTrashList() {
    try {
      const data = uni.getStorageSync(TRASH_STORAGE_KEY)
      const list = data ? JSON.parse(data) : []
      
      // 清理超过30天的日记
      const now = new Date()
      const validList = list.filter((item) => {
        const deletedDate = parseDate(item.deletedTime)
        const diffDays = (now - deletedDate) / (1000 * 60 * 60 * 24)
        return diffDays <= 30
      })
      
      // 如果有变化，保存更新后的列表
      if (validList.length !== list.length) {
        this.SaveTrashList(validList)
      }
      
      return validList
    } catch (e) {
      console.error('获取垃圾桶列表失败:', e)
      return []
    }
  },

  /**
   * 保存垃圾桶列表
   */
  SaveTrashList(list) {
    try {
      uni.setStorageSync(TRASH_STORAGE_KEY, JSON.stringify(list))
      return true
    } catch (e) {
      console.error('保存垃圾桶列表失败:', e)
      return false
    }
  },

  /**
   * 添加到垃圾桶
   */
  AddToTrash(diary) {
    try {
      const list = this.GetTrashList()
      list.unshift(diary)
      this.SaveTrashList(list)
      return true
    } catch (e) {
      console.error('添加到垃圾桶失败:', e)
      return false
    }
  },

  /**
   * 从垃圾桶恢复日记
   */
  RestoreFromTrash(id) {
    try {
      const trashList = this.GetTrashList()
      const diary = trashList.find((item) => item.id === id)
      if (!diary) return false

      // 移除deletedTime字段
      const { deletedTime, ...restoredDiary } = diary

      // 添加回日记列表
      const diaryList = this.GetDiaryList()
      diaryList.unshift(restoredDiary)
      this.SaveDiaryList(diaryList)

      // 从垃圾桶移除
      const newTrashList = trashList.filter((item) => item.id !== id)
      this.SaveTrashList(newTrashList)

      return true
    } catch (e) {
      console.error('恢复日记失败:', e)
      return false
    }
  },

  /**
   * 永久删除日记
   */
  PermanentDeleteDiary(id) {
    try {
      const trashList = this.GetTrashList()
      const newTrashList = trashList.filter((item) => item.id !== id)
      this.SaveTrashList(newTrashList)
      return true
    } catch (e) {
      console.error('永久删除日记失败:', e)
      return false
    }
  },

  /**
   * 清空垃圾桶
   */
  ClearTrash() {
    try {
      this.SaveTrashList([])
      return true
    } catch (e) {
      console.error('清空垃圾桶失败:', e)
      return false
    }
  },

  // ==================== 分类管理 ====================

  /**
   * 获取所有分类
   */
  GetCategoryList() {
    try {
      const data = uni.getStorageSync(CATEGORY_STORAGE_KEY)
      const list = data ? JSON.parse(data) : []
      
      // 如果没有分类，创建默认分类
      if (list.length === 0) {
        const defaultCategory = {
          id: 'default',
          name: '默认分类',
          color: '#FF9A76',
          icon: '📝',
          createTime: new Date().toISOString(),
        }
        list.push(defaultCategory)
        this.SaveCategoryList(list)
      }
      
      return list
    } catch (e) {
      console.error('获取分类列表失败:', e)
      return []
    }
  },

  /**
   * 保存分类列表
   */
  SaveCategoryList(list) {
    try {
      uni.setStorageSync(CATEGORY_STORAGE_KEY, JSON.stringify(list))
      return true
    } catch (e) {
      console.error('保存分类列表失败:', e)
      return false
    }
  },

  /**
   * 添加新分类
   */
  AddCategory(category) {
    try {
      const list = this.GetCategoryList()
      const newCategory = {
        id: Date.now().toString(),
        name: category.name,
        color: category.color || '#FF9A76',
        icon: category.icon || '📁',
        createTime: new Date().toISOString(),
      }
      list.push(newCategory)
      this.SaveCategoryList(list)
      return newCategory
    } catch (e) {
      console.error('添加分类失败:', e)
      return null
    }
  },

  /**
   * 根据ID获取分类
   */
  GetCategoryById(id) {
    try {
      const list = this.GetCategoryList()
      return list.find((item) => item.id === id)
    } catch (e) {
      console.error('获取分类详情失败:', e)
      return null
    }
  },

  /**
   * 更新分类
   */
  UpdateCategory(id, category) {
    try {
      const list = this.GetCategoryList()
      const index = list.findIndex((item) => item.id === id)
      if (index !== -1) {
        list[index] = {
          ...list[index],
          ...category,
        }
        this.SaveCategoryList(list)
        return list[index]
      }
      return null
    } catch (e) {
      console.error('更新分类失败:', e)
      return null
    }
  },

  /**
   * 删除分类
   */
  DeleteCategory(id) {
    try {
      // 不能删除默认分类
      if (id === 'default') {
        return false
      }

      // 将该分类下的所有日记移到默认分类
      const diaryList = this.GetDiaryList()
      diaryList.forEach((diary) => {
        if (diary.categoryId === id) {
          diary.categoryId = 'default'
        }
      })
      this.SaveDiaryList(diaryList)

      // 删除分类
      const list = this.GetCategoryList()
      const newList = list.filter((item) => item.id !== id)
      this.SaveCategoryList(newList)
      return true
    } catch (e) {
      console.error('删除分类失败:', e)
      return false
    }
  },

  /**
   * 根据分类ID获取日记列表
   */
  GetDiaryListByCategory(categoryId) {
    try {
      const list = this.GetDiaryList()
      return list.filter((item) => item.categoryId === categoryId)
    } catch (e) {
      console.error('获取分类日记列表失败:', e)
      return []
    }
  },

  /**
   * 获取分类统计信息
   */
  GetCategoryStats(categoryId) {
    try {
      const diaryList = this.GetDiaryListByCategory(categoryId)
      return {
        totalCount: diaryList.length,
        recentDiary: diaryList.length > 0 ? diaryList[0] : null,
      }
    } catch (e) {
      console.error('获取分类统计失败:', e)
      return { totalCount: 0, recentDiary: null }
    }
  },

  /**
   * 获取全局统计信息
   */
  GetGlobalStats() {
    try {
      const allDiaries = this.GetDiaryList()
      const totalCount = allDiaries.length

      // 计算连续天数（任意分类今天有日记就算）
      let continueDays = 0
      if (allDiaries.length > 0) {
        const today = new Date()
        today.setHours(0, 0, 0, 0)

        // 按日期分组
        const dateMap = new Map()
        allDiaries.forEach((diary) => {
          const date = parseDate(diary.createTime)
          date.setHours(0, 0, 0, 0)
          const dateStr = date.toISOString()
          if (!dateMap.has(dateStr)) {
            dateMap.set(dateStr, true)
          }
        })

        // 获取所有有日记的日期，按时间倒序排列
        const dates = Array.from(dateMap.keys())
          .map((dateStr) => parseDate(dateStr))
          .sort((a, b) => b - a)

        // 计算连续天数
        let currentDate = today
        for (let i = 0; i < dates.length; i++) {
          const diaryDate = dates[i]
          const diffTime = currentDate - diaryDate
          const diffDays = diffTime / (1000 * 60 * 60 * 24)

          if (diffDays === 0) {
            continueDays++
            currentDate = new Date(currentDate)
            currentDate.setDate(currentDate.getDate() - 1)
            currentDate.setHours(0, 0, 0, 0)
          } else if (diffDays === 1) {
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
        continueDays,
      }
    } catch (e) {
      console.error('获取全局统计失败:', e)
      return { totalCount: 0, continueDays: 0 }
    }
  },
  
  // ==================== 设置管理 ====================
  
  /**
   * 获取日记卡片对齐方式
   * @returns {string} 'alternate' | 'default'
   */
  GetCardAlignment() {
    try {
      const alignment = uni.getStorageSync('diary_card_alignment')
      return alignment || 'alternate'
    } catch (e) {
      console.error('获取对齐方式失败:', e)
      return 'alternate'
    }
  },
  
  /**
   * 保存日记卡片对齐方式
   * @param {string} alignment - 'alternate' | 'default'
   */
  SetCardAlignment(alignment) {
    try {
      uni.setStorageSync('diary_card_alignment', alignment)
      return true
    } catch (e) {
      console.error('保存对齐方式失败:', e)
      return false
    }
  },
}




