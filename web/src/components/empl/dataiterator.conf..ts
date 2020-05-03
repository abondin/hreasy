interface DataIteratorSortElement {
    value: any;
    text: string;
}

interface DataIteratorConf {
    search: string;
    itemsPerPage: number;
    page: number;
    sortBy: string;
    sortDesc: boolean,
    sortKeys: Array<DataIteratorSortElement>,
    numberOfPages: number;

    nextPage(): void;

    formerPage(): void;
}

class DefaultDataIteratorConf implements DataIteratorConf {
    public constructor(
        public search = '',
        public itemsPerPage = 20,
        public page = 1,
        public sortBy = 'displayName',
        public sortDesc = false,
        public sortKeys:Array<DataIteratorSortElement>  = [],
        public numberOfPages = 0,
    ) {
    }
    formerPage(): void {
        if (this.page - 1 >= 1) this.page -= 1
    }

    nextPage(): void {
        if (this.page + 1 <= this.numberOfPages) {
            this.page += 1
        }
    }
}

export default DefaultDataIteratorConf;
