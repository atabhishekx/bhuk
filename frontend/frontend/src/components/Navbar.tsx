import { useState, useEffect } from 'react'
import { Link, useLocation } from 'react-router-dom'
import { useAuth } from '@/contexts/AuthContext'
import logo from '@/assets/logo.png'

interface NavbarProps { cartCount?: number }

export default function Navbar({ cartCount = 0 }: NavbarProps) {
  const { user, logout } = useAuth()
  const location = useLocation()
  const [mobileOpen, setMobileOpen] = useState(false)
  const [scrolled, setScrolled] = useState(false)
  const isAdmin = user?.roles.includes('ADMIN')
  const isDeliveryGuy = user?.roles.includes('DELIVERY_GUY')
  const linkClass = (path: string) => `bhuk-nav-link ${location.pathname === path ? 'active' : ''}`
  const close = () => setMobileOpen(false)

  useEffect(() => {
    const handleScroll = () => setScrolled(window.scrollY > 8)
    window.addEventListener('scroll', handleScroll, { passive: true })
    return () => window.removeEventListener('scroll', handleScroll)
  }, [])

  useEffect(() => {
    if (mobileOpen) {
      document.body.style.overflow = 'hidden'
    } else {
      document.body.style.overflow = ''
    }
    return () => { document.body.style.overflow = '' }
  }, [mobileOpen])

  return (
    <header className={`bhuk-header ${scrolled ? 'shadow-md' : ''}`}>
      <nav className="bhuk-nav-shell" aria-label="Main navigation">
        <Link to={isAdmin ? '/admin/dashboard' : isDeliveryGuy ? '/delivery/dashboard' : '/menu'} className="bhuk-brand" onClick={close} aria-label="Bhuk Home">
          <span className="bhuk-logo-wrap"><img src={logo} alt="" className="bhuk-logo" /></span>
          <span className="bhuk-brand-name">bhuk<span>.</span><small>GOOD FOOD. GOOD MOOD.</small></span>
        </Link>

        <div className={`bhuk-nav-links ${mobileOpen ? 'is-open' : ''}`} role="navigation" aria-label="Main menu">
          {!isAdmin && !isDeliveryGuy && <>
            <Link to="/menu" className={linkClass('/menu')} onClick={close}>Explore menu</Link>
            <Link to="/orders" className={linkClass('/orders')} onClick={close}>My orders</Link>
            <Link to="/profile" className={linkClass('/profile')} onClick={close}>My profile</Link>
          </>}
          {isDeliveryGuy && <Link to="/delivery/dashboard" className={linkClass('/delivery/dashboard')} onClick={close}>Delivery dashboard</Link>}
          {isAdmin && <>
            <Link to="/admin/dashboard" className={linkClass('/admin/dashboard')} onClick={close}>Overview</Link>
            <Link to="/admin/orders" className={linkClass('/admin/orders')} onClick={close}>Orders</Link>
            <Link to="/admin/menu" className={linkClass('/admin/menu')} onClick={close}>Menu manager</Link>
          </>}
        </div>

        <div className="bhuk-nav-actions">
          {!isAdmin && !isDeliveryGuy && <Link to="/cart" className="bhuk-cart-link" aria-label={`Cart with ${cartCount} items`}>
            <span className="bhuk-cart-icon" aria-hidden="true">⌑</span><span className="bhuk-cart-label">Cart</span>
            {cartCount > 0 && <span className="bhuk-cart-count" aria-label={`${cartCount} items in cart`}>{cartCount > 99 ? '99+' : cartCount}</span>}
          </Link>}
          {user && (
            <div className="bhuk-user-chip">
              <span className="bhuk-user-avatar" aria-hidden="true">{(user?.name || 'B').trim().charAt(0).toUpperCase()}</span>
              <span className="bhuk-user-meta"><strong>{user?.name || 'Welcome'}</strong><small>{isAdmin ? 'Administrator' : isDeliveryGuy ? 'Delivery partner' : 'Food lover'}</small></span>
            </div>
          )}
          {user && <button className="bhuk-signout" onClick={logout} type="button">Sign out <span aria-hidden="true">↗</span></button>}
          <button className="bhuk-mobile-toggle" type="button" aria-label={mobileOpen ? 'Close navigation' : 'Open navigation'} aria-expanded={mobileOpen} aria-controls="mobile-menu" onClick={() => setMobileOpen(v => !v)}>{mobileOpen ? '×' : '☰'}</button>
        </div>
      </nav>
      {mobileOpen && <div className="bhuk-mobile-overlay" onClick={close} aria-hidden="true" />}
    </header>
  )
}
