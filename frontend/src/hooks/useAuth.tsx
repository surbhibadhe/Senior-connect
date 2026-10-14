import {
  createContext,
  useContext,
  useEffect,
  useState,
  useCallback,
  type ReactNode,
} from 'react'
import type { UserProfileResponse, ProfileUpdateRequest } from '../types'
import * as auth from '../services/authService'

interface AuthContextType {
  user: UserProfileResponse | null
  loading: boolean
  signIn: () => void
  signOut: () => Promise<void>
  refreshUser: () => Promise<UserProfileResponse | null>
  updateProfile: (data: ProfileUpdateRequest) => Promise<UserProfileResponse>
}

const AuthContext = createContext<AuthContextType | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<UserProfileResponse | null>(null)
  const [loading, setLoading] = useState(true)

  const refreshUser = useCallback(async () => {
    try {
      const u = await auth.currentUser()
      setUser(u)
      return u
    } catch {
      setUser(null)
      return null
    }
  }, [])

  useEffect(() => {
    refreshUser().finally(() => setLoading(false))
  }, [refreshUser])

  const handleSignIn = () => {
    auth.signIn()
  }

  const handleSignOut = async () => {
    await auth.signOut()
    setUser(null)
    window.location.href = '/'
  }

  const handleUpdateProfile = async (data: ProfileUpdateRequest) => {
    const updated = await auth.updateProfile(data)
    setUser(updated)
    return updated
  }

  const value: AuthContextType = {
    user,
    loading,
    signIn: handleSignIn,
    signOut: handleSignOut,
    refreshUser,
    updateProfile: handleUpdateProfile,
  }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth(): AuthContextType {
  const ctx = useContext(AuthContext)
  if (!ctx) {
    throw new Error('useAuth must be used within an AuthProvider')
  }
  return ctx
}
