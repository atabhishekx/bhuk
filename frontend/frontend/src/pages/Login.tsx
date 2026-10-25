import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { useAuth } from '@/contexts/AuthContext'
import { useToastHelpers } from '@/components/Toast'
import logo from '@/assets/logo.png'

const foodImages = [
  { img: '/menu-images/001_butter_chicken.jpg', name: 'Butter Chicken' },
  { img: '/menu-images/002_chicken_tikka_masala.jpg', name: 'Chicken Tikka Masala' },
  { img: '/menu-images/003_mutton_rogan_josh.jpg', name: 'Mutton Rogan Josh' },
  { img: '/menu-images/004_paneer_butter_masala.jpg', name: 'Paneer Butter Masala' },
  { img: '/menu-images/005_kadai_paneer.jpg', name: 'Kadai Paneer' },
  { img: '/menu-images/006_palak_paneer.jpg', name: 'Palak Paneer' },
  { img: '/menu-images/007_chole_bhature.jpg', name: 'Chole Bhature' },
  { img: '/menu-images/008_dal_makhani.jpg', name: 'Dal Makhani' },
  { img: '/menu-images/009_rajma_chawal.jpg', name: 'Rajma Chawal' },
  { img: '/menu-images/010_aloo_paratha.jpg', name: 'Aloo Paratha' },
  { img: '/menu-images/011_stuffed_naan.jpg', name: 'Stuffed Naan' },
  { img: '/menu-images/012_garlic_naan.jpg', name: 'Garlic Naan' },
  { img: '/menu-images/013_tandoori_roti.jpg', name: 'Tandoori Roti' },
  { img: '/menu-images/014_lachha_paratha.jpg', name: 'Lachha Paratha' },
  { img: '/menu-images/015_steamed_rice.jpg', name: 'Steamed Rice' },
  { img: '/menu-images/016_jeera_rice.jpg', name: 'Jeera Rice' },
  { img: '/menu-images/017_veg_biryani.jpg', name: 'Veg Biryani' },
  { img: '/menu-images/018_chicken_biryani.jpg', name: 'Chicken Biryani' },
  { img: '/menu-images/019_mutton_biryani.jpg', name: 'Mutton Biryani' },
  { img: '/menu-images/020_hyderabadi_biryani.jpg', name: 'Hyderabadi Biryani' },
  { img: '/menu-images/021_masala_dosa.jpg', name: 'Masala Dosa' },
  { img: '/menu-images/022_plain_dosa.jpg', name: 'Plain Dosa' },
  { img: '/menu-images/023_rava_dosa.jpg', name: 'Rava Dosa' },
  { img: '/menu-images/024_idli_sambar.jpg', name: 'Idli Sambar' },
  { img: '/menu-images/025_medu_vada.jpg', name: 'Medu Vada' },
  { img: '/menu-images/026_uttapam.jpg', name: 'Uttapam' },
  { img: '/menu-images/027_pongal.jpg', name: 'Pongal' },
  { img: '/menu-images/028_poori_masala.jpg', name: 'Poori Masala' },
  { img: '/menu-images/029_lemon_rice.jpg', name: 'Lemon Rice' },
  { img: '/menu-images/030_curd_rice.jpg', name: 'Curd Rice' },
  { img: '/menu-images/031_samosa.jpg', name: 'Samosa' },
  { img: '/menu-images/032_kachori.jpg', name: 'Kachori' },
  { img: '/menu-images/033_aloo_tikki.jpg', name: 'Aloo Tikki' },
  { img: '/menu-images/034_paneer_pakora.jpg', name: 'Paneer Pakora' },
  { img: '/menu-images/035_onion_pakora.jpg', name: 'Onion Pakora' },
  { img: '/menu-images/036_chicken_65.jpg', name: 'Chicken 65' },
  { img: '/menu-images/037_fish_fry.jpg', name: 'Fish Fry' },
  { img: '/menu-images/038_chicken_tikka_dry.jpg', name: 'Chicken Tikka' },
  { img: '/menu-images/039_tandoori_chicken.jpg', name: 'Tandoori Chicken' },
  { img: '/menu-images/040_chicken_wings.jpg', name: 'Chicken Wings' },
  { img: '/menu-images/041_french_fries.jpg', name: 'French Fries' },
  { img: '/menu-images/042_masala_papad.jpg', name: 'Masala Papad' },
  { img: '/menu-images/043_green_salad.jpg', name: 'Green Salad' },
  { img: '/menu-images/044_raita.jpg', name: 'Raita' },
  { img: '/menu-images/045_gulab_jamun.jpg', name: 'Gulab Jamun' },
  { img: '/menu-images/046_rasmalai.jpg', name: 'Rasmalai' },
  { img: '/menu-images/047_kheer.jpg', name: 'Kheer' },
  { img: '/menu-images/048_filter_coffee.jpg', name: 'Filter Coffee' },
  { img: '/menu-images/049_masala_chai.jpg', name: 'Masala Chai' },
  { img: '/menu-images/050_fresh_lime_soda.jpg', name: 'Fresh Lime Soda' },
]

export default function Login() {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const [showPassword, setShowPassword] = useState(false)
  const [rememberMe, setRememberMe] = useState(false)
  const [fieldErrors, setFieldErrors] = useState<{ email?: string; password?: string }>({})
  const { login } = useAuth()
  const navigate = useNavigate()
  const { error: toastError } = useToastHelpers()

  const validateEmail = (value: string) => {
    if (!value) return 'Email is required'
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)) return 'Enter a valid email address'
    return ''
  }

  const validatePassword = (value: string) => {
    if (!value) return 'Password is required'
    if (value.length < 6) return 'Password must be at least 6 characters'
    return ''
  }

  const handleEmailBlur = () => {
    const err = validateEmail(email)
    setFieldErrors(prev => ({ ...prev, email: err }))
  }

  const handlePasswordBlur = () => {
    const err = validatePassword(password)
    setFieldErrors(prev => ({ ...prev, password: err }))
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    const emailErr = validateEmail(email)
    const passwordErr = validatePassword(password)
    setFieldErrors({ email: emailErr, password: passwordErr })
    if (emailErr || passwordErr) return

    setError('')
    setLoading(true)
    try {
      const res = await login(email, password)
      const roles = res.data?.user?.roles ?? []
      if (roles.includes('ADMIN')) navigate('/admin/dashboard')
      else if (roles.includes('DELIVERY_GUY')) navigate('/delivery/dashboard')
      else navigate('/menu')
    } catch (err: any) {
      const msg = err.response?.data?.error?.message || 'Login failed. Please try again.'
      setError(msg)
      toastError(msg)
    } finally {
      setLoading(false)
    }
  }

  // Split images into 3 columns
  const col1 = foodImages.filter((_, i) => i % 3 === 0)
  const col2 = foodImages.filter((_, i) => i % 3 === 1)
  const col3 = foodImages.filter((_, i) => i % 3 === 2)

  return (
    <div className="min-h-screen bg-gray-50 flex relative overflow-hidden">
      {/* Left side - Form */}
      <div className="flex-1 flex items-center justify-center px-6 py-12 relative z-10">
        {/* Background decoration */}
        <div className="absolute inset-0 -z-10" aria-hidden="true">
          <div className="absolute top-0 left-1/4 w-72 h-72 bg-bhuk-100 rounded-full blur-3xl opacity-40 animate-pulse" style={{ animationDelay: '0s' }} />
          <div className="absolute bottom-0 right-1/4 w-96 h-96 bg-food-orange-100 rounded-full blur-3xl opacity-30 animate-pulse" style={{ animationDelay: '1s' }} />
        </div>

        <div className="w-full max-w-md animate-in" style={{ animationDelay: '0ms' }}>
          {/* Logo with animated ring */}
          <div className="text-center mb-10 animate-slide-up" style={{ animationDelay: '100ms' }}>
            <div className="relative inline-block mb-6">
              <div className="absolute inset-0 bg-gradient-to-r from-bhuk-500 to-food-orange-500 rounded-full blur-xl opacity-30 animate-pulse" style={{ animationDuration: '3s' }} />
              <div className="relative w-[220px] h-[220px] rounded-full p-3 bg-gradient-to-br from-bhuk-400 via-bhuk-500 to-food-orange-500 shadow-2xl shadow-bhuk-500/40 ring-4 ring-white/70">
                <div className="w-full h-full rounded-full bg-white p-3">
                  <img src={logo} alt="Bhuk logo" className="w-full h-full rounded-full object-cover" />
                </div>
              </div>
            </div>
            <h2 className="page-title animate-slide-up" style={{ animationDelay: '200ms' }}>Welcome back</h2>
            <p className="page-subtitle animate-slide-up" style={{ animationDelay: '300ms' }}>Sign in to continue to your Bhuk account</p>
          </div>

          {/* Error Toast */}
          {error && (
            <div className="mb-6 p-4 rounded-xl bg-food-red-50 border border-food-red-200 text-food-red-700 text-sm animate-in animate-shake flex items-start gap-3" role="alert">
              <svg className="w-5 h-5 flex-shrink-0 mt-0.5" fill="currentColor" viewBox="0 0 20 20">
                <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clipRule="evenodd" />
              </svg>
              <span>{error}</span>
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-5 animate-in" style={{ animationDelay: '400ms' }}>
            {/* Email Field */}
            <div className="animate-slide-up" style={{ animationDelay: '450ms' }}>
              <label htmlFor="email" className="label">Email Address</label>
              <div className="relative">
                <svg className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400 pointer-events-none" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
                </svg>
                <input
                  id="email"
                  type="email"
                  required
                  value={email}
                  onChange={e => { setEmail(e.target.value); if (fieldErrors.email) setFieldErrors(prev => ({ ...prev, email: '' })) }}
                  onBlur={handleEmailBlur}
                  className={`input-field h-12 pl-11 pr-4 text-base ${fieldErrors.email ? 'input-field-error' : ''}`}
                  placeholder="you@example.com"
                  autoComplete="email"
                  autoFocus
                  aria-invalid={!!fieldErrors.email}
                  aria-describedby={fieldErrors.email ? 'email-error' : undefined}
                />
              </div>
              {fieldErrors.email && (
                <p id="email-error" className="field-error animate-fade-in" role="alert">{fieldErrors.email}</p>
              )}
            </div>

            {/* Password Field */}
            <div className="animate-slide-up" style={{ animationDelay: '500ms' }}>
              <label htmlFor="password" className="label">Password</label>
              <div className="relative">
                <svg className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400 pointer-events-none" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
                </svg>
                <input
                  id="password"
                  type={showPassword ? 'text' : 'password'}
                  required
                  value={password}
                  onChange={e => { setPassword(e.target.value); if (fieldErrors.password) setFieldErrors(prev => ({ ...prev, password: '' })) }}
                  onBlur={handlePasswordBlur}
                  className={`input-field h-12 pl-11 pr-12 text-base ${fieldErrors.password ? 'input-field-error' : ''}`}
                  placeholder="Enter your password"
                  autoComplete="current-password"
                  aria-invalid={!!fieldErrors.password}
                  aria-describedby={fieldErrors.password ? 'password-error' : undefined}
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute right-4 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600 transition-colors"
                  aria-label={showPassword ? 'Hide password' : 'Show password'}
                >
                  {showPassword ? (
                    <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l3.59 3.59m0 0A9.953 9.953 0 0112 5c4.478 0 8.268 2.943 9.543 7a10.025 10.025 0 01-4.132 5.411m0 0L21 21" />
                    </svg>
                  ) : (
                    <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                    </svg>
                  )}
                </button>
              </div>
              {fieldErrors.password && (
                <p id="password-error" className="field-error animate-fade-in" role="alert">{fieldErrors.password}</p>
              )}
            </div>

            {/* Remember Me & Forgot Password */}
            <div className="flex items-center justify-between animate-slide-up" style={{ animationDelay: '550ms' }}>
              <label className="flex items-center gap-2 cursor-pointer">
                <input
                  type="checkbox"
                  checked={rememberMe}
                  onChange={e => setRememberMe(e.target.checked)}
                  className="checkbox-field"
                />
                <span className="text-sm text-gray-600">Remember me</span>
              </label>
              <Link to="/forgot-password" className="text-sm text-bhuk-600 hover:text-bhuk-700 font-medium transition-colors">
                Forgot password?
              </Link>
            </div>

            {/* Submit Button */}
            <button
              type="submit"
              disabled={loading}
              className="btn btn-primary btn-lg btn-block animate-slide-up relative overflow-hidden"
              style={{ animationDelay: '600ms' }}
            >
              {loading ? (
                <>
                  <svg className="animate-spin -ml-1 mr-2 h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
                  </svg>
                  Signing in...
                </>
              ) : (
                'Sign In'
              )}
            </button>
          </form>

          {/* Divider */}
          <div className="relative my-6 animate-fade-in" style={{ animationDelay: '700ms' }}>
            <div className="absolute inset-0 flex items-center">
              <div className="w-full border-t border-gray-200" />
            </div>
            <div className="relative flex justify-center text-sm">
              <span className="px-4 bg-bhuk-cream text-gray-500">Or continue with</span>
            </div>
          </div>

          {/* Social Login Buttons */}
          <div className="grid grid-cols-2 gap-3 animate-slide-up" style={{ animationDelay: '750ms' }}>
            <button
              type="button"
              className="btn btn-outline btn-sm flex items-center justify-center gap-2 transition-all duration-200 hover:bg-gray-50 hover:border-bhuk-200"
              disabled={loading}
            >
              <svg className="w-5 h-5" viewBox="0 0 24 24">
                <path fill="currentColor" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
                <path fill="currentColor" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.72-4.54H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
                <path fill="currentColor" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/>
                <path fill="currentColor" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/>
              </svg>
              Google
            </button>
            <button
              type="button"
              className="btn btn-outline btn-sm flex items-center justify-center gap-2 transition-all duration-200 hover:bg-gray-50 hover:border-bhuk-200"
              disabled={loading}
            >
              <svg className="w-5 h-5" fill="currentColor" viewBox="0 0 24 24">
                <path d="M12 0C5.374 0 0 5.373 0 12c0 5.302 3.438 9.8 8.207 11.387.599.111.793-.261.793-.579v-2.234c-3.338.726-4.033-1.416-4.033-1.416-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.729.083-.729 1.205.084 1.839 1.237 1.839 1.237 1.07 1.834 2.807 1.304 3.492.997.107-.775.418-1.305.762-1.604-2.665-.305-5.467-1.334-5.467-5.931 0-1.311.469-2.381 1.236-3.221-.124-.303-.536-1.524.117-3.176 0 0 1.008-.322 3.301 1.23A11.509 11.509 0 0112 5.803c1.02.005 2.047.138 3.006.404 2.291-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.874.118 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.807 5.624-5.479 5.921.43.372.823 1.102.823 2.222v3.293c0 .319.192.694.801.576C20.566 21.797 24 17.3 24 12c0-6.627-5.373-12-12-12z"/>
              </svg>
              GitHub
            </button>
          </div>

          {/* Sign up link */}
          <p className="mt-6 text-center text-sm text-gray-600 animate-fade-in" style={{ animationDelay: '850ms' }}>
            Don't have an account?{' '}
            <Link to="/register" className="text-bhuk-600 font-medium hover:text-bhuk-700 transition-colors">
              Create one now
            </Link>
          </p>
        </div>
      </div>

      {/* Right side - 3 Animated Food Image Columns */}
      <div className="hidden lg:flex flex-1 relative bg-gradient-to-br from-bhuk-100 via-bhuk-50 to-food-orange-50 overflow-hidden h-screen">
        {/* Decorative blobs */}
        <div className="absolute top-0 right-0 w-96 h-96 bg-bhuk-200 rounded-full blur-3xl opacity-40 -translate-y-1/2 translate-x-1/2" />
        <div className="absolute bottom-0 left-0 w-96 h-96 bg-food-orange-100 rounded-full blur-3xl opacity-40 translate-y-1/2 -translate-x-1/2" />
        
        <div className="relative w-full h-full overflow-hidden py-8">
          {/* Left column - scrolls UP */}
          <div className="absolute left-4 top-0 flex flex-col gap-4 animate-scroll-up">
            {[...col1, ...col1].map((item, i) => (
              <div key={`col1-${i}`} className="w-44 rounded-xl bg-white/80 backdrop-blur-sm shadow-md p-3 transform transition-transform duration-300 hover:scale-105 hover:shadow-xl border border-white/50 overflow-hidden">
                <img src={item.img} alt={item.name} className="w-full h-32 object-cover rounded-lg mb-2" loading="lazy" />
                <h3 className="font-bold text-gray-900 text-xs text-center truncate">{item.name}</h3>
              </div>
            ))}
          </div>

          {/* Middle column - scrolls DOWN */}
          <div className="absolute left-1/2 -translate-x-1/2 top-0 flex flex-col gap-4 animate-scroll-down">
            {[...col2, ...col2].map((item, i) => (
              <div key={`col2-${i}`} className="w-44 rounded-xl bg-white/80 backdrop-blur-sm shadow-md p-3 transform transition-transform duration-300 hover:scale-105 hover:shadow-xl border border-white/50 overflow-hidden">
                <img src={item.img} alt={item.name} className="w-full h-32 object-cover rounded-lg mb-2" loading="lazy" />
                <h3 className="font-bold text-gray-900 text-xs text-center truncate">{item.name}</h3>
              </div>
            ))}
          </div>

          {/* Right column - scrolls UP */}
          <div className="absolute right-4 top-0 flex flex-col gap-4 animate-scroll-up" style={{ animationDelay: '2s' }}>
            {[...col3, ...col3].map((item, i) => (
              <div key={`col3-${i}`} className="w-44 rounded-xl bg-white/80 backdrop-blur-sm shadow-md p-3 transform transition-transform duration-300 hover:scale-105 hover:shadow-xl border border-white/50 overflow-hidden">
                <img src={item.img} alt={item.name} className="w-full h-32 object-cover rounded-lg mb-2" loading="lazy" />
                <h3 className="font-bold text-gray-900 text-xs text-center truncate">{item.name}</h3>
              </div>
            ))}
          </div>

          {/* Gradient overlays for fade effect */}
          <div className="absolute top-0 left-0 right-0 h-24 bg-gradient-to-b from-bhuk-100/80 to-transparent pointer-events-none z-10" />
          <div className="absolute bottom-0 left-0 right-0 h-24 bg-gradient-to-t from-food-orange-50/80 to-transparent pointer-events-none z-10" />
          
          {/* Center badge */}
          <div className="absolute bottom-8 left-1/2 -translate-x-1/2 bg-white/90 backdrop-blur-sm px-6 py-3 rounded-full shadow-lg border border-white/50 animate-bounce-slow z-10">
            <p className="text-sm font-medium text-gray-700">🍽️ Over <span className="text-bhuk-600 font-bold">50+</span> delicious recipes</p>
          </div>
        </div>
      </div>
    </div>
  )
}