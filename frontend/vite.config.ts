import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueJsx from '@vitejs/plugin-vue-jsx'
import vueDevTools from 'vite-plugin-vue-devtools'
import tailwindcss from '@tailwindcss/vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    vueJsx(),
    vueDevTools(),
    tailwindcss(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
  server: {
    host: '0.0.0.0', // Allow external connections
    port: 3000,
    strictPort: true,
    proxy: {
      '/chat.': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        ws: false,
      },
      '/user.': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        ws: false,
      },
      '/admin.': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        ws: false,
      },
      '/auth.': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        ws: false,
      },
      '/notification.': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        ws: false,
      },
    },
  }
})
