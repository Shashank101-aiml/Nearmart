import { PRODUCT_CATEGORIES } from '../../constants/categories'

export default function CategoryGrid({ selectedCategory, onSelect }) {
  return (
    <div className="mt-4 flex flex-wrap gap-3">
      <button
        type="button"
        onClick={() => onSelect('')}
        className={`flex w-24 flex-col items-center gap-1.5 rounded-lg border p-2 text-xs font-semibold transition ${
          selectedCategory === ''
            ? 'border-accent bg-accent-bg text-accent'
            : 'border-border bg-bg text-text-h hover:border-accent-border'
        }`}
      >
        <span className="flex h-14 w-14 items-center justify-center rounded-xl bg-accent-bg text-2xl">🛒</span>
        All
      </button>
      {PRODUCT_CATEGORIES.map((c) => (
        <button
          key={c.value}
          type="button"
          onClick={() => onSelect(c.value)}
          className={`flex w-24 flex-col items-center gap-1.5 rounded-lg border p-2 text-xs font-semibold transition ${
            selectedCategory === c.value
              ? 'border-accent bg-accent-bg text-accent'
              : 'border-border bg-bg text-text-h hover:border-accent-border'
          }`}
        >
          <img src={c.image} alt="" className="h-14 w-14 rounded-xl object-cover" loading="lazy" />
          {c.label}
        </button>
      ))}
    </div>
  )
}
