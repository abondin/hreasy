import {globalIgnores} from 'eslint/config';
import {defineConfigWithVueTs, vueTsConfigs} from '@vue/eslint-config-typescript';
import pluginVue from 'eslint-plugin-vue';
import pluginVitest from '@vitest/eslint-plugin';
import pluginPlaywright from 'eslint-plugin-playwright';
import skipFormatting from '@vue/eslint-config-prettier/skip-formatting';

const vitestRecommendedRules = Object.fromEntries(
  Object.entries(pluginVitest.configs.recommended.rules).filter(
    ([ruleName]) => ruleName !== 'vitest/no-standalone-expect'
  )
);

export default defineConfigWithVueTs(
  {
    name: 'app/files-to-lint',
    files: ['**/*.{ts,mts,tsx,vue}']
  },

  globalIgnores(['**/dist/**', '**/dist-ssr/**', '**/coverage/**']),

  pluginVue.configs['flat/essential'],
  vueTsConfigs.recommended,

  {
    name: 'app/vitest',
    files: [
      'src/**/__tests__/**/*.{test,spec}.{ts,tsx,js,jsx}',
      'tests/**/*.{test,spec}.{ts,tsx,js,jsx}'
    ],
    plugins: {
      vitest: pluginVitest
    },
    languageOptions: {
      ...pluginVitest.configs.env.languageOptions
    },
    rules: {
      ...vitestRecommendedRules
    }
  },

  {
    ...pluginPlaywright.configs['flat/recommended'],
    files: ['e2e/**/*.{test,spec}.{js,ts,jsx,tsx}']
  },
  skipFormatting
);
