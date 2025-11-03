import {fileURLToPath, URL} from 'node:url';

import {configDefaults, defineConfig, mergeConfig} from 'vitest/config';
import rawViteConfig from './vite.config';

const resolvedViteConfig =
  typeof rawViteConfig === 'function'
    ? await rawViteConfig({
        command: 'serve',
        mode: 'test',
        isSsrBuild: false,
        isPreview: false
      })
    : rawViteConfig;

export default mergeConfig(
  resolvedViteConfig,
  defineConfig({
    test: {
      environment: 'happy-dom',
      setupFiles: ['tests/setup.ts'],
      css: true,
      pool: 'threads',
      exclude: [...configDefaults.exclude, 'e2e/**'],
      root: fileURLToPath(new URL('./', import.meta.url)),
      server: {
        deps: {
          inline: ['vuetify']
        }
      }
    }
  })
);
