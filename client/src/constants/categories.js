const UNSPLASH_PARAMS = 'w=200&h=200&fit=crop&auto=format&q=70'

export const PRODUCT_CATEGORIES = [
  {
    value: 'PAAN_CORNER',
    label: 'Paan Corner',
    icon: '🍃',
    image: `https://images.unsplash.com/photo-1717429863975-4d45cbdb3d42?${UNSPLASH_PARAMS}`,
  },
  {
    value: 'DAIRY_EGGS',
    label: 'Dairy, Bread & Eggs',
    icon: '🥛',
    image: `https://images.unsplash.com/photo-1497581175344-8a5f1a0142a5?${UNSPLASH_PARAMS}`,
  },
  {
    value: 'VEGETABLES',
    label: 'Fruits & Vegetables',
    icon: '🍎',
    image: `https://images.unsplash.com/photo-1488459716781-31db52582fe9?${UNSPLASH_PARAMS}`,
  },
  {
    value: 'COLD_DRINKS',
    label: 'Cold Drinks & Juices',
    icon: '🧃',
    image: `https://images.unsplash.com/photo-1568657624422-1b8713e79461?${UNSPLASH_PARAMS}`,
  },
  {
    value: 'SNACKS',
    label: 'Snacks & Munchies',
    icon: '🍿',
    image: `https://images.unsplash.com/photo-1641693148759-843d17ceac24?${UNSPLASH_PARAMS}`,
  },
  {
    value: 'BREAKFAST_INSTANT_FOOD',
    label: 'Breakfast & Instant Food',
    icon: '🥣',
    image: `https://images.unsplash.com/photo-1521483451569-e33803c0330c?${UNSPLASH_PARAMS}`,
  },
  {
    value: 'SWEET_TOOTH',
    label: 'Sweet Tooth',
    icon: '🍫',
    image: `https://images.unsplash.com/photo-1599599810769-bcde5a160d32?${UNSPLASH_PARAMS}`,
  },
  {
    value: 'BAKERY_BISCUITS',
    label: 'Bakery & Biscuits',
    icon: '🍪',
    image: `https://images.unsplash.com/photo-1576717585968-8ea8166b89b8?${UNSPLASH_PARAMS}`,
  },
  {
    value: 'TEA_COFFEE_MILK_DRINKS',
    label: 'Tea, Coffee & Milk Drinks',
    icon: '☕',
    image: `https://images.unsplash.com/photo-1643316798735-187fc442febb?${UNSPLASH_PARAMS}`,
  },
  {
    value: 'ATTA_RICE_DAL',
    label: 'Atta, Rice & Dal',
    icon: '🌾',
    image: `https://images.unsplash.com/photo-1704916029292-ec7b5976204c?${UNSPLASH_PARAMS}`,
  },
  {
    value: 'MASALA_OIL_MORE',
    label: 'Masala, Oil & More',
    icon: '🧂',
    image: `https://images.unsplash.com/photo-1596040033229-a9821ebd058d?${UNSPLASH_PARAMS}`,
  },
  {
    value: 'SAUCES_SPREADS',
    label: 'Sauces & Spreads',
    icon: '🍯',
    image: `https://images.unsplash.com/photo-1638697586690-37f66f05083a?${UNSPLASH_PARAMS}`,
  },
  {
    value: 'CHICKEN_MEAT_FISH',
    label: 'Chicken, Meat & Fish',
    icon: '🍗',
    image: `https://images.unsplash.com/photo-1587593810167-a84920ea0781?${UNSPLASH_PARAMS}`,
  },
  {
    value: 'ORGANIC_HEALTHY_LIVING',
    label: 'Organic & Healthy Living',
    icon: '🌱',
    image: `https://images.unsplash.com/photo-1512621776951-a57141f2eefd?${UNSPLASH_PARAMS}`,
  },
  {
    value: 'BABY_CARE',
    label: 'Baby Care',
    icon: '🍼',
    image: `https://images.unsplash.com/photo-1716972065448-e08a46809530?${UNSPLASH_PARAMS}`,
  },
  {
    value: 'PHARMACY',
    label: 'Pharma & Wellness',
    icon: '💊',
    image: `https://images.unsplash.com/photo-1628771065518-0d82f1938462?${UNSPLASH_PARAMS}`,
  },
  {
    value: 'CLEANING_ESSENTIALS',
    label: 'Cleaning Essentials',
    icon: '🧹',
    image: `https://images.unsplash.com/photo-1528740561666-dc2479dc08ab?${UNSPLASH_PARAMS}`,
  },
  {
    value: 'HOME_OFFICE',
    label: 'Home & Office',
    icon: '🏠',
    image: `https://images.unsplash.com/photo-1676282831194-f7dcd46eafef?${UNSPLASH_PARAMS}`,
  },
  {
    value: 'PERSONAL_CARE',
    label: 'Personal Care',
    icon: '🧴',
    image: `https://images.unsplash.com/photo-1629380108599-ea06489d66f5?${UNSPLASH_PARAMS}`,
  },
  {
    value: 'PET_CARE',
    label: 'Pet Care',
    icon: '🐶',
    image: `https://images.unsplash.com/photo-1568640347023-a616a30bc3bd?${UNSPLASH_PARAMS}`,
  },
]

export function categoryLabel(value) {
  return PRODUCT_CATEGORIES.find((c) => c.value === value)?.label || ''
}
