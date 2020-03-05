/* eslint-disable no-console */
/////////////////////////////////////////////////////
// Create i18n report using vue-i18n-extract
// and then write missing keys to locale files
/////////////////////////////////////////////////////
const path = require('path');
const fs = require('fs');

//TODO use __dirname
const base = ".";
const localeFilesPath = path.join(base, 'src/locales')


function groupBy(xs, key) {
    return xs.reduce(function (rv, x) {
        (rv[x[key]] = rv[x[key]] || []).push(x);
        return rv;
    }, {});
}

function appendMissingKey(lang, missingKeys) {
    const file = path.join(localeFilesPath, lang + ".json");
    const json = JSON.parse(fs.readFileSync(file));
    missingKeys.forEach(key => {
        const path = key.path;
        const normalizedMessage = path.replace(/(_)+/g, ' ');
        console.log(` -- Adding ${path} to ${file}`);
        json[path] = normalizedMessage;
    })
    fs.writeFileSync(file, JSON.stringify(json, undefined, 2));
}

const vueFilesPattern = path.join(base, 'src/**/*.?(ts|vue)');
const localeFilesPattern = localeFilesPath + '/*.json';


console.log(`Create i18n and add missing keys to the locale files`);
console.log(` -- vueFilesPattern: ${vueFilesPattern}`);
console.log(` -- localeFilesPattern: ${localeFilesPattern}`);

const VueI18NExtract = require('vue-i18n-extract').default;

const report = VueI18NExtract.createI18NReport(vueFilesPattern, localeFilesPattern);


console.log(JSON.stringify(report, undefined, 2));

if (report.missingKeys) {
    const missingKeys = groupBy(report.missingKeys, 'language');
    console.log(`Adding missing keys to locale files ${Object.keys(missingKeys)}`);
    for (let lang in missingKeys) {
        appendMissingKey(lang, missingKeys[lang]);
    }
}



