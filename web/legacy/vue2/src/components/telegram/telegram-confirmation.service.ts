import {AxiosInstance} from "axios";
import httpService from "@/components/http.service";

export interface TelegramConfirmationService {
    confirmTelegramAccount(employeeId: number, accountName: string, confirmationCode: string): Promise<number>;
}

class RestTelegramConfirmationService implements TelegramConfirmationService {
    constructor(private httpService: AxiosInstance) {
    }

    confirmTelegramAccount(employeeId: number, accountName: string, confirmationCode: string): Promise<number> {
        return this.httpService.post(`v1/telegram/confirm/${employeeId}/${accountName}/${confirmationCode}`).then(response => {
            return response.data;
        });
    }
}

const telegramConfirmationService = new RestTelegramConfirmationService(httpService);

export default telegramConfirmationService;