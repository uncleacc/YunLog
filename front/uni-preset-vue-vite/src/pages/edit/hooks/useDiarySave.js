// 日记保存管理 Hook
import { ref } from 'vue'
import storage from '../../../utils/storage.js'

export function useDiarySave() {
  // 保存状态
  const isSaving = ref(false)

  // 生成日记ID
  const generateDiaryId = () => {
    return Date.now().toString()
  }

  // 格式化日期
  const formatDate = (date = new Date()) => {
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    const hours = String(date.getHours()).padStart(2, '0')
    const minutes = String(date.getMinutes()).padStart(2, '0')
    
    return `${year}-${month}-${day} ${hours}:${minutes}`
  }

  // 保存新日记
  const saveNewDiary = async (title, content, categoryId = null) => {
    if (isSaving.value) {
      console.log('SaveNewDiary - 正在保存中，跳过重复保存')
      return { success: false, message: '正在保存中，请稍候' }
    }

    try {
      isSaving.value = true
      
      const diary = {
        id: generateDiaryId(),
        title: title.trim() || '无标题',
        content: content,
        categoryId: categoryId,
        createTime: formatDate(),
        updateTime: formatDate()
      }

      const result = storage.AddDiary(diary)
      
      if (result) {
        console.log('SaveNewDiary - 新日记保存成功:', result.id)
        return { success: true, diary: result, message: '保存成功' }
      } else {
        console.error('SaveNewDiary - 保存失败')
        return { success: false, message: '保存失败' }
      }
    } catch (error) {
      console.error('SaveNewDiary - 保存异常:', error)
      return { success: false, message: '保存失败，请重试' }
    } finally {
      isSaving.value = false
    }
  }

  // 更新现有日记
  const updateExistingDiary = async (diary, title, content, categoryId = null) => {
    if (isSaving.value) {
      console.log('UpdateExistingDiary - 正在保存中，跳过重复保存')
      return { success: false, message: '正在保存中，请稍候' }
    }

    try {
      isSaving.value = true
      
      const updatedDiary = {
        ...diary,
        title: title.trim() || '无标题',
        content: content,
        categoryId: categoryId,
        updateTime: formatDate()
      }

      const result = storage.UpdateDiary(diary.id, updatedDiary)
      
      if (result) {
        console.log('UpdateExistingDiary - 日记更新成功:', diary.id)
        return { success: true, diary: result, message: '更新成功' }
      } else {
        console.error('UpdateExistingDiary - 更新失败')
        return { success: false, message: '更新失败' }
      }
    } catch (error) {
      console.error('UpdateExistingDiary - 更新异常:', error)
      return { success: false, message: '更新失败，请重试' }
    } finally {
      isSaving.value = false
    }
  }

  // 自动保存逻辑
  const autoSave = async (currentDiary, title, content, categoryId, isEditing) => {
    console.log('AutoSave - 开始自动保存')
    
    let result
    if (isEditing && currentDiary) {
      result = await updateExistingDiary(currentDiary, title, content, categoryId)
    } else {
      result = await saveNewDiary(title, content, categoryId)
    }
    
    if (result.success) {
      console.log('AutoSave - 自动保存成功')
      return result
    } else {
      console.error('AutoSave - 自动保存失败:', result.message)
      return result
    }
  }

  // 检查内容是否有变化
  const hasContentChanged = (currentDiary, newTitle, newContent, newCategoryId) => {
    if (!currentDiary) return true // 新日记总是认为有变化
    
    return (
      currentDiary.title !== (newTitle.trim() || '无标题') ||
      currentDiary.content !== newContent ||
      currentDiary.categoryId !== newCategoryId
    )
  }

  return {
    isSaving,
    saveNewDiary,
    updateExistingDiary,
    autoSave,
    hasContentChanged
  }
}