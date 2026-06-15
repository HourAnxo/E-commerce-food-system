import axios from 'axios'

// Single axios instance pointed at the Spring Boot backend.
// In production set VITE_API_URL (e.g. "" so requests hit /api on the same
// origin and Nginx proxies them to the backend). Falls back to the local
// dev backend when the env var is undefined.
const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL ?? 'http://localhost:8081',
  headers: { 'Content-Type': 'application/json' },
})

export default api
