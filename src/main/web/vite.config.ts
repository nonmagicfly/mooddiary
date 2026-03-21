import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      // Spring Boot API (см. application.yml server.port)
      '/api': { target: 'http://localhost:8080', changeOrigin: true }
    }
  },
  test: {
    environment: 'jsdom',
    setupFiles: ['src/test/setup.ts']
  }
})

