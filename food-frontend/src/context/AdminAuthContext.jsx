import { createContext, useContext, useEffect, useState } from 'react'
import { adminApi } from '../api/services'

// Admin session backed by POST /api/admins/login. The authenticated admin is
// persisted in localStorage (without the password) and gates the /admin routes.
const AdminAuthContext = createContext(null)

const STORAGE_KEY = 'foodapp.admin'

export function AdminAuthProvider({ children }) {
  const [admin, setAdmin] = useState(() => {
    const saved = localStorage.getItem(STORAGE_KEY)
    return saved ? JSON.parse(saved) : null
  })

  useEffect(() => {
    if (admin) {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(admin))
    } else {
      localStorage.removeItem(STORAGE_KEY)
    }
  }, [admin])

  const login = async (email, password) => {
    const res = await adminApi.login(email, password)
    // Drop the password before persisting.
    const safe = { ...res.data }
    delete safe.password
    setAdmin(safe)
    return safe
  }

  const logout = () => setAdmin(null)

  return (
    <AdminAuthContext.Provider value={{ admin, login, logout }}>
      {children}
    </AdminAuthContext.Provider>
  )
}

// eslint-disable-next-line react-refresh/only-export-components
export function useAdminAuth() {
  return useContext(AdminAuthContext)
}
