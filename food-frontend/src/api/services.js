import api from './axios'

// Thin wrappers around the backend REST endpoints, grouped by resource.
// Endpoint paths mirror the Spring Boot @RequestMapping values exactly.

export const productApi = {
    getAll: () => api.get('/api/products'),
    getById: (id) => api.get(`/api/products/${id}`),
    getByCategory: (categoryId) => api.get(`/api/products/category/${categoryId}`),
    create: (product) => api.post('/api/products', product),
    // PUT overwrites the whole product, so callers must send the full DTO.
    update: (id, product) => api.put(`/api/products/${id}`, product),
    remove: (id) => api.delete(`/api/products/${id}`),
}

export const categoryApi = {
    getAll: () => api.get('/api/categories'),
}

export const customerApi = {
    getAll: () => api.get('/api/customers'),
    getByEmail: (email) => api.get(`/api/customers/email/${encodeURIComponent(email)}`),
    create: (customer) => api.post('/api/customers', customer),
}

export const orderApi = {
    getAll: () => api.get('/api/orders'),
    getByCustomer: (customerId) => api.get(`/api/orders/customer/${customerId}`),
    create: (order) => api.post('/api/orders', order),
    // PUT overwrites the whole order, so callers must send the full DTO.
    update: (id, order) => api.put(`/api/orders/${id}`, order),
}

export const deliveryApi = {
    getAll: () => api.get('/api/deliveries'),
    getByOrder: (orderId) => api.get(`/api/deliveries/order/${orderId}`),
    create: (delivery) => api.post('/api/deliveries', delivery),
    update: (id, delivery) => api.put(`/api/deliveries/${id}`, delivery),
    // ===== NEW =====
    // Driver/admin enters the customer's 6-digit code -> Delivered
    complete: (deliveryId, code) => api.post(`/api/deliveries/${deliveryId}/complete`, { code }),
    // Customer confirms they received the order -> Completed
    confirm: (deliveryId) => api.post(`/api/deliveries/${deliveryId}/confirm`),
    // Customer reports a problem -> Disputed
    reportProblem: (deliveryId) => api.post(`/api/deliveries/${deliveryId}/problem`),
    // ===============
}

export const adminApi = {
    login: (email, password) => api.post('/api/admins/login', { email, password }),
}

// NOTE: the backend mapping is singular — @RequestMapping("api/payment").
export const paymentApi = {
    getByOrder: (orderId) => api.get(`/api/payment/order/${orderId}`),
    create: (payment) => api.post('/api/payment', payment),
}

// Bakong KHQR: generate a QR for an order, then poll until it's paid.
export const bakongApi = {
    generateQr: (orderId) => api.post(`/api/payment/bakong/qr/${orderId}`),
    checkStatus: (md5, orderId) =>
        api.get('/api/payment/bakong/status', { params: { md5, orderId } }),
}

// Backend enum values — must match exactly or PUT requests fail.
export const ORDER_STATUSES = ['Pending', 'Processing', 'Shipped', 'Delivery', 'Cancelled']
// ===== CHANGED: admin can only set Preparing/Shipped by hand.
// Delivered requires the customer's 6-digit code (complete endpoint);
// Completed/Disputed come from the customer's confirm/problem actions. =====
export const DELIVERY_STATUSES = ['Preparing', 'Shipped']
// ===============
// Payment.PaymentMethod enum — "Credit Card" maps via @JsonProperty on the backend.
export const PAYMENT_METHODS = ['Cash', 'Credit Card', 'ABA', 'ACELEDA', 'Wing', 'Bakong']