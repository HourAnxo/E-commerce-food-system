import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    // Pin to 5173 so the origin always matches the backend CORS allow-list
    // (CorsConfig defaults to http://localhost:5173). strictPort makes Vite
    // fail instead of silently drifting to 5174/5175 when the port is taken.
    port: 5173,
    strictPort: true,
  },
})
