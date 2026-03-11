import process from 'node:process';
import {defineConfig, devices} from '@playwright/test';

const isCi = !!process.env.CI;
const defaultPort = isCi ? 4173 : 5173;
const port = Number(process.env.PLAYWRIGHT_PORT ?? defaultPort);
const configuredBasePath = process.env.PLAYWRIGHT_BASE_PATH ?? process.env.VITE_APP_BASE_PATH ?? '';
const normalizedBasePath = configuredBasePath
  ? `/${configuredBasePath.replace(/^\/+|\/+$/g, '')}`
  : '';
const baseURL = process.env.PLAYWRIGHT_BASE_URL ?? `http://localhost:${port}${normalizedBasePath}`;
const webServerCommand = isCi
  ? `npm run preview -- --port ${port}`
  : `npm run dev -- --port ${port}`;

export default defineConfig({
  testDir: './e2e',
  timeout: 30 * 1000,
  expect: {
    timeout: 5000
  },
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  workers: process.env.CI ? 1 : undefined,
  reporter: 'html',
  use: {
    actionTimeout: 0,
    baseURL,
    trace: 'on-first-retry',
    headless: isCi
  },
  projects: [
    {
      name: 'chromium',
      use: {
        ...devices['Desktop Chrome']
      }
    },
    {
      name: 'firefox',
      use: {
        ...devices['Desktop Firefox']
      }
    },
    {
      name: 'webkit',
      use: {
        ...devices['Desktop Safari']
      }
    }
  ],
  webServer: {
    command: webServerCommand,
    port,
    reuseExistingServer: !isCi
  }
});
