import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react-swc';
import viteTsconfigPaths from 'vite-tsconfig-paths';
// https://vitejs.dev/config/
/**
 * this is heavily borrowed from
 * https://github.com/DataBiosphere/terra-ui/blob/dev/packages/build-utils/src/baseViteConfig.ts
 */
export default defineConfig(({ mode }) => {
    const preserveExternalImports = () => {
        // Depends on running the build from the package's directory.
        // This is a valid assumption when running the build from a package.json script with yarn or npm.
        const packageDirectory = process.cwd();
        return {
            name: 'vite-plugin-leave-external-imports-unchanged',
            enforce: 'pre',
            resolveId: id => {
                const isInternal = id.startsWith('.') || id.startsWith(`${packageDirectory}/`);
                return isInternal ? null : {
                    id,
                    external: true
                };
            }
        };
    };
    return {
        base: '/',
        build: {
            lib: {
                entry: 'src/index.ts',
                // Build ES Module as CommonJS versions.
                formats: ['es'],
                fileName: (format, entryName) => {
                    // Narrow type of format to 'es' or 'cjs'.
                    // This should never throw because those are the only formats listed in `build.lib.formats`.
                    if (!(format === 'es' || format === 'cjs')) {
                        throw new Error(`Unsupported module format: ${format}`);
                    }
                    // Since package.json contains `type: "module"`, .js files will be interpreted as ES Modules.
                    // CommonJS modules must be distinguished with a .cjs extension.
                    const extension = {
                        es: 'js',
                        cjs: 'cjs'
                    }[format];
                    // Base output directory on the the module format.
                    // ES Modules are written to lib/es, CommonJS to lib/cjs.
                    return `${format}/${entryName}.${extension}`;
                }
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
                // Check types and emit type declarations.
                // Because the library is built in two different formats, this results in the type declarations
                // being written twice. This isn't ideal, but it's acceptable to keep the build within Vite
                // and avoid running tsc separately. Using `@rollup/plugin-typescript` instead of `tsc` keeps
                // package.json scripts simpler, allows us to use Vite's watcher to regenerate types after changes,
                // and allows us to fail the Vite build if there are type errors (using noEmitOnError).
                //
                // emitDeclarationOnly is specified here because putting it in tsconfig.json breaks check-dts.
                // noEmitOnError causes the Vite build to fail if there are type errors. This is disabled in
                // the `dev` package.json script.
                // plugins: [
                //   typescript({
                //     compilerOptions: {
                //       emitDeclarationOnly: true,
                //       noEmitOnError: mode !== 'development'
                //     }
                //   })
                // ]
            }
        },
        plugins: [
            react(),
            viteTsconfigPaths(),
            preserveExternalImports()
        ]
    };
});
