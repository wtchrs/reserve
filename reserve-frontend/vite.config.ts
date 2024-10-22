import {defineConfig} from 'vite'
import react from '@vitejs/plugin-react'
import tsconfigPaths from 'vite-tsconfig-paths'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react(), tsconfigPaths()],
  build: {
    rollupOptions: {
      output: {
        manualChunks: {
          'react': [
            'react',
            'react-dom',
            'react-router-dom',
            'react-hook-form',
            '@hookform/resolvers',
            'zod'
          ],
          'mui': [
            '@mui/material',
            '@mui/icons-material',
            '@emotion/react',
            '@emotion/styled',
            '@fontsource/roboto',
          ],
          'axios': ['axios'],
        },
      },
    }
  }
})
