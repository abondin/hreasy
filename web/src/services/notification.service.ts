import http from "@/lib/http";

export interface NotificationItem {
  id: number;
  clientUuid: string;
  title: string;
  category: string;
  markdownText: string;
  context: string | null;
  createdAt: string;
  acknowledgedAt: string | null;
  archivedAt: string | null;
}

export async function fetchMyNotifications(): Promise<NotificationItem[]> {
  const response = await http.get<NotificationItem[]>("v1/notifications/my");
  return response.data;
}

export async function fetchMyUnreadNotificationCount(): Promise<number> {
  const response = await http.get<number>("v1/notifications/my/unread-count");
  return response.data;
}

export async function acknowledgeNotification(notificationId: number): Promise<number> {
  const response = await http.post<number>(`v1/notifications/${notificationId}/acknowledge`);
  return response.data;
}
