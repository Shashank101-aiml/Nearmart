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
    <div className="product-filters">
      <input
        type="text"
        placeholder="Search products..."
        value={searchTerm}
        onChange={(e) => onSearchChange(e.target.value)}
        aria-label="Search products"
      />

      <select value={selectedVendorId} onChange={(e) => onVendorChange(e.target.value)} aria-label="Filter by vendor">
        <option value="">All vendors</option>
        {vendorOptions.map((vendor) => (
          <option key={vendor.id} value={vendor.id}>
            {vendor.name}
          </option>
        ))}
      </select>

      <div className="price-range">
        <input
          type="number"
          placeholder="Min price"
          value={minPrice}
          onChange={(e) => onMinPriceChange(e.target.value)}
          min="0"
          aria-label="Minimum price"
        />
        <span>&ndash;</span>
        <input
          type="number"
          placeholder="Max price"
          value={maxPrice}
          onChange={(e) => onMaxPriceChange(e.target.value)}
          min="0"
          aria-label="Maximum price"
        />
      </div>

      <label className="checkbox-label">
        <input
          type="checkbox"
          checked={inStockOnly}
          onChange={(e) => onInStockOnlyChange(e.target.checked)}
        />
        In stock only
      </label>

      {hasActiveFilters && (
        <button type="button" onClick={onClear}>
          Clear filters
        </button>
      )}
    </div>
  )
}
