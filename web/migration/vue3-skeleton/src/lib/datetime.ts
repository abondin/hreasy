const DATE_PATTERN_SEPARATOR = ".";

function pad(value: number): string {
  return value.toString().padStart(2, "0");
}

function parseIso(value: string | undefined | null): Date | null {
  if (!value) {
    return null;
  }
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? null : date;
}

export function formatDate(value: string | undefined | null): string {
  const date = parseIso(value);
  if (!date) {
    return "";
  }
  const day = pad(date.getDate());
  const month = pad(date.getMonth() + 1);
  const year = date.getFullYear();
  return `${day}${DATE_PATTERN_SEPARATOR}${month}${DATE_PATTERN_SEPARATOR}${year}`;
}

export function formatDateTime(value: string | undefined | null): string {
  const date = parseIso(value);
  if (!date) {
    return "";
  }
  const day = pad(date.getDate());
  const month = pad(date.getMonth() + 1);
  const year = date.getFullYear();
  const hours = pad(date.getHours());
  const minutes = pad(date.getMinutes());
  return `${day}${DATE_PATTERN_SEPARATOR}${month}${DATE_PATTERN_SEPARATOR}${year} ${hours}:${minutes}`;
}
