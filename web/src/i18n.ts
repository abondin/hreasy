import { createI18n } from "vue-i18n";

type MessageSchema = {
  [key: string]: string | MessageSchema;
};

type LocaleModule = {
  default: MessageSchema;
};

const localeModules = import.meta.glob<LocaleModule>("./locales/*.json", {
  eager: true,
});

const messages: Record<string, MessageSchema> = {};

const ruBuiltInMessages: MessageSchema = {
  notifications: {
    title: "\u0423\u0432\u0435\u0434\u043e\u043c\u043b\u0435\u043d\u0438\u044f",
    refresh: "\u041e\u0431\u043d\u043e\u0432\u0438\u0442\u044c \u0443\u0432\u0435\u0434\u043e\u043c\u043b\u0435\u043d\u0438\u044f",
    empty: "\u041d\u0435\u0442 \u043d\u043e\u0432\u044b\u0445 \u0443\u0432\u0435\u0434\u043e\u043c\u043b\u0435\u043d\u0438\u0439",
    markAsRead: "\u041e\u0442\u043c\u0435\u0442\u0438\u0442\u044c \u043a\u0430\u043a \u043f\u0440\u043e\u0447\u0438\u0442\u0430\u043d\u043d\u043e\u0435",
    loadError: "\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u0437\u0430\u0433\u0440\u0443\u0437\u0438\u0442\u044c \u0443\u0432\u0435\u0434\u043e\u043c\u043b\u0435\u043d\u0438\u044f",
  },
};

Object.entries(localeModules).forEach(([path, module]) => {
  const matched = path.match(/([\w-]+)\.json$/i);
  if (matched && matched[1]) {
    messages[matched[1]] = module.default;
  }
});

messages.ru = {
  ...(messages.ru ?? {}),
  ...ruBuiltInMessages,
};

const i18n = createI18n({
  legacy: false,
  locale: "ru",
  fallbackLocale: "ru",
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
