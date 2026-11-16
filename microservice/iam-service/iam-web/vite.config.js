import path from 'node:path'
import tailwindcss from '@tailwindcss/vite'
import react from '@vitejs/plugin-react'
import { defineConfig, loadEnv } from 'vite'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, import.meta.dirname, '')
  const iamUrl = env.VITE_IAM_URL || 'http://localhost:9999'

  return {
    plugins: [react(), tailwindcss()],
    base: '/me/',
    resolve: {
      alias: { '@': path.resolve(import.meta.dirname, './src') },
    },
    server: {
      proxy: {
        '/api': iamUrl,
        '/oauth2/token': iamUrl,
      },
    },
  }
})
