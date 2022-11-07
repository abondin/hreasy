export enum TextFilterContainerType {
    SIMPLE_IGNORE_CASE, SPLIT_WORDS
}

export interface TextFilterContainer {
    objectFieldInp: string | undefined | null,
    type: TextFilterContainerType
}

export class TextFilterBuilder {
    private _containers: Array<TextFilterContainer> = [];

    public static of(): TextFilterBuilder {
        return new TextFilterBuilder();
    }

    public ignoreCase(objectFieldInp: string | undefined | null): TextFilterBuilder {
        this.add(objectFieldInp, TextFilterContainerType.SIMPLE_IGNORE_CASE);
        return this;
    }

    public splitWords(objectFieldInp: string | undefined | null): TextFilterBuilder {
        this.add(objectFieldInp, TextFilterContainerType.SPLIT_WORDS);
        return this;
    }

    private add(objectFieldInp: string | undefined | null, type: TextFilterContainerType) {
        this._containers.push({objectFieldInp: objectFieldInp, type: type});
    }

    get container() {
        return this._containers;
    }
}

/**
 * Helper to implement table filter
 */
export class SearchUtils {

    public textFilter(textInp: string | undefined | null, builder: TextFilterBuilder): boolean {
        return !textInp || builder.container
            .map(c => {
                switch (c.type) {
                    case TextFilterContainerType.SIMPLE_IGNORE_CASE:
                        return this.matchText(textInp, c.objectFieldInp);
                    case TextFilterContainerType.SPLIT_WORDS:
                        return this.matchWords(textInp, c.objectFieldInp);
                    default:
                        throw new Error('Unsupported text filter type' + c.type);
                }
            }).reduce((cur, prev) => cur || prev, false);
    }

    public matchText(textInp: string | undefined | null, objectFieldInp: string | undefined | null): boolean {
        return !textInp || Boolean(objectFieldInp && this.prepareStr(objectFieldInp).indexOf(this.prepareStr(textInp)) >= 0);
    }

    public matchWords(textInp: string | undefined | null, objectFieldInp: string | undefined | null): boolean {
        const words = this.prepareStr(textInp).split(' ');
        return !textInp || words.map(w => Boolean(objectFieldInp && this.prepareStr(objectFieldInp).indexOf(this.prepareStr(w)) >= 0))
            .reduce((prev, cur) => prev && cur, true);
    }

    public array<T>(array: Array<T | null> | undefined, objectFieldInp: T | null | undefined): boolean {
        return (!array || array.length == 0) || (
            objectFieldInp ? array.indexOf(objectFieldInp) >= 0 : array.indexOf(null) >= 0
        )
    }

    private prepareStr(textInp: string | undefined | null): string {
        return textInp ? textInp.trim().toLowerCase() : '';
    }
}

export const searchUtils = new SearchUtils();
