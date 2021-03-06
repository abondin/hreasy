import Vue from 'vue'
import Vuex, {StoreOptions} from 'vuex'
import auth from './modules/auth'
import dict from './modules/dict'
import error from './modules/error'


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
        dict,
        error
    }
}

const store = new Vuex.Store(storeOptions);

export default store;

