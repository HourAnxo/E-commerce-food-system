import axios from 'axios'

// Single axios instance pointed at the Spring Boot backend.
const api = axios.create({
  baseURL: 'http://localhost:8081',
  headers: { 'Content-Type': 'application/json' },
})

export default api
