import { createContext, useContext, useEffect, useState } from 'react'

// The backend has no password/token auth, so "login" simply resolves a
// customer record by email and persists it in localStorage as the
// current user. Register creates the customer then logs them in.
const AuthContext = createContext(null)

const STORAGE_KEY = 'foodapp.customer'

export function AuthProvider({ children }) {
  const [customer, setCustomer] = useState(() => {
    const saved = localStorage.getItem(STORAGE_KEY)
    return saved ? JSON.parse(saved) : null
  })

  useEffect(() => {
    if (customer) {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(customer))
    } else {
      localStorage.removeItem(STORAGE_KEY)
    }
  }, [customer])

  const login = (customerData) => setCustomer(customerData)
  const logout = () => setCustomer(null)

  return (
    <AuthContext.Provider value={{ customer, login, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

// eslint-disable-next-line react-refresh/only-export-components
export function useAuth() {
  return useContext(AuthContext)
}
