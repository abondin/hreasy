import {UiConstants} from "@/components/uiconstants";

describe("Telegram account extraction", () => {
    it.each(
        [
            {url:"https://telegram.me/abondin", account:"abondin"}
            , {url:"https://www.telegram.me/abondin", account:"abondin"}
            , {url:"telegram.me/abondin", account:"abondin"}
            , {url:"@abondin", account:"abondin"}
            , {url:"t.me/abondin", account:"abondin"}
            , {url:"abondin", account:"abondin"}
            , {url:"https://www.telegram.me/abondin_123", account:"abondin_123"}
            , {url:"https://www.telegram.me/123", account:"123"}
            , {url:"abondin_123", account:"abondin_123"}
            , {url:"@abondin_123", account:"abondin_123"}
        ]
    )('should extracted %p', (input: {url:string,account:string}) =>
        expect(UiConstants.extractTelegramAccount(input.url)).toBe(input.account)
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
    )('should not account "abondin" extracted from "%p". Expect undefined', (input: string|null|undefined) =>
        expect(UiConstants.extractTelegramAccount(input)).toBeUndefined()
    )
})
