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
import Component from "vue-class-component";
import Vue from "vue";

@Component
export default class PageNotFoundComponent extends Vue {
}
</script>
