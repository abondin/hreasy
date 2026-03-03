<template>
  <v-card>
    <shared-articles-window></shared-articles-window>
  </v-card>
</template>


<script lang="ts">
import Vue from 'vue'
import Component from 'vue-class-component';
import SharedArticlesWindow from "@/components/article/SharedArticlesWindow.vue";


@Component({
  components: {SharedArticlesWindow}
})
export default class MySharedArticles extends Vue {
}
</script>

<style scoped>

</style>
