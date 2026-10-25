import { type ReactNode } from 'react'
import Navbar from './Navbar'

interface PageLayoutProps {
  children: ReactNode
  title?: string
  subtitle?: string
  cartCount?: number
  header?: ReactNode
  className?: string
}

export default function PageLayout({
  children,
  title,
  subtitle,
  cartCount = 0,
  header,
  className = ''
}: PageLayoutProps) {
  return (
    <div className={`page ${className}`}>
      <header className="page-header">
        <Navbar cartCount={cartCount} />
      </header>
      <main className="page-content">
        {(title || header) && (
          <div className="mb-6 animate-in">
            {header || (
              <>
                <h1 className="page-title">{title}</h1>
                {subtitle && <p className="page-subtitle">{subtitle}</p>}
              </>
            )}
          </div>
        )}
        {children}
      </main>
    </div>
  )
}