import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import monacoEditorPlugin from 'vite-plugin-monaco-editor-esm';

// https://vite.dev/config/
export default defineConfig({
  plugins: [react(), monacoEditorPlugin()],
})
