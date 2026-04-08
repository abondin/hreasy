/**
 * Builds app-relative paths for environments where the app is served under a base path
 * and for plain root deployments (`/`).
 */
export function appPath(path: string): string {
  const configuredBasePath = process.env.PLAYWRIGHT_BASE_PATH ?? process.env.VITE_APP_BASE_PATH ?? "";
  const normalizedBasePath = configuredBasePath
    ? `/${configuredBasePath.replace(/^\/+|\/+$/g, "")}`
    : "";

  if (!normalizedBasePath) {
    return path;
  }

  const normalizedPath = path.startsWith("/") ? path : `/${path}`;
  return normalizedPath === "/" ? `${normalizedBasePath}/` : `${normalizedBasePath}${normalizedPath}`;
}
