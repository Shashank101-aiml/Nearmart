export default function PriceDisplay({ price, mrp }) {
  const hasDiscount = mrp && mrp > price

  return (
    <span className="flex items-baseline gap-1.5">
      <span className="font-semibold text-text-h">${price.toFixed(2)}</span>
      {hasDiscount && <span className="text-sm text-text line-through">${mrp.toFixed(2)}</span>}
    </span>
  )
}
