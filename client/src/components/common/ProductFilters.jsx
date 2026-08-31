const fieldClasses = 'rounded-md border border-border bg-bg px-2.5 py-2 text-text-h'

export default function ProductFilters({
  searchTerm,
  onSearchChange,
  vendorOptions,
  selectedVendorId,
  onVendorChange,
  minPrice,
  maxPrice,
  onMinPriceChange,
  onMaxPriceChange,
  inStockOnly,
  onInStockOnlyChange,
  onClear,
  hasActiveFilters,
}) {
  return (
    <div className="mt-4 flex flex-wrap items-center gap-3 rounded-lg border border-border p-4">
      <input
        type="text"
        placeholder="Search products..."
        value={searchTerm}
        onChange={(e) => onSearchChange(e.target.value)}
        aria-label="Search products"
        className={`min-w-40 flex-1 ${fieldClasses}`}
      />

      {vendorOptions && (
        <select
          value={selectedVendorId}
          onChange={(e) => onVendorChange(e.target.value)}
          aria-label="Filter by vendor"
          className={fieldClasses}
        >
          <option value="">All vendors</option>
          {vendorOptions.map((vendor) => (
            <option key={vendor.id} value={vendor.id}>
              {vendor.name}
            </option>
          ))}
        </select>
      )}

      <div className="flex items-center gap-1.5 text-text">
        <input
          type="number"
          placeholder="Min price"
          value={minPrice}
          onChange={(e) => onMinPriceChange(e.target.value)}
          min="0"
          aria-label="Minimum price"
          className={`w-25 ${fieldClasses}`}
        />
        <span>&ndash;</span>
        <input
          type="number"
          placeholder="Max price"
          value={maxPrice}
          onChange={(e) => onMaxPriceChange(e.target.value)}
          min="0"
          aria-label="Maximum price"
          className={`w-25 ${fieldClasses}`}
        />
      </div>

      <label className="flex items-center gap-1.5 whitespace-nowrap text-sm text-text-h">
        <input
          type="checkbox"
          checked={inStockOnly}
          onChange={(e) => onInStockOnlyChange(e.target.checked)}
        />
        In stock only
      </label>

      {hasActiveFilters && (
        <button
          type="button"
          onClick={onClear}
          className="cursor-pointer whitespace-nowrap rounded-md border border-border bg-bg px-3.5 py-2 text-text-h"
        >
          Clear filters
        </button>
      )}
    </div>
  )
}
