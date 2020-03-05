module.exports = {
    "transpileDependencies": [
        "vuetify"
    ],
    devServer: {
        proxy: {
            '^/api': {
                target: process.env.BACKEND_API_BASE_URL,
                changeOrigin: true
            }
        }
    },
    pluginOptions: {
        i18n: {
            locale: 'ru',
            fallbackLocale: 'en',
            localeDir: 'locales',
            enableInSFC: false
        }
    },
    runtimeCompiler: true
}
