<template>
  <v-card rounded class="align-content-center">
    <v-card-text>
      <v-alert rounded type="warning" colored-border border="left" outlined>
        <p>
        {{ $t('Запрошенная страница не найдена') }}
        </p>
        <router-link to="/">{{ $t('Вернуться на главную') }}</router-link>
      </v-alert>
    </v-card-text>
  </v-card>
</template>

<script lang="ts">
import {defineComponent} from 'vue';

export default defineComponent({
  name: 'PageNotFoundComponent',
  setup() {
    return {};
  }
});
</script>
