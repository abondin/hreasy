import { createI18n } from "vue-i18n";

type MessageSchema = Record<string, string>;

type LocaleModule = {
  default: MessageSchema;
};

const localeModules = import.meta.glob<LocaleModule>("@locales/*.json", {
  eager: true,
});

const messages: Record<string, MessageSchema> = {};

Object.entries(localeModules).forEach(([path, module]) => {
  const matched = path.match(/([\w-]+)\.json$/i);
  if (matched && matched[1]) {
    messages[matched[1]] = module.default;
  }
});

const i18n = createI18n({
  legacy: false,
  locale: "ru",
  fallbackLocale: "en",
  globalInjection: true,
  messages,
  pluralRules: {
    ru(choice, choicesLength) {
      if (choice === 0) {
        return 0;
      }

      const teen = choice > 10 && choice < 20;
      const endsWithOne = choice % 10 === 1;

      if (choicesLength < 4) {
        return !teen && endsWithOne ? 1 : 2;
      }
      if (!teen && endsWithOne) {
        return 1;
      }
      if (!teen && choice % 10 >= 2 && choice % 10 <= 4) {
        return 2;
      }

      return choicesLength < 4 ? 2 : 3;
    },
  },
});

export default i18n;
