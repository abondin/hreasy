export type DictOptionLike = {
  id: number;
  name?: string | null;
  value?: string | null;
  displayName?: string | null;
};

export function withCurrentOptionById<T extends { id: number }>(
  options: T[],
  current: T | null | undefined,
): T[] {
  if (!current) {
    return options;
  }
  if (options.some((item) => item.id === current.id)) {
    return options;
  }
  return [...options, current];
}

export function withArchivedOptionById<T extends { id: number }>(
  options: T[],
  id: number | null | undefined,
  factory: (id: number) => T,
): T[] {
  if (id == null) {
    return options;
  }
  if (options.some((item) => item.id === id)) {
    return options;
  }
  return [...options, factory(id)];
}

export function dictOptionTitle(item: unknown): string {
  if (typeof item === "string" || typeof item === "number") {
    return String(item);
  }
  const option = item as DictOptionLike | null;
  if (!option) {
    return "";
  }
  return (
    option.displayName?.trim()
    || option.name?.trim()
    || option.value?.trim()
    || String(option.id)
  );
}
