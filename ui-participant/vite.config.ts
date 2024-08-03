import {
  defineConfig,
  PluginOption
} from 'vite'
import react from '@vitejs/plugin-react-swc'
import viteTsconfigPaths from 'vite-tsconfig-paths'
import * as path from 'path'
import mkcert from 'vite-plugin-mkcert'

/** set to true to do a full refresh on all code changes -- useful for troubleshooting deeply interrelated changes */
const FULL_RELOAD_ON_ALL_CHANGES = false

const fullReloadAlwaysPlugin: PluginOption = {
  name: 'full-reload-always',
  handleHotUpdate({ server }) {
    server.ws.send({ type: 'full-reload' })
    return []
  }
} as PluginOption

const plugins = [
  react(),
  viteTsconfigPaths(),
  mkcert()
]
if (FULL_RELOAD_ON_ALL_CHANGES) {
  plugins.push(fullReloadAlwaysPlugin)
}


// https://vitejs.dev/config/
export default defineConfig({
  base: '/',
  server: {
    port: 3001,
    proxy: {
      '/api': { target: 'http://localhost:8081', changeOrigin: false },
      '/config': { target: 'http://localhost:8081', changeOrigin: false },
      '/favicon.ico': { target: 'http://localhost:8081', changeOrigin: false }
    },
    open: 'https://sandbox.demo.localhost:3001'
  },
  resolve: {
    alias: {
      '~bootstrap': path.resolve(__dirname, '../node_modules/bootstrap'),
      // the below enables live-reloading from the ui-core package
      '@juniper/ui-core': path.resolve(__dirname, '../ui-core/src/index.ts')
    }
  },
  plugins
})
