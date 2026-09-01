export default function DiscountBadge({ percent }) {
  if (!percent || percent <= 0) {
    return null
  }

  return (
    <span className="rounded-md bg-accent px-1.5 py-0.5 text-xs font-semibold text-white">
      {Math.round(percent)}% OFF
    </span>
  )
}
