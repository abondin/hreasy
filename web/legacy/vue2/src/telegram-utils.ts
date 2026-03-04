/**
 * Helper to work with Telegram
 */
export class TelegramUtils {
    /**
     * Group number 5 is extracted account name from telegram url
     * @private
     */
    private static telegramFullAccountRegexp = /(https?:\/\/)?(www[.])?(telegram|t)(\.me\/)([a-zA-Z0-9_-]+)\/?$/;
    /**
     * First group is extracted account name from @account string
     * @private
     */
    private static telegramAtOrSimpleAccountRegexp = /^@?([a-zA-Z0-9_-]+)\/?$/;

    private static validShortUsernameOrPhone = /^([a-zA-Z0-9_-]+|(\+[0-9]+))\/?$/;


    /**
     *
     * @param input - link to telegram account. Can be in following formats
     * https://telegram.me/abondin
     * https://www.telegram.me/abondin
     * telegram.me/abondin
     * @abondin
     * t.me/abondin
     * abondin
     *
     * @return extracted telegram account. In example above - abondin.
     */
    public static extractTelegramAccount(input: string|null|undefined) : string | undefined{
        if (input && input.trim().length>0) {
            let result = this.telegramFullAccountRegexp.exec(input.trim());
            if (result){
                return result[5];
            }
            result = this.telegramAtOrSimpleAccountRegexp.exec(input.trim());
            return result ? result[1] : undefined;
        } else {
            return undefined;
        }
    }

    /**
     *
     * @param input
     * @return true if input is valid short telegram username (without @ and https://t.me)
     * or phone in international format (+79998887766) or empty string (or undefined, or null)
     */
    public static isShortTelegramUsernameOrPhoneValid(input: string|null|undefined) : boolean{
        return input? this.validShortUsernameOrPhone.test(input) : true;
    }
}
