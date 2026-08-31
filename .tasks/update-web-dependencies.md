# Update web dependencies

## Goal

- Update all direct dependencies and devDependencies in `web/` to current compatible/latest releases, not only vulnerable packages.
- Keep the existing stack and avoid adding dependencies or speculative refactors.
- Verify install, audit, type-check, lint, unit tests, and production build.

## Decisions

- Use npm and preserve the repository's exact/range style unless npm's latest release requires a deliberate major migration.
- Fix only compatibility issues caused by dependency updates.
- Keep TypeScript at 6.0.3: TypeScript 7 is not supported by current `vue-tsc` or `typescript-eslint`.

## Progress

- [x] Read repository and frontend instructions.
- [x] Inspect outdated packages and vulnerability paths.
- [x] Update package metadata and lockfile.
- [x] Resolve required compatibility issues.
- [x] Run verification.

## Validation

- `npm audit`: passed, 0 vulnerabilities.
- `npm run type-check`: passed.
- `npm run lint`: passed.
- `npm run test:unit -- --run`: passed, 7 files / 18 tests.
- `npm run build-only`: passed, 897 modules transformed.
- `npm outdated --json`: only TypeScript 7 remains; intentionally held at 6.0.3 due toolchain incompatibility.

## Open questions

- E2E was not run because it requires its browser/backend environment; dependency-level unit/build checks pass.
