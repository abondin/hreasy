import type { VDialog } from "vuetify/components";

/** Selects the initial control through the public VDialog content reference after its transition. */
export function focusDialog(dialog: unknown) {
  const content = (dialog as InstanceType<typeof VDialog> | null)?.contentEl;
  if (!content) return;
  const available = (selector: string) => Array.from(content.querySelectorAll<HTMLElement>(selector))
    .find(element => !element.matches(':disabled') && !element.closest('[inert], [aria-hidden="true"]')
      && element.getClientRects().length > 0);
  const target = available('[data-dialog-initial-focus]')
    ?? available('[autofocus]')
    ?? available('input:not([type="hidden"]):not([readonly]), input[role="combobox"], textarea:not([readonly]), select, [contenteditable="true"]')
    ?? available('button, [tabindex="0"]');
  (target ?? content).focus({ preventScroll: true });
}
