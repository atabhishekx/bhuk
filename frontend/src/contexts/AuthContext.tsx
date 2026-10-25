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
  login: (email: string, password: string) => Promise<void>
  register: (email: string, password: string, name: string, phone?: string, role?: string) => Promise<void>
  logout: () => void
}

const AuthContext = createContext<AuthContextType | undefined>(undefined)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null)
  const [token, setToken] = useState<string | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    const t = localStorage.getItem('restru_token')
    setToken(t)
    setIsLoading(false)
  }, [])

  const removeAuth = () => {
    setUser(null)
    setToken(null)
    localStorage.removeItem('restru_token')
  }

  const login = async (email: string, password: string) => {
    const res = await api.post<{ data: { token: string; user: User } }>('/auth/login', {
      email,
      password,
    })
    const { token: tok, user: u } = res.data.data
    setToken(tok)
    setUser(u)
    localStorage.setItem('restru_token', tok)
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
