/**
 * Basic constants for UI components
 */
export class UiConstants {
    static defaultItemsPerTablePage = 15;
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
}
