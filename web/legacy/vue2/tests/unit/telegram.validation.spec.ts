import {TelegramUtils} from "@/telegram-utils";

describe("Telegram account extraction", () => {
    it.each(
        [
            {url: "https://telegram.me/abondin", account: "abondin"}
            , {url: "https://www.telegram.me/abondin", account: "abondin"}
            , {url: "telegram.me/abondin", account: "abondin"}
            , {url: "@abondin", account: "abondin"}
            , {url: "t.me/abondin", account: "abondin"}
            , {url: "abondin", account: "abondin"}
            , {url: "https://www.telegram.me/abondin_123", account: "abondin_123"}
            , {url: "https://www.telegram.me/123", account: "123"}
            , {url: "abondin_123", account: "abondin_123"}
            , {url: "@abondin_123", account: "abondin_123"}
        ]
    )('should extracted %p', (input: { url: string, account: string }) =>
        expect(TelegramUtils.extractTelegramAccount(input.url)).toBe(input.account)
    )
    it.each(
        [
            "https://telegram.me/@abondin@"
            , "https://abondin/dfg"
            , "@abondin/me"
            , "https://www.telegram.me/123#234"
            , null
            , undefined
            , ""
        ]
    )('should not account "abondin" extracted from "%p". Expect undefined', (input: string | null | undefined) =>
        expect(TelegramUtils.extractTelegramAccount(input)).toBeUndefined()
    )
});

describe("Short telegram account validation", () => {
    it.each(
        [
            "abondin"
            , "+79998887766"
            , null
            , undefined
            , ""
        ]
    )('should %p be valid short telegram username or phone', (input: string | null | undefined) =>
        expect(TelegramUtils.isShortTelegramUsernameOrPhoneValid(input)).toBeTruthy()
    )
    it.each(
        [
            "@abondin"
            , "7 999 8887766"
            ,"https://telegram.me/abondin"
        ]
    )('should %p not be valid short telegram username or phone', (input: string | null | undefined) =>
        expect(TelegramUtils.isShortTelegramUsernameOrPhoneValid(input)).toBeFalsy()
    )
});
