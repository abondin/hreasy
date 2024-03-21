import '@mdi/font/css/materialdesignicons.css'
import 'material-design-icons-iconfont/dist/material-design-icons.css'
import Vue from 'vue';
import Vuetify from 'vuetify/lib';
import VueClipboard from 'vue-clipboard2'
// Translation provided by Vuetify (typescript)
import ru from 'vuetify/src/locale/ru';
import en from 'vuetify/src/locale/en';


Vue.use(Vuetify);
Vue.use(VueClipboard)

export default new Vuetify({
    lang: {
        locales: {ru, en},
        current: 'ru',
    },
    icons: {
        iconfont: 'mdiSvg'
    }
});
