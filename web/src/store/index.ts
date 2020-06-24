import Vue from 'vue'
import Vuex, {StoreOptions} from 'vuex'
import auth from './modules/auth'
import dict from './modules/dict'


Vue.use(Vuex)

export interface RootState {
    version: string;
}

const storeOptions: StoreOptions<RootState> = {
    state: {
        version: "1.0.0"
    },
    modules: {
        auth,
        dict
    }
}

const store = new Vuex.Store(storeOptions);

export default store;

