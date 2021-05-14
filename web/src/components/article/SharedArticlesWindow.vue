<!--
Window to show shared articles for all employees
 -->

<template>
  <v-card>
    <v-skeleton-loader v-if="loading" class="mx-auto" type="card"></v-skeleton-loader>
    <Editor v-if="!loading" v-model="content" mode="viewer" ref="editor"></Editor>
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
import articleService, {ArticleMeta} from "@/components/article/article.service";
import {Editor} from "vuetify-markdown-editor";

const namespace: string = 'auth';

@Component({
  components: {Editor}
})
export default class SharedArticlesWindow extends Vue {

  private allArticles: ArticleMeta[] = [];
  private selectedArticleIndex = -1;
  private loading = false;
  private content = '';

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
        .then(() => this.renderContent(false))
        .finally(() => {
          this.loading = false;
        })
  }

  private renderContent(showLoading = true) {
    if (this.selectedArticleIndex == -1) {
      this.content = '';
      return;
    }
    if (showLoading) {
      this.loading = true;
    }
    articleService
        .getSharedArticleContent(this.allArticles[this.selectedArticleIndex].name).then(markdown => {
      this.content = markdown;
    })
        .finally(() => {
          if (showLoading) {
            this.loading = false;
          }
        })
  }

  private prev() {
    if (this.allArticles && this.allArticles.length > 0) {
      this.selectedArticleIndex--;
      if (this.selectedArticleIndex < 0) {
        this.selectedArticleIndex = this.allArticles.length - 1;
      }
      this.renderContent();
    }
  }

  private next() {
    if (this.allArticles && this.allArticles.length > 0) {
      this.selectedArticleIndex++;
      if (this.selectedArticleIndex >= this.allArticles.length) {
        this.selectedArticleIndex = 0;
      }
      this.renderContent();
    }
  }

}
</script>

<style scoped>

</style>
