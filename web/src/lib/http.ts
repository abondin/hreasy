import axios from "axios";
import type { AxiosError, AxiosInstance } from "axios";
import {
  AccessDeniedError,
  AuthenticationError,
  BusinessError,
  UnknownBackendError,
} from "./errors";

const baseURL = import.meta.env.VITE_API_BASE_URL ?? "/api/";

const httpService: AxiosInstance = axios.create({
  baseURL,
  withCredentials: true,
  headers: {
    "Accept-Language": "ru-RU,ru",
  },
});

httpService.interceptors.response.use(
  (response) => response,
  (error: AxiosError) => {
    let wrappedError: unknown = error;
    const status = error.response?.status;
    const data = error.response?.data as
      | {
          code?: string;
          attrs?: Record<string, string>;
          message?: string;
        }
      | undefined;

    const message = data?.message ?? error.message;
    const code = data?.code;

    switch (status) {
      case 401:
        wrappedError = new AuthenticationError(message, code);
        break;
      case 403:
        wrappedError = new AccessDeniedError(message, code);
        break;
      case 422:
        wrappedError = new BusinessError(message, code, data?.attrs);
        break;
      case 500:
        wrappedError = new UnknownBackendError(message);
        break;
      default:
        wrappedError = error;
    }

    return Promise.reject(wrappedError);
  },
);

export default httpService;
