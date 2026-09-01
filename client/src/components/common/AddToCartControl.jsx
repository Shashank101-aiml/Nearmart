import QuantityStepper from './QuantityStepper'

export default function AddToCartControl({ quantity = 0, onAdd, onIncrement, onDecrement, disabled = false }) {
  if (quantity > 0) {
    return (
      <QuantityStepper quantity={quantity} onIncrement={onIncrement} onDecrement={onDecrement} disabled={disabled} />
    )
  }

  return (
    <button
      type="button"
      onClick={onAdd}
      disabled={disabled}
      className="cursor-pointer rounded-md border border-accent px-4 py-1.5 text-sm font-semibold text-accent disabled:cursor-not-allowed disabled:opacity-60"
    >
      ADD
    </button>
  )
}
