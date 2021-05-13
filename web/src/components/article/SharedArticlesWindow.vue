<!--
Window to show shared articles for all employees
 -->

<template>
  <div>
    All articles: {{ allArticles }}

    <div v-if="selectedArticleIndex>=0">
      
    </div>
  </div>
</template>


<script lang="ts">
import Vue from 'vue'
import Component from 'vue-class-component';
import articleService, {ArticleMeta} from "@/components/article/article.service";

const namespace: string = 'auth';

@Component({})
export default class SharedArticlesWindow extends Vue {

  private allArticles: ArticleMeta[] = [];
  private selectedArticleIndex = -1;
  private loading = false;

  /**
   * Lifecycle hook
   */
  created() {
    this.fetchMeta();
  }

  private fetchMeta() {
    this.loading = true;
    this.selectedArticleIndex = -1;
    articleService.listShared()
        .then((result) => {
          this.allArticles = result;
          if (this.allArticles.length > 0) {
            this.selectedArticleIndex = 0;
          }
        })
        .finally(() => {
          this.loading = false;
        })
  }
}
</script>

<style scoped>

</style>
