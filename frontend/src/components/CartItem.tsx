interface CartItemProps {
  item: any
  onRemove: () => void
  onQtyChange: (qty: number) => void
}

export default function CartItem({ item, onRemove, onQtyChange }: CartItemProps) {
  const lineTotal = (item.basePriceMinor + (item.modifiers?.reduce((sum: number, m: any) => sum + (m.priceDeltaMinor || 0), 0) || 0)) * item.quantity

  return (
    <div className="flex items-center gap-4 p-4 border-b border-gray-100 last:border-0">
      <div className="w-16 h-16 rounded-xl bg-gray-100 flex items-center justify-center overflow-hidden relative">
        <span className="absolute inset-0 flex items-center justify-center text-gray-400 text-xl">
          🍔
        </span>
      </div>
      <div className="flex-1 min-w-0">
        <h4 className="font-semibold text-gray-900 truncate">{item.itemName}</h4>
        <p className="text-sm text-gray-500 mt-1">
          {item.modifiers?.map((m: any) => m.modifierName).join(', ')}
        </p>
      </div>
      <div className="flex items-center gap-4">
        <div className="flex items-center border border-gray-200 rounded-lg overflow-hidden">
          <button
            onClick={() => onQtyChange(Math.max(1, item.quantity - 1))}
            className="px-3 py-1 bg-gray-50 hover:bg-gray-100 border-r border-gray-200 text-gray-600"
          >
            −
          </button>
          <span className="px-3 py-1 font-medium text-gray-900">{item.quantity}</span>
          <button
            onClick={() => onQtyChange(item.quantity + 1)}
            className="px-3 py-1 bg-gray-50 hover:bg-gray-100 border-l border-gray-200 text-gray-600"
          >
            +
          </button>
        </div>
        <span className="font-semibold text-restru-600">₹{(lineTotal / 100).toFixed(2)}</span>
        <button
          onClick={onRemove}
          className="text-food-red-500 hover:text-food-red-700 text-sm font-medium"
        >
          Remove
        </button>
      </div>
    </div>
  )
}
