// 分类管理 Hook
import { ref } from 'vue'
import storage from '../../../utils/storage.js'

export function useCategories() {
  // 分类列表
  const categories = ref([])

  // 加载分类数据
  const loadCategories = () => {
    categories.value = storage.GetCategoryList()
    console.log('LoadCategories - 分类数据加载完成:', categories.value)
  }

  // 获取分类名称
  const getCategoryName = (categoryId) => {
    if (!categoryId) return '默认'
    
    const category = categories.value.find(cat => cat.id === categoryId)
    return category ? category.name : '未知分类'
  }

  // 获取分类颜色
  const getCategoryColor = (categoryId) => {
    if (!categoryId) return '#FF9A76'
    
    const category = categories.value.find(cat => cat.id === categoryId)
    return category ? category.color : '#FF9A76'
  }

  // 检查分类是否存在
  const categoryExists = (categoryId) => {
    return categories.value.some(cat => cat.id === categoryId)
  }

  return {
    categories,
    loadCategories,
    getCategoryName,
    getCategoryColor,
    categoryExists
  }
}