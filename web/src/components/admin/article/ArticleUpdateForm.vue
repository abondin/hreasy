<!-- Dialog to edit article -->
<template>
  <v-form ref="articleEditHtmlForm">
    <v-card>
      <v-card-title>{{ $t('Редактирование статьи') }}</v-card-title>
      <v-card-text>
        <v-select class="mr-5"
                  v-model="articleForm.moderated"
                  :items="[{value:false, text:$t('Нет')}, {value:true, text:$t('Да')}]"
                  :label="$t('Модерированная')"></v-select>


        <Editor v-model="articleForm.content" mode="review">
        </Editor>

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

        <v-select class="mr-5"
                  v-model="articleForm.archived"
                  :items="[{value:false, text:$t('Нет')}, {value:true, text:$t('Да')}]"
                  :label="$t('Архив')"></v-select>



        <!-- Error block -->
        <v-alert v-if="error" type="error">
          {{ error }}
        </v-alert>
      </v-card-text>
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn @click="closeDialog()">{{ $t('Закрыть') }}</v-btn>
        <v-btn @click="submit()" color="primary">{{ $t('Применить')}}</v-btn>
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
import {ALL_ARTICLES_GROUPS, Article, ARTICLE_SHARED_GROUP} from "@/components/article/article.service";
import articleAdminService, {
  ArticleFull,
  CreateEmptyArticleBody, UpdateArticleBody
} from "@/components/admin/article/admin.article.service";
import {Editor} from "vuetify-markdown-editor";
import {Prop, Watch} from "vue-property-decorator";


class ArticleForm {
  public name = '';
  public articleGroup = '';
  public description = '';
  public content = '';
  public moderated = false;
  public archived = false;
}

@Component(
    {components: {MyDateFormComponent, Editor}}
)

export default class ArticleEditForm extends Vue {
  loading: boolean = false;

  private articleForm = new ArticleForm();

  private error: String | null = null;

  @Prop({required:true})
  private input!: ArticleFull;

  private created() {
    this.reset();
  }

  @Watch("input")
  private watchInput(){
    this.reset();
  }

  private reset() {
    this.error = null;
    this.articleForm.name = '';
    this.articleForm.articleGroup = ARTICLE_SHARED_GROUP;
    this.articleForm.description = '';
    this.articleForm.content = '';
    this.articleForm.moderated = false;
    this.articleForm.archived = false;
    if (this.input){
      this.articleForm.name = this.input.name;
      this.articleForm.articleGroup = this.input.articleGroup;
      this.articleForm.description = this.input.description ? this.input.description : '';
      this.articleForm.content = this.input.content ? this.input.content : '';
      this.articleForm.moderated = this.input.moderated;
      this.articleForm.archived = this.input.archived;
    }
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
        description: this.articleForm.description,
        moderated : this.articleForm.moderated,
        archived : this.articleForm.archived,
        content : this.articleForm.content
      } as UpdateArticleBody;
      return articleAdminService.update(this.input.id, body)
          .then((result) => {
            logger.log(`Article updated: ${result}`);
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
