<!--
Window to show shared articles for all employees
 -->

<template>
  <v-card
      v-if="allArticles && allArticles.length>0 && selectedArticleIndex>=0 && allArticles[selectedArticleIndex].content">
    <v-skeleton-loader v-if="loading" class="mx-auto" type="card"></v-skeleton-loader>
    <Editor v-if="!loading" v-model="allArticles[selectedArticleIndex].content" mode="viewer" ref="editor"></Editor>
    <v-card-actions class="justify-space-between">
      <v-btn text @click="prev">
        <v-icon>mdi-chevron-left</v-icon>
      </v-btn>
      <v-btn text @click="next">
        <v-icon>mdi-chevron-right</v-icon>
      </v-btn>
    </v-card-actions>
  </v-card>
</template>


<script lang="ts">
import Vue from 'vue'
import Component from 'vue-class-component';
import articleService, {Article} from "@/components/article/article.service";
import {Editor} from "vuetify-markdown-editor";

const namespace: string = 'auth';

@Component({
  components: {Editor}
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
      this.selectedArticleIndex--;
      if (this.selectedArticleIndex < 0) {
        this.selectedArticleIndex = this.allArticles.length - 1;
      }
    }
  }

  private next() {
    if (this.allArticles && this.allArticles.length > 0) {
      this.selectedArticleIndex++;
      if (this.selectedArticleIndex >= this.allArticles.length) {
        this.selectedArticleIndex = 0;
      }
    }
  }

}
</script>

<style scoped>

</style>
