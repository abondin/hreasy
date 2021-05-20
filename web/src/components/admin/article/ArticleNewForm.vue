<!-- Dialog to create new article -->
<template>
  <v-form ref="articleEditHtmlForm">
    <v-card>
      <v-card-title>{{ $t('Добавление статьи') }}</v-card-title>
      <v-card-text>
        <v-select class="mr-5"
            v-model="articleForm.articleGroup"
            :items="allGroups()"
            :label="$t('Группа')"
            :rules="[v=>(v.length>0 || $t('Обязательное поле'))]"
        ></v-select>

        <v-text-field
            v-model="articleForm.name"
            :label="$t('Название статьи')"
            :counter="255"
            :rules="[v=>(v && v.length <= 255 || $t('Обязательное поле. Не более N символов', {n:255}))]"
        ></v-text-field>

        <v-textarea
            v-model="articleForm.description"
            row-height="5"
            :counter="1024"
            :rules="[v=>(!v ||  v.length <= 1024 || $t('Не более N символов', {n:1024}))]"
            :label="$t('Описание')"
        ></v-textarea>

        <!-- Error block -->
        <v-alert v-if="error" type="error">
          {{ error }}
        </v-alert>
      </v-card-text>
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn @click="closeDialog()">{{ $t('Закрыть') }}</v-btn>
        <v-btn @click="submit()" color="primary">{{ $t('Создать')}}</v-btn>
      </v-card-actions>
    </v-card>
  </v-form>
</template>


<script lang="ts">
import Vue from 'vue'
import Component from "vue-class-component";
import MyDateFormComponent from "@/components/shared/MyDateFormComponent.vue";
import logger from "@/logger";
import {errorUtils} from "@/components/errors";
import {ALL_ARTICLES_GROUPS, ARTICLE_SHARED_GROUP} from "@/components/article/article.service";
import articleAdminService, {CreateEmptyArticleBody} from "@/components/admin/article/admin.article.service";


class ArticleForm {
  public name = '';
  public articleGroup = '';
  public description = '';
}

@Component(
    {components: {MyDateFormComponent}}
)

export default class ArticleEditForm extends Vue {
  loading: boolean = false;

  private articleForm = new ArticleForm();

  private error: String | null = null;

  private created() {
    this.reset();
  }

  private reset() {
    this.error = null;
    this.articleForm.name = '';
    this.articleForm.articleGroup = ARTICLE_SHARED_GROUP;
    this.articleForm.description = '';
  }

  private closeDialog() {
    this.reset();
    this.$nextTick(function () {
      this.$emit('close');
    })
  }

  private submit() {
    const form: any = this.$refs.articleEditHtmlForm;
    if (form.validate()) {
      const body = {
        name: this.articleForm.name,
        articleGroup: this.articleForm.articleGroup,
        description: this.articleForm.description
      } as CreateEmptyArticleBody;
      return articleAdminService.create(body)
          .then((result) => {
            logger.log(`Article created: ${result}`);
            this.$emit('submit');
            this.$emit('close');
          })
          .catch(error => {
            this.error = errorUtils.shortMessage(error);
          });
    }
  }

  allGroups() {
    return ALL_ARTICLES_GROUPS;
  }

}
</script>
