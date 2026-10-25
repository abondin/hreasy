import DOMPurify from "dompurify";
import http from "@/lib/http";

export async function fetchOfficeMapSvg(filename: string): Promise<string> {
  const response = await http.get<string>(`v1/dict/office_maps/${filename}`, {
    responseType: "text",
  });
  return DOMPurify.sanitize(response.data, {
    USE_PROFILES: { svg: true },
  }) as string;
}
