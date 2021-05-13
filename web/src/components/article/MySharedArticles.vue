<template>
  <v-card>
    <v-card-title>
      <div>{{ $t('Важная информация') }}</div>
    </v-card-title>

    <v-card-text>
      <shared-articles-window></shared-articles-window>
    </v-card-text>
  </v-card>
</template>


<script lang="ts">
import Vue from 'vue'
import Component from 'vue-class-component';
import SharedArticlesWindow from "@/components/article/SharedArticlesWindow.vue";

const namespace: string = 'auth';

@Component({
  components: {SharedArticlesWindow}
})
export default class MySharedArticles extends Vue {
}
</script>

<style scoped>

</style>
