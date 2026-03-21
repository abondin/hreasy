// Utility helpers for Telegram account parsing and validation.
const telegramFullAccountRegexp =
  /(https?:\/\/)?(www[.])?(telegram|t)(\.me\/)([a-zA-Z0-9_-]+)\/?$/;
const telegramAtOrSimpleAccountRegexp = /^@?([a-zA-Z0-9_-]+)\/?$/;
const validShortUsernameOrPhone = /^([a-zA-Z0-9_-]+|(\+[0-9]+))\/?$/;

export function extractTelegramAccount(
  input: string | null | undefined,
): string | undefined {
  if (input && input.trim().length > 0) {
    const trimmed = input.trim();
    const urlMatch = telegramFullAccountRegexp.exec(trimmed);
    if (urlMatch) {
      return urlMatch[5];
    }
    const simpleMatch = telegramAtOrSimpleAccountRegexp.exec(trimmed);
    return simpleMatch ? simpleMatch[1] : undefined;
  }
  return undefined;
}

export function isShortTelegramUsernameOrPhoneValid(
  input: string | null | undefined,
): boolean {
  if (!input) {
    return true;
  }
  return validShortUsernameOrPhone.test(input);
}
