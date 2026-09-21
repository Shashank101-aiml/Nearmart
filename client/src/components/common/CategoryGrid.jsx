import { PRODUCT_CATEGORIES } from '../../constants/categories'

export default function CategoryGrid({ selectedCategory, onSelect }) {
  return (
    <div className="mt-4 flex flex-wrap gap-3">
      <button
        type="button"
        onClick={() => onSelect('')}
        className={`flex flex-col items-center gap-1.5 rounded-lg border px-4 py-3 text-xs font-semibold transition ${
          selectedCategory === ''
            ? 'border-accent bg-accent-bg text-accent'
            : 'border-border bg-bg text-text-h hover:border-accent-border'
        }`}
      >
        <span className="text-2xl">🛒</span>
        All
      </button>
      {PRODUCT_CATEGORIES.map((c) => (
        <button
          key={c.value}
          type="button"
          onClick={() => onSelect(c.value)}
          className={`flex flex-col items-center gap-1.5 rounded-lg border px-4 py-3 text-xs font-semibold transition ${
            selectedCategory === c.value
              ? 'border-accent bg-accent-bg text-accent'
              : 'border-border bg-bg text-text-h hover:border-accent-border'
          }`}
        >
          <span className="text-2xl">{c.icon}</span>
          {c.label}
        </button>
      ))}
    </div>
  )
}
