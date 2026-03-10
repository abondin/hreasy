export function extractDataTableRow<T>(
  payload: unknown,
  guard?: (value: unknown) => value is T,
): T | null {
  const candidate = unwrapDataTableRow(payload);
  if (candidate == null) {
    return null;
  }
  if (guard) {
    return guard(candidate) ? candidate : null;
  }
  return candidate as T;
}

function unwrapDataTableRow(payload: unknown): unknown {
  if (!payload || typeof payload !== "object") {
    return null;
  }
  if ("item" in payload) {
    const item = (payload as { item?: { raw?: unknown } | unknown }).item;
    if (!item) {
      return null;
    }
    if (typeof item === "object" && item !== null && "raw" in item) {
      return (item as { raw?: unknown }).raw ?? null;
    }
    return item;
  }
  return payload;
}
