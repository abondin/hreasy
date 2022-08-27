/**
 * Basic constants for UI components
 */
export class UiConstants {
    static defaultItemsPerTablePage = 15;

    /**
     * Print ()=>string, string or default value
     * @param customValue
     * @param defaultValue
     */
    static print(customValue: Function | string | undefined, defaultValue?: string): string|undefined {
        if (!customValue) {
            return defaultValue;
        }
        if (customValue instanceof Function) {
            return customValue();
        }
        return customValue;
    }
}
