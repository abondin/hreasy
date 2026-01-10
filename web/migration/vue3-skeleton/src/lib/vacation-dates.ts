import { formatIsoDate } from "@/lib/datetime";

export function parseDateOnly(value?: string | null): Date | null {
  if (!value) {
    return null;
  }
  const match = value.match(/^(\d{4})-(\d{2})-(\d{2})$/);
  if (match) {
    const year = Number(match[1]);
    const month = Number(match[2]);
    const day = Number(match[3]);
    const date = new Date(year, month - 1, day);
    return Number.isNaN(date.getTime()) ? null : date;
  }
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? null : date;
}

export function formatDateOnly(date: Date): string {
  return formatIsoDate(date);
}

export function addDays(date: Date, days: number): Date {
  const copy = new Date(date.getTime());
  copy.setDate(copy.getDate() + days);
  return copy;
}

export function getDefaultYears(): number[] {
  const currentYear = new Date().getFullYear();
  return [currentYear - 2, currentYear - 1, currentYear, currentYear + 1];
}

export function getStartOfYear(year: number): Date {
  return new Date(year, 0, 1);
}

export function getEndOfYear(year: number): Date {
  return new Date(year, 11, 31, 23, 59, 59, 999);
}

export function isDateInRange(
  targetIso: string | undefined | null,
  range: string[],
): boolean {
  if (!targetIso || range.length === 0) {
    return true;
  }
  const start = parseDateOnly(range[0]);
  const end = range.length > 1 ? parseDateOnly(range[1]) : null;
  const target = parseDateOnly(targetIso);
  if (!target || !start) {
    return true;
  }
  if (end && target > end) {
    return false;
  }
  return target >= start;
}

export function calculateVacationDays(
  startIso: string,
  endIso: string,
  excludedDays: string[],
): number {
  const start = parseDateOnly(startIso);
  const end = parseDateOnly(endIso);
  if (!start || !end) {
    return 0;
  }

  const excluded = new Set(excludedDays);
  let count = 0;
  for (let cursor = new Date(start); cursor <= end; cursor = addDays(cursor, 1)) {
    if (!excluded.has(formatDateOnly(cursor))) {
      count += 1;
    }
  }
  return count;
}
