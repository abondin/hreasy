import axios, {AxiosInstance} from 'axios';
import {AccessDeniedError, AuthenticationError, BusinessError} from "@/components/errors";


function createAxios(): AxiosInstance {
    const http = axios.create(
        {
            withCredentials: true,
            baseURL: '/api/',
            headers:{"Accept-Language": "ru-RU,ru"}
        }
    )

    http.interceptors.response.use(undefined, (error) => {
        let wrappedError: Error | undefined;
        let code: string;
        let attrs: Map<string, string>;
        let message: string;
        if (error.response && error.response.data) {
            const data = error.response.data;
            code = data.code;
            attrs = data.attrs;
            message = data.message ? data.message : error.toString();
            switch (error.response.status) {
                case 401:
                    wrappedError = new AuthenticationError(message, code);
                    break;
                case 403:
                    wrappedError = new AccessDeniedError(message, code);
                    break;
                case 422:
                    wrappedError = new BusinessError(message, code, attrs);
                    break;
            }
        }
        if (!wrappedError) {
            wrappedError = error;
        }
        return Promise.reject(wrappedError);
    });

    return http;
}

/**
 * HTTP REST client
 */
const
    httpService: AxiosInstance = createAxios();


export default httpService;
