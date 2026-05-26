export function normalizeSearchInput(value: unknown): string {
  return typeof value === "string" ? value : "";
}
