export function Loading() {
  return (
    <div className="flex flex-col items-center justify-center py-20">
      <div className="w-12 h-12 border-4 border-restru-200 border-t-restru-600 rounded-full animate-spin mb-4"></div>
      <p className="text-gray-500">Loading...</p>
    </div>
  )
}

export function EmptyState({ title, description, icon }: { title: string; description: string; icon: string }) {
  return (
    <div className="flex flex-col items-center justify-center py-20 text-center">
      <div className="w-20 h-20 rounded-full bg-gray-100 flex items-center justify-center text-4xl mb-4">
        {icon}
      </div>
      <h3 className="text-lg font-semibold text-gray-900">{title}</h3>
      <p className="text-gray-500 mt-2 max-w-sm">{description}</p>
    </div>
  )
}

export function Alert({ type, message }: { type: 'success' | 'error' | 'warning' | 'info'; message: string }) {
  const styles = {
    success: 'bg-food-green-50 border-food-green-200 text-food-green-800',
    error: 'bg-food-red-50 border-food-red-200 text-food-red-800',
    warning: 'bg-food-orange-50 border-food-orange-200 text-food-orange-800',
    info: 'bg-blue-50 border-blue-200 text-blue-800',
  }
  return (
    <div className={`rounded-xl border p-4 ${styles[type]}`}>
      {message}
    </div>
  )
}
