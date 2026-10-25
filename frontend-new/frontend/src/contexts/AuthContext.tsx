import { createContext, useContext, useState, useEffect, type ReactNode } from 'react'
import api from '@/api/axios'

export type User = {
  id: number
  email: string
  name: string
  phone?: string
  roles: string[]
}

type AuthContextType = {
  user: User | null
  token: string | null
  isLoading: boolean
  login: (email: string, password: string) => Promise<{ data: { token: string; user: User } }>
  register: (email: string, password: string, name: string, phone?: string, role?: string) => Promise<void>
  logout: () => void
}

const AuthContext = createContext<AuthContextType | undefined>(undefined)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null)
  const [token, setToken] = useState<string | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    const t = localStorage.getItem('bhuk_token')
    setToken(t)
    if (t) {
      api
        .get<{ data: { user: User } }>('/me')
        .then((res) => setUser(res.data.data.user))
        .catch((err) => {
          // Only clear auth on actual auth failures; transient errors shouldn't kick the user out
          if (err.response?.status === 401 || err.response?.status === 403) {
            removeAuth()
          }
        })
        .finally(() => setIsLoading(false))
    } else {
      setIsLoading(false)
    }
  }, [])

  const removeAuth = () => {
    setUser(null)
    setToken(null)
    localStorage.removeItem('bhuk_token')
  }

  const login = async (email: string, password: string) => {
    const res = await api.post<{ data: { token: string; user: User } }>('/auth/login', {
      email,
      password,
    })
    const { token: tok, user: u } = res.data.data
    setToken(tok)
    setUser(u)
    localStorage.setItem('bhuk_token', tok)
    return res.data
  }

  const register = async (
    email: string,
    password: string,
    name: string,
    phone?: string,
    role?: string,
  ) => {
    await api.post('/auth/register', { email, password, name, phone, role })
  }

  const logout = () => {
    removeAuth()
  }

  return (
    <AuthContext.Provider value={{ user, token, isLoading, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) {
    throw new Error('useAuth must be used within an AuthProvider')
  }
  return ctx
}
