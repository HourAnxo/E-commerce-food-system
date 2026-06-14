import { Routes, Route, Outlet } from 'react-router-dom'
import Navbar from './components/Navbar.jsx'
import Home from './pages/Home.jsx'
import ProductList from './pages/ProductList.jsx'
import ProductDetail from './pages/ProductDetail.jsx'
import Cart from './pages/Cart.jsx'
import Checkout from './pages/Checkout.jsx'
import OrderHistory from './pages/OrderHistory.jsx'
import DeliveryTracking from './pages/DeliveryTracking.jsx'
import DeliveryDetail from './pages/DeliveryDetail.jsx'
import Login from './pages/Login.jsx'
import Register from './pages/Register.jsx'
import RequireAdmin from './admin/RequireAdmin.jsx'
import AdminLayout from './admin/AdminLayout.jsx'
import AdminLogin from './admin/AdminLogin.jsx'
import Dashboard from './admin/Dashboard.jsx'
import AdminProducts from './admin/AdminProducts.jsx'
import AdminOrders from './admin/AdminOrders.jsx'
import AdminDeliveries from './admin/AdminDeliveries.jsx'
import AdminCustomers from './admin/AdminCustomers.jsx'
import './App.css'

// Customer-facing shell: navbar + centered content container.
function CustomerLayout() {
  return (
    <>
      <Navbar />
      <main className="container">
        <Outlet />
      </main>
    </>
  )
}

function App() {
  return (
    <Routes>
      {/* Admin section — own full-screen layout, no customer navbar */}
      <Route path="/admin/login" element={<AdminLogin />} />
      <Route
        path="/admin"
        element={
          <RequireAdmin>
            <AdminLayout />
          </RequireAdmin>
        }
      >
        <Route index element={<Dashboard />} />
        <Route path="products" element={<AdminProducts />} />
        <Route path="orders" element={<AdminOrders />} />
        <Route path="deliveries" element={<AdminDeliveries />} />
        <Route path="customers" element={<AdminCustomers />} />
      </Route>

      {/* Customer section */}
      <Route element={<CustomerLayout />}>
        <Route path="/" element={<Home />} />
        <Route path="/products" element={<ProductList />} />
        <Route path="/products/:id" element={<ProductDetail />} />
        <Route path="/cart" element={<Cart />} />
        <Route path="/checkout" element={<Checkout />} />
        <Route path="/orders" element={<OrderHistory />} />
        <Route path="/deliveries" element={<DeliveryTracking />} />
        <Route path="/deliveries/:orderId" element={<DeliveryDetail />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="*" element={<NotFound />} />
      </Route>
    </Routes>
  )
}

function NotFound() {
  return (
    <div className="empty-state">
      <h2>Page not found</h2>
      <p>The page you are looking for doesn’t exist.</p>
    </div>
  )
}

export default App
