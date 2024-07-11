import { defineConfig, PluginOption } from 'vite'
import react from '@vitejs/plugin-react-swc'
import { VitePluginWatchWorkspace } from '@prosopo/vite-plugin-watch-workspace'
import viteTsconfigPaths from 'vite-tsconfig-paths'
import * as path from 'path'

const fullReloadAlways: PluginOption = {
  name: 'full-reload-always',
  handleHotUpdate({ server }) {
    server.ws.send({ type: 'full-reload' })
    return []
  }
} as PluginOption

// https://vitejs.dev/config/
export default defineConfig({
  base: '/',
  server: {
    port: 3000,
    proxy: {
      '/api': 'http://localhost:8080',
      '/config': 'http://localhost:8080'
    }
  },
  resolve: {
    alias: {
      '~bootstrap': path.resolve(__dirname, '../node_modules/bootstrap')
    }
  },
  plugins: [
    react(),
    viteTsconfigPaths(),
    //fullReloadAlways,
    VitePluginWatchWorkspace({
      workspaceRoot: '..',
      currentPackage: 'ui-admin',
      format: 'esm', // or 'cjs'
      fileTypes: ['ts', 'tsx', 'js', 'jsx'], // optional - file types to watch. default is ['ts', 'tsx']
      ignorePaths: ['node_modules', 'dist'] // optional - globs to ignore
    })
  ]
})
