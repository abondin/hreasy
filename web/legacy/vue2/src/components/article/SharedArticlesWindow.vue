<!--
Window to show shared articles for all employees
 -->

<template>
  <v-card
      v-if="allArticles && allArticles.length>0
       && selectedArticleIndex>=0">
    <v-card-actions class="justify-space-between">
      <v-btn text @click="prev" small>
        <v-icon>mdi-chevron-left</v-icon>
        {{ getPrevArticleName() }}
      </v-btn>
      <span class="font-weight-bold">{{ allArticles[selectedArticleIndex].name }}</span>
      <v-btn text @click="next" small>
        {{ getNextArticleName() }}
        <v-icon>mdi-chevron-right</v-icon>
      </v-btn>
    </v-card-actions>
    <v-skeleton-loader v-if="loading" class="mx-auto" type="card"></v-skeleton-loader>
    <div class="pa-5" v-if="!loading">
      <MarkdownTextRenderer :content="allArticles[selectedArticleIndex].content" />
    </div>
  </v-card>
</template>


<script lang="ts">
import Vue from 'vue'
import Component from 'vue-class-component';
import articleService, {Article} from "@/components/article/article.service";


import MarkdownTextRenderer from '@/components/shared/MarkdownTextRenderer.vue'

@Component({
  components: { MarkdownTextRenderer }
})
export default class SharedArticlesWindow extends Vue {

  private allArticles: Article[] = [];
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
    articleService.getShared()
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


  private prev() {
    if (this.allArticles && this.allArticles.length > 0) {
      this.selectedArticleIndex =
          (this.selectedArticleIndex <= 0 ? (this.allArticles.length - 1) : (this.selectedArticleIndex - 1));
    }
  }

  private next() {
    if (this.allArticles && this.allArticles.length > 0) {
      this.selectedArticleIndex =
          (this.selectedArticleIndex >= (this.allArticles.length - 1) ? 0 : (this.selectedArticleIndex + 1));
    }
  }

  private getPrevArticleName(): string | undefined {
    if (this.allArticles && this.allArticles.length > 1) {
      const index = this.selectedArticleIndex == 0 ? this.allArticles.length - 1 : this.selectedArticleIndex - 1;
      const article = this.allArticles[index];
      return article && article.name ? this.truncate(article.name, 30) : undefined;
    }
    return undefined;
  }

  private getNextArticleName(): string | undefined {
    if (this.allArticles && this.allArticles.length > 1) {
      const index = this.selectedArticleIndex == this.allArticles.length - 1 ? 0 : this.selectedArticleIndex + 1;
      const article = this.allArticles[index];
      return article && article.name ? this.truncate(article.name, 30) : undefined;
    }
    return undefined;
  }

  truncate(str: string, n: number) {
    return (str.length > n) ? str.substr(0, n - 1) + '...' : str;
  }

}
</script>

<style scoped>

</style>
