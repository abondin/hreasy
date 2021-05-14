<!--
Window to show shared articles for all employees
 -->

<template>
  <v-carousel hide-delimiters>
    <template v-slot:prev="{ on, attrs }">
      <v-btn v-bind="attrs" v-on="on" @click="prev" icon><v-icon>mdi-arrow-left</v-icon></v-btn>
    </template>
    <template v-slot:next="{ on, attrs }">
      <v-btn v-bind="attrs" v-on="on" @click="next" icon><v-icon>mdi-arrow-right</v-icon></v-btn>
    </template>

    <v-skeleton-loader class="mx-auto" type="card" v-if="loading"></v-skeleton-loader>
    <v-carousel-item transition="fade-transition" v-if="!loading">
      <v-sheet class="border_all" style="border: 5px red solid">
        <div v-html="content"></div>
      </v-sheet>
    </v-carousel-item>
  </v-carousel>
</template>


<script lang="ts">
import Vue from 'vue'
import Component from 'vue-class-component';
import articleService, {ArticleMeta} from "@/components/article/article.service";
import logger from "@/logger";

const marked = require("marked");

const namespace: string = 'auth';

@Component({})
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
      let formatted = markdown;
      try {
        formatted = marked(markdown)
      } catch (error) {
        logger.error("Unable parse markdown document", error)
      }
      this.content = formatted;
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
