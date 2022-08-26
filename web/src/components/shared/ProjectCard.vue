<template>
  <v-container>
    <v-row>
      <v-col cols="6" lg="3">
        <my-description-list :items="[
            {t:$t('Наименование'), d:project.name}
            ,{t:$t('Отдел'), d:project.department ? project.department.name : $t('Не задан')}
            ,{t:$t('Бизнес аккаунт'), d:project.businessAccount ? project.businessAccount.name : $t('Не задан')}
            ,{t:$t('Заказчик'), d:(project.customer ? project.customer : $t('Не задан'))}
            ,{t:$t('Начало'), d:formatDate(project.startDate)}
            ,{t:$t('Окончание'), d:formatDate(project.endDate)}
        ]">
        </my-description-list>
      </v-col>
      <v-col v-if="project.info">
        <v-card>
          <v-card-text>
            <div v-html="project.info" style="height: 250px" class="overflow-y-auto"></div>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<script lang="ts">
import Vue from "vue";
import {ProjectFullInfo} from "../admin/project/admin.project.service";
import {Prop} from "vue-property-decorator";
import Component from "vue-class-component";
import {DateTimeUtils} from "@/components/datetimeutils";
import MyDescriptionList from "@/components/shared/MyDescriptionList.vue";

@Component({
  components: {MyDescriptionList}
})
export default class ProjectCard extends Vue {
  @Prop({required: true})
  private project!: ProjectFullInfo;

  private formatDate(date: string | undefined): string | undefined {
    return DateTimeUtils.formatFromIso(date);
  }
}
</script>

<style scoped>

</style>
