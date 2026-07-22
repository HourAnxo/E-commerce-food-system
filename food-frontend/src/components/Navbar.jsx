import { Link, NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'
import { useCart } from '../context/CartContext.jsx'

export default function Navbar() {
  const { customer, logout } = useAuth()
  const { totalItems } = useCart()
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    navigate('/')
  }

  return (
    <header className="navbar">
      <div className="navbar-inner container">
        <Link to="/" className="brand">
          <svg className="brand-logo" viewBox="0 0 48 48" aria-hidden="true">
            <g
              stroke="currentColor"
              strokeWidth="2.4"
              strokeLinecap="round"
              fill="none"
              opacity="0.9"
            >
              <path d="M19 20c-2.4-2.6-2.4-4.4 0-7 2.4-2.6 2.4-4.4 0-7" />
              <path d="M29 20c-2.4-2.6-2.4-4.4 0-7 2.4-2.6 2.4-4.4 0-7" />
            </g>
            <path d="M11 25.5h26a13 13 0 0 1-26 0Z" fill="currentColor" />
            <ellipse cx="24" cy="25.5" rx="13.6" ry="3.2" fill="currentColor" />
          </svg>
          food<span className="brand-accent">.</span>
        </Link>

        <nav className="nav-links">
          <NavLink to="/" end>
            Home
          </NavLink>
          <NavLink to="/products">Products</NavLink>
          <NavLink to="/admin">Admin</NavLink>
          <NavLink to="/cart" className="cart-link">
            Cart
            {totalItems > 0 && <span className="cart-badge">{totalItems}</span>}
          </NavLink>

          {customer ? (
            <>
              <NavLink to="/orders">Orders</NavLink>
              <button className="link-button" onClick={handleLogout}>
                Logout
              </button>
            </>
          ) : (
            <NavLink to="/login">Login</NavLink>
          )}
        </nav>
      </div>
    </header>
  )
}
