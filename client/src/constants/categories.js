export const PRODUCT_CATEGORIES = [
  { value: 'FRESH_PRODUCE', label: 'Fresh Produce', icon: '🍎' },
  { value: 'VEGETABLES', label: 'Vegetables', icon: '🥦' },
  { value: 'DAIRY_EGGS', label: 'Dairy & Eggs', icon: '🥛' },
  { value: 'SNACKS', label: 'Snacks & Munchies', icon: '🍿' },
  { value: 'COLD_DRINKS', label: 'Cold Drinks', icon: '🧃' },
  { value: 'PHARMACY', label: 'Medicines', icon: '💊' },
  { value: 'PET_CARE', label: 'Pet Care', icon: '🐶' },
]

export function categoryLabel(value) {
  return PRODUCT_CATEGORIES.find((c) => c.value === value)?.label || ''
}
