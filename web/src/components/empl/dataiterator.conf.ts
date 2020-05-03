interface DataIteratorSortElement {
    value: any;
    text: string;
}

interface DataiteratorConf<T> {
    items : Array<T>,
    search: string;
    itemsPerPage: number;
    page: number;
    sortBy: string;
    sortDesc: boolean,
    sortKeys: Array<DataIteratorSortElement>,
    numberOfPages: number;
    nextPage(): void;
    formerPage(): void;
    setDataItems(items :  Array<T>): void;
}

class DefaultDataIteratorConf<T> implements DataiteratorConf<T> {
    public constructor(
        public items: Array<T> = [],
        public search = '',
        public itemsPerPage = 30,
        public page = 1,
        public sortBy = 'displayName',
        public sortDesc = false,
        public sortKeys:Array<DataIteratorSortElement>  = [],
        public numberOfPages = 0,
        public nextPage = () => {
            if (this.page + 1 <= this.numberOfPages) {
                this.page += 1
            }
        },
        public formerPage = () => {
            if (this.page - 1 >= 1) this.page -= 1
        },
        public setDataItems = (items :  Array<T>) =>{
            this.items = items;
            this.numberOfPages =  Math.ceil(this.items.length / this.itemsPerPage);
        }
    ) {
    }

}

export default DefaultDataIteratorConf;
