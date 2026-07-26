import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import tailwindcss from '@tailwindcss/vite';
import path from 'node:path';
 
export default defineConfig({
  plugins: [react(), tailwindcss()],
  resolve: {
    // Lets imports use "@/components/..." instead of relative "../../components/...".
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
  server: {
    port: 5173,
    proxy: {
      // Forwards /api and /r calls to the Spring Boot backend during dev,
      // avoiding CORS entirely for local development.
      '/api': 'http://localhost:8081',
    },
  },
});
 