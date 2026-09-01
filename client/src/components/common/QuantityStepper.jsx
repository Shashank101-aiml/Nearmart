export default function QuantityStepper({ quantity, onIncrement, onDecrement, disabled = false }) {
  return (
    <div className="flex items-center gap-2 rounded-md bg-accent px-2 py-1.5 text-white">
      <button
        type="button"
        onClick={onDecrement}
        disabled={disabled}
        aria-label="Decrease quantity"
        className="w-4 cursor-pointer font-bold disabled:cursor-not-allowed disabled:opacity-60"
      >
        &minus;
      </button>
      <span className="w-4 text-center text-sm font-semibold">{quantity}</span>
      <button
        type="button"
        onClick={onIncrement}
        disabled={disabled}
        aria-label="Increase quantity"
        className="w-4 cursor-pointer font-bold disabled:cursor-not-allowed disabled:opacity-60"
      >
        +
      </button>
    </div>
  )
}
