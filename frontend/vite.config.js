import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  build: {
    rollupOptions: {
      output: {
        manualChunks(id) {
          if (id.includes('recharts')) {
            return 'charts'
          }
          if (id.includes('lucide-react')) {
            return 'icons'
          }
          if (id.includes('react-router-dom') || id.includes('react-dom') || id.includes('react')) {
            return 'vendor'
          }
        },
      },
    },
    chunkSizeWarningLimit: 1000,
  },
})
