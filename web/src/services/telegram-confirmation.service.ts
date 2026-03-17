import http from "@/lib/http";

/**
 * Confirms employee Telegram account by one-time confirmation code from bot link.
 */
export async function confirmTelegramAccount(
  employeeId: number,
  accountName: string,
  confirmationCode: string,
): Promise<number> {
  const response = await http.post<number>(
    `v1/telegram/confirm/${employeeId}/${accountName}/${confirmationCode}`,
  );
  return response.data;
}
