import type { AxiosProgressEvent } from "axios";
import http from "@/lib/http";

export interface TechProfile {
  filename: string;
  accessToken: string;
}

export async function findTechProfiles(
  employeeId: number,
): Promise<TechProfile[]> {
  const response = await http.get<TechProfile[]>(`v1/techprofile/${employeeId}`);
  return response.data;
}

export async function deleteTechProfile(
  employeeId: number,
  filename: string,
): Promise<void> {
  await http.delete(`v1/techprofile/${employeeId}/file/${encodeURIComponent(filename)}`);
}

export async function uploadTechProfile(
  employeeId: number,
  file: File,
  onProgress?: (percentage: number) => void,
): Promise<void> {
  const formData = new FormData();
  formData.append("file", file);

  await http.post(`v1/techprofile/${employeeId}/file`, formData, {
    headers: {
      "Content-Type": "multipart/form-data",
    },
    onUploadProgress: (event: AxiosProgressEvent) => {
      if (!onProgress || !event.total) {
        return;
      }
      const percentage = Math.round((event.loaded / event.total) * 100);
      onProgress(percentage);
    },
  });
}

export function getTechProfileDownloadUrl(
  employeeId: number,
  profile: TechProfile,
): string {
  const baseUrl = http.defaults.baseURL ?? "";
  const normalizedBaseUrl = baseUrl.endsWith("/") ? baseUrl : `${baseUrl}/`;
  const encodedToken = encodeURIComponent(profile.accessToken);
  const encodedFilename = encodeURIComponent(profile.filename);
  return `${normalizedBaseUrl}v1/fs/techprofile/${employeeId}/${encodedToken}/${encodedFilename}`;
}

export function getTechProfileUploadUrl(employeeId: number): string {
  return `v1/techprofile/${employeeId}/file`;
}

export function getTechProfileDeleteUrl(
  employeeId: number,
  filename: string,
): string {
  return `v1/techprofile/${employeeId}/file/${encodeURIComponent(filename)}`;
}
