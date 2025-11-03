import {createRequire} from 'node:module';
import {fileURLToPath, URL} from 'node:url';

import type {PluginOption} from 'vite';
import {defineConfig, loadEnv} from 'vite';
import vue from '@vitejs/plugin-vue';
import vuetify from 'vite-plugin-vuetify';

const projectRoot = fileURLToPath(new URL('.', import.meta.url));
const srcAlias = fileURLToPath(new URL('./src', import.meta.url));
const legacyLocalesDir = fileURLToPath(new URL('../../src/locales', import.meta.url));
const require = createRequire(import.meta.url);

let vueDevToolsPlugin: PluginOption | null = null;
try {
  const {default: pluginFactory} = require('vite-plugin-vue-devtools');
  if (typeof pluginFactory === 'function') {
    vueDevToolsPlugin = pluginFactory();
  }
} catch {
  vueDevToolsPlugin = null;
}

export default defineConfig(({mode}) => {
  const env = loadEnv(mode, process.cwd(), '');
  const proxyTarget = env.VITE_DEV_SERVER_PROXY || env.BACKEND_API_BASE_URL;

  return {
    plugins: [
      vue(),
      ...(vueDevToolsPlugin ? [vueDevToolsPlugin] : []),
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
