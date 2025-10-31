import {defineConfig, loadEnv} from 'vite';
import vue from '@vitejs/plugin-vue';
import vuetify from 'vite-plugin-vuetify';
import path from 'path';

const projectRoot = __dirname;
const srcAlias = path.resolve(projectRoot, 'src');
const legacyLocalesDir = path.resolve(projectRoot, '../../src/locales');

export default defineConfig(({mode}) => {
  const env = loadEnv(mode, process.cwd(), '');
  const proxyTarget = env.VITE_DEV_SERVER_PROXY || env.BACKEND_API_BASE_URL;

  return {
    plugins: [
      vue(),
      vuetify({
        autoImport: true
      })
    ],
    resolve: {
      alias: {
        '@': srcAlias,
        '@locales': legacyLocalesDir
      }
    },
    server: {
      port: 5173,
      proxy: proxyTarget
        ? {
            '/api': {
              target: proxyTarget,
              changeOrigin: true
            }
          }
        : undefined,
      fs: {
        allow: [
          projectRoot,
          legacyLocalesDir
        ]
      }
    }
  };
});
