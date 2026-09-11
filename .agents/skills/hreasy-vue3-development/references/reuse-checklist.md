
# Vue3 Reuse Checklist (HREasy)

## Search targets before coding

- `web/src/views/**`
- `web/src/components/**`
- `web/src/composables/**`
- `web/src/stores/**`
- `web/src/services/**`
- `web/src/lib/**`

## Reuse decision tree

1. Existing component matches >= 70% of needed behavior:
   - extend via props/slots/events
   - avoid cloning UI blocks
2. Existing service has same API domain:
   - add method to existing service file
   - avoid creating parallel service for same entity
3. Existing composable/store overlaps behavior:
   - extend existing state/actions
   - avoid duplicate state sources

## Create new module only if

- Feature is domain-new with no coherent host module.
- Reusing existing module would introduce brittle conditional logic.
- Separation improves maintainability without duplicating contracts.

## Checks before completion

- User-facing strings are i18n-based.
- Route/permissions behavior remains correct.
- From `web/`, `npm run type-check` passes.
- From `web/`, `npm run lint` passes.
- From `web/`, `npm run test:unit -- --run` passes when logic changed.
