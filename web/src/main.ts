import Vue from 'vue'
import App from '@/App.vue'
import vuetify from '@/plugins/vuetify';
import store from '@/store'
import router from "@/router"
import logger from "@/logger";
import i18n from "@/i18n";
import VueExcelEditor from 'vue-excel-editor'

Vue.config.productionTip = false

const vue = new Vue({
    i18n,
    store,
    router,
    vuetify,
    render: h => h(App),
    renderError(h, err) {
        return h('pre', {style: {color: 'red'}}, err.stack)
    }
}).$mount('#app')

Vue.use(VueExcelEditor)

window.addEventListener('unhandledrejection', function (event) {
    logger.log('Dispath unhandledrejection', event);
    vue.$store.dispatch('error/unhandledrejection', event.reason);
});

