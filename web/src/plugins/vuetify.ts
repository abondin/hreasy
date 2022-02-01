import Vue from 'vue';
import Vuetify from 'vuetify/lib';
import VueClipboard from 'vue-clipboard2'

Vue.use(Vuetify);
Vue.use(VueClipboard)

// Translation provided by Vuetify (typescript)
import ru from 'vuetify/src/locale/ru';
import en from 'vuetify/src/locale/en';

export default new Vuetify({
    lang: {
        locales: {ru, en},
        current: 'ru',
    },
});
