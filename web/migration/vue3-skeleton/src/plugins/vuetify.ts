import { createVuetify } from "vuetify";
import { aliases, mdi } from "vuetify/iconsets/mdi";
import { fa } from "vuetify/iconsets/fa";
import "vuetify/styles";
import "@mdi/font/css/materialdesignicons.css";
import "@fortawesome/fontawesome-free/css/all.css";

const vuetify = createVuetify({
  theme: {
    defaultTheme: "light",
    themes: {
      light: {
        colors: {
          primary: "#f4511e",
          secondary: "#4a148c",
        },
      },
    },
  },
  icons: {
    defaultSet: "mdi",
    aliases,
    sets: {
      mdi,
      fa,
    },
  },
});

export default vuetify;
