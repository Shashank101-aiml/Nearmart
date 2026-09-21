export const PRODUCT_CATEGORIES = [
  { value: 'PAAN_CORNER', label: 'Paan Corner', icon: '🍃' },
  { value: 'DAIRY_EGGS', label: 'Dairy, Bread & Eggs', icon: '🥛' },
  { value: 'VEGETABLES', label: 'Fruits & Vegetables', icon: '🍎' },
  { value: 'COLD_DRINKS', label: 'Cold Drinks & Juices', icon: '🧃' },
  { value: 'SNACKS', label: 'Snacks & Munchies', icon: '🍿' },
  { value: 'BREAKFAST_INSTANT_FOOD', label: 'Breakfast & Instant Food', icon: '🥣' },
  { value: 'SWEET_TOOTH', label: 'Sweet Tooth', icon: '🍫' },
  { value: 'BAKERY_BISCUITS', label: 'Bakery & Biscuits', icon: '🍪' },
  { value: 'TEA_COFFEE_MILK_DRINKS', label: 'Tea, Coffee & Milk Drinks', icon: '☕' },
  { value: 'ATTA_RICE_DAL', label: 'Atta, Rice & Dal', icon: '🌾' },
  { value: 'MASALA_OIL_MORE', label: 'Masala, Oil & More', icon: '🧂' },
  { value: 'SAUCES_SPREADS', label: 'Sauces & Spreads', icon: '🍯' },
  { value: 'CHICKEN_MEAT_FISH', label: 'Chicken, Meat & Fish', icon: '🍗' },
  { value: 'ORGANIC_HEALTHY_LIVING', label: 'Organic & Healthy Living', icon: '🌱' },
  { value: 'BABY_CARE', label: 'Baby Care', icon: '🍼' },
  { value: 'PHARMACY', label: 'Pharma & Wellness', icon: '💊' },
  { value: 'CLEANING_ESSENTIALS', label: 'Cleaning Essentials', icon: '🧹' },
  { value: 'HOME_OFFICE', label: 'Home & Office', icon: '🏠' },
  { value: 'PERSONAL_CARE', label: 'Personal Care', icon: '🧴' },
  { value: 'PET_CARE', label: 'Pet Care', icon: '🐶' },
]

export function categoryLabel(value) {
  return PRODUCT_CATEGORIES.find((c) => c.value === value)?.label || ''
}
