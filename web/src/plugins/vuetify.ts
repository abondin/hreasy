import Vue from 'vue';
import Vuetify from 'vuetify/lib';

Vue.use(Vuetify);

// Translation provided by Vuetify (typescript)
import ru from 'vuetify/src/locale/ru';
import en from 'vuetify/src/locale/en';

export default new Vuetify({
    lang: {
        locales: {ru, en},
        current: 'ru',
    },
});
