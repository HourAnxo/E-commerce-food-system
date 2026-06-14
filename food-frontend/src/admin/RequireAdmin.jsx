import { Navigate, useLocation } from 'react-router-dom'
import { useAdminAuth } from '../context/AdminAuthContext.jsx'

// Gate for /admin routes — redirects to the admin login when not authenticated.
export default function RequireAdmin({ children }) {
  const { admin } = useAdminAuth()
  const location = useLocation()

  if (!admin) {
    return <Navigate to="/admin/login" replace state={{ from: location }} />
  }
  return children
}
