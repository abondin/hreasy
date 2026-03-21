const DATE_PATTERN_SEPARATOR = ".";

function pad(value: number): string {
  return value.toString().padStart(2, "0");
}

function parseIso(value: string | undefined | null): Date | null {
  if (!value) {
    return null;
  }
  const dateOnly = value.match(/^(\d{4})-(\d{2})-(\d{2})$/);
  if (dateOnly) {
    const year = Number(dateOnly[1]);
    const month = Number(dateOnly[2]);
    const day = Number(dateOnly[3]);
    const localDate = new Date(year, month - 1, day);
    if (Number.isNaN(localDate.getTime())) {
      return null;
    }
    if (
      localDate.getFullYear() !== year ||
      localDate.getMonth() !== month - 1 ||
      localDate.getDate() !== day
    ) {
      return null;
    }
    return localDate;
  }
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? null : date;
}

export function formatIsoDate(date: Date): string {
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`;
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
