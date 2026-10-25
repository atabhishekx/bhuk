import { type ReactNode } from 'react'

export function Loading() {
  return (
    <div className="flex flex-col items-center justify-center py-20">
      <div className="w-12 h-12 border-4 border-bhuk-200 border-t-bhuk-600 rounded-full animate-spin mb-4"></div>
      <p className="text-bhuk-muted">Loading...</p>
    </div>
  )
}

export function EmptyState({ title, description, icon }: { title: string; description: string; icon: string }) {
  return (
    <div className="empty-state">
      <div className="empty-state-icon">{icon}</div>
      <h3 className="empty-state-title">{title}</h3>
      <p className="empty-state-desc">{description}</p>
    </div>
  )
}

export function Alert({ type, message, onClose }: { type: 'success' | 'error' | 'warning' | 'info'; message: string; onClose?: () => void }) {
  const styles = {
    success: 'bg-food-green-50 border-food-green-200 text-food-green-800',
    error: 'bg-food-red-50 border-food-red-200 text-food-red-800',
    warning: 'bg-food-orange-50 border-food-orange-200 text-food-orange-800',
    info: 'bg-blue-50 border-blue-200 text-blue-800',
  }
  return (
    <div className={`rounded-xl border p-4 ${styles[type]} flex items-start justify-between gap-3 animate-in`}>
      <span>{message}</span>
      {onClose && <button onClick={onClose} className="p-1 text-current opacity-50 hover:opacity-100" aria-label="Dismiss">✕</button>}
    </div>
  )
}

export function Skeleton({ className = '', ...props }: React.HTMLAttributes<HTMLDivElement>) {
  return <div className={`skeleton ${className}`} {...props} />
}

export function SkeletonCard() {
  return (
    <div className="card p-5 space-y-3 animate-in">
      <Skeleton className="h-32 w-full rounded-xl" />
      <Skeleton className="h-4 w-3/4" />
      <Skeleton className="h-4 w-1/2" />
      <Skeleton className="h-10 w-1/4 rounded-lg" />
    </div>
  )
}

export function PageHeader({ title, subtitle, action }: { title: string; subtitle?: string; action?: ReactNode }) {
  return (
    <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 mb-6 animate-in">
      <div>
        <h1 className="page-title">{title}</h1>
        {subtitle && <p className="page-subtitle">{subtitle}</p>}
      </div>
      {action && <div className="flex-shrink-0">{action}</div>}
    </div>
  )
}

export function Divider() {
  return <hr className="divider" />
}