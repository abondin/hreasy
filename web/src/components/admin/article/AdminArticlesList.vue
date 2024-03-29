<!-- All articles table to admin-->
<template>
  <v-container>
    <v-card>
      <v-card-title>
        <div class="d-flex align-center justify-space-between">
          <!-- Refresh button -->
          <v-btn text icon @click="fetchData()">
            <v-icon>refresh</v-icon>
          </v-btn>
          <v-divider vertical></v-divider>
          <v-text-field
              v-model="filter.search"
              :label="$t('Поиск')" class="mr-5 ml-5"></v-text-field>
          <v-select
              multiple
              clearable
              class="mr-5"
              v-model="filter.articleGroup"
              :items="allGroups()"
              :label="$t('Группа')"></v-select>
          <v-select
              class="mr-5"
              v-model="filter.onlyModerated"
              :items="[{value:false, text:$t('Нет')}, {value:true, text:$t('Да')}]"
              :label="$t('Только модерированные')"></v-select>

          <v-select
              clearable
              class="mr-5"
              v-model="filter.showArchived"
              :items="[{value:false, text:$t('Нет')}, {value:true, text:$t('Да')}]"
              :label="$t('Показать архив')"
              multiple
          ></v-select>

          <v-divider vertical></v-divider>
          <!-- Add new article -->
          <v-tooltip bottom>
            <template v-slot:activator="{ on: ton, attrs: tattrs}">
              <div v-bind="tattrs" v-on="ton" class="col-auto">
                <v-btn text color="primary" :disabled="loading" @click="openEditArticleDialog(null)" icon>
                  <v-icon>mdi-plus</v-icon>
                </v-btn>
              </div>
            </template>
            <span>{{ $t('Добавить статью') }}</span>
          </v-tooltip>
        </div>
      </v-card-title>

      <v-card-text>
        <v-data-table
            :loading="loading"
            :loading-text="$t('Загрузка_данных')"
            :headers="headers"
            :items="filteredItems()"
            multi-sort
            hide-default-footer
            :sort-by="['updatedAt']"
            sort-desc
            disable-pagination>
          <template v-slot:item.name="{ item }">
            <v-btn text @click="openEditArticleDialog(item)">{{ item.name }}
            </v-btn>
          </template>
          <template
              v-slot:item.moderated="{ item }">
            {{ item.moderated ? $t('Да') : $t('Нет') }}
          </template>
          <template
              v-slot:item.archived="{ item }">
            {{ item.archived ? $t('Да') : $t('Нет') }}
          </template>
          <template
              v-slot:item.articleGroup="{ item }">
            {{ getGroupName(item.articleGroup) }}
          </template>
          <template
              v-slot:item.updatedAt="{ item }">
            {{ formatDate(item.updatedAt) }}
          </template>
        </v-data-table>

        <v-dialog v-model="updateArticleDialog" persistent>
          <article-update-form
              v-bind:input="selectedArticle"
              @close="updateArticleDialog=false"
              @submit="fetchData()"></article-update-form>
        </v-dialog>

      </v-card-text>
    </v-card>
  </v-container>
</template>


<script lang="ts">
import Vue from 'vue'
import Component from 'vue-class-component';
import {DataTableHeader} from "vuetify";
import {DateTimeUtils} from "@/components/datetimeutils";
import articleAdminService, {ArticleFull} from "@/components/admin/article/admin.article.service";
import {ALL_ARTICLES_GROUPS} from "@/components/article/article.service";
import ArticleUpdateForm from "@/components/admin/article/ArticleEditForm.vue";

class Filter {
  public search = '';
  public articleGroup: string[] = [];
  public onlyModerated = false;
  public showArchived = false;
}

@Component({
  components: {ArticleUpdateForm}
})
export default class AdminArticlesList extends Vue {
  headers: DataTableHeader[] = [];
  loading = false;
  articles: ArticleFull[] = [];

  private filter: Filter = new Filter();


  private updateArticleDialog = false;
  private selectedArticle: ArticleFull | null = null;

  /**
   * Lifecycle hook
   */
  created() {
    this.reloadHeaders();
    this.fetchData();
  }

  private reloadHeaders() {
    this.headers.length = 0;
    this.headers.push({text: this.$tc('Статья'), value: 'name'});
    this.headers.push({text: this.$tc('Группа'), value: 'articleGroup'});
    this.headers.push({text: this.$tc('Модерированная'), value: 'moderated'});
    this.headers.push({text: this.$tc('Архивная'), value: 'archived'});
    this.headers.push({text: this.$tc('Обновлена'), value: 'updatedAt'});
  }


  private filteredItems() {
    return this.articles.filter(item => {
      let filtered = true;
      if (this.filter.search) {
        filtered = filtered && item.name.toLowerCase().indexOf(this.filter.search.toLowerCase()) >= 0;
      }
      if (this.filter.articleGroup.length > 0) {
        filtered = filtered && this.filter.articleGroup.indexOf(item.articleGroup) >= 0;
      }
      if (this.filter.onlyModerated) {
        filtered = filtered && item.moderated;
      }
      if (!this.filter.showArchived) {
        filtered = filtered && !item.archived;
      }
      return filtered;
    });
  }


  private fetchData() {
    this.loading = true;
    return articleAdminService.getAll()
        .then(data => {
              this.articles = data;
              return this.articles;
            }
        ).finally(() => {
          this.loading = false
        });
  }

  openEditArticleDialog(item: ArticleFull) {
    this.selectedArticle = item;
    this.updateArticleDialog = true;
  }

  allGroups() {
    return ALL_ARTICLES_GROUPS.map(g => {
      return {value: g, text: this.$tc('ARTICLE_GROUP.' + g)}
    });
  }

  getGroupName(groupKey: string | undefined): string {
    let name = '';
    if (groupKey) {
      const group = this.allGroups().find(g => g.value == groupKey);
      if (group) {
        name = group.text;
      }
    }
    return name;
  }


  private formatDate(date: string): string | undefined {
    return DateTimeUtils.formatDateTimeFromIso(date);
  }

}
</script>

<style scoped>

</style>
