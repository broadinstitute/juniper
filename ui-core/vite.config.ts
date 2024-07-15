import { defineConfig, ConfigEnv, PluginOption, UserConfig } from 'vite'
import react from '@vitejs/plugin-react-swc'
import viteTsconfigPaths from 'vite-tsconfig-paths'

// https://vitejs.dev/config/
/**
 * this is heavily borrowed from
 * https://github.com/DataBiosphere/terra-ui/blob/dev/packages/build-utils/src/baseViteConfig.ts
 */
export default defineConfig(({ mode }: ConfigEnv): UserConfig => {
  return {
    base: '/',
    build: {
      lib: {
        entry: 'src/index.ts',
        // Build ES Module as CommonJS versions.
        formats: ['es']
      },
      // Leave minification to consumers.
      // This makes the distributed code easier to read and stack traces easier to relate
      // back to the original source.
      minify: false,
      // Base output directory. Output paths are based on this and build.fileName.
      outDir: 'build',
      rollupOptions: {
        // Do not bundle dependencies.
        external: /node_modules/,
        output: {
          // Handle default exports from CommonJS dependencies.
          // https://rollupjs.org/configuration-options/#output-interop
          interop: 'auto',
          // Preserve source directory structure and file names.
          // This makes the distributed code easier to read and stack traces easier to relate
          // back to the original source.
          chunkFileNames: '[name].js',
          preserveModules: true,
          preserveModulesRoot: 'src'
        }
      }
    },
    plugins: [
      react(),
      viteTsconfigPaths()
    ]
  }
})
