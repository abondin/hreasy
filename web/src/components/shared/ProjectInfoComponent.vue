<template>
  <v-container fluid class="pa-0">
    <v-row v-if="project" justify="center" class="ma-0">
      <v-col cols="12" sm="6" class="pa-2">
        <v-card flat class="ma-0">
          <v-card-text class="pa-2">
            <v-list dense class="pa-0">
              <v-list-item>
                <v-list-item-content>
                  <v-list-item-title>{{ $t('Наименование') }}:</v-list-item-title>
                  <v-list-item-subtitle>{{ project.name }}</v-list-item-subtitle>
                </v-list-item-content>
              </v-list-item>
              <v-list-item>
                <v-list-item-content>
                  <v-list-item-title>{{ $t('Отдел') }}:</v-list-item-title>
                  <v-list-item-subtitle>{{
                      project.department ? project.department.name : this.$t('Не задан')
                    }}
                  </v-list-item-subtitle>
                </v-list-item-content>
              </v-list-item>
              <v-list-item>
                <v-list-item-content>
                  <v-list-item-title>{{ $t('Бизнес аккаунт') }}:</v-list-item-title>
                  <v-list-item-subtitle>{{
                      project.businessAccount ? project.businessAccount.name : this.$t('Не задан')
                    }}
                  </v-list-item-subtitle>
                </v-list-item-content>
              </v-list-item>
              <v-list-item>
                <v-list-item-content>
                  <v-list-item-title>{{ $t('Заказчик') }}:</v-list-item-title>
                  <v-list-item-subtitle>{{
                      project.customer ? project.customer : $t('Не задан')
                    }}
                  </v-list-item-subtitle>
                </v-list-item-content>
              </v-list-item>
              <v-list-item>
                <v-list-item-content>
                  <v-list-item-title>{{ $t('Начало') }}:</v-list-item-title>
                  <v-list-item-subtitle>{{
                      formatDatePlanActual(project.planStartDate, project.startDate)
                    }}
                  </v-list-item-subtitle>
                </v-list-item-content>
              </v-list-item>
              <v-list-item>
                <v-list-item-content>
                  <v-list-item-title>{{ $t('Окончание') }}:</v-list-item-title>
                  <v-list-item-subtitle>{{
                      formatDatePlanActual(project.planEndDate, project.endDate)
                    }}
                  </v-list-item-subtitle>
                </v-list-item-content>
              </v-list-item>
              <v-list-item v-for="managersByType in managers()" v-bind:key="managersByType.type">
                <v-list-item-content>
                  <v-list-item-title>{{ managersByType.type == 'project' ? $t('Менеджеры проекта') : $t('Менеджеры бизнес аккаунта')}}:</v-list-item-title>
                  <v-list-item-subtitle>
                    <v-chip v-for="m in managersByType.managers" :key="m.id" class="mr-1 mb-1" outlined>
                      {{ m.employeeName }}
                      <v-tooltip bottom>
                        <template v-slot:activator="{ on: ton, attrs: tattrs}">
                          <v-icon v-bind="tattrs" v-on="ton" small>mdi-help-circle</v-icon>
                        </template>
                        <span>{{
                            $t("Основное направление") + ': ' + $t('MANAGER_RESPONSIBILITY_TYPE.' + m.responsibilityType)
                          }}</span>
                      </v-tooltip>
                    </v-chip>
                  </v-list-item-subtitle>
                </v-list-item-content>
              </v-list-item>
            </v-list>
          </v-card-text>
        </v-card>
      </v-col>
      <v-col v-if="project.info" cols="12" sm="6" class="pa-2">
        <v-card flat class="ma-0">
          <v-card-text class="pa-2">
            <div v-html="project.info" :style="{'max-height': maxHeight}" class="overflow-y-auto"></div>
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
import {ProjectInfo} from "@/store/modules/dict.service";
import {ManagerOfObject} from "@/store/modules/dict";

@Component({
  components: {}
})
export default class ProjectInfoComponent extends Vue {
  @Prop({required: true})
  private project!: ProjectFullInfo | ProjectInfo;

  private managers(): { type: 'project'|'business_account', managers:  ManagerOfObject[] }[] {
    const project = this.project && 'managers' in this.project && this.project.managers && this.project.managers.length > 0 ? this.project.managers : null;
    const ba = this.project && 'baManagers' in this.project && this.project.baManagers && this.project.baManagers.length > 0 ? this.project.baManagers :null;
    const result:{ type: 'project'|'business_account', managers:  ManagerOfObject[] }[]=[];
    if (project){
      result.push({type:'project', managers:project});
    }
    if (ba){
      result.push({type:'business_account', managers:ba});
    }
    return result;
  }

  @Prop({required: false, default: "250px"})
  private maxHeight!: string;

  private formatDate(date: string | undefined): string | undefined {
    return DateTimeUtils.formatFromIso(date);
  }

  private formatDatePlanActual(plan: string | undefined, actual: string | undefined) {
    let result = '';
    if (actual) {
      result += this.formatDate(actual) + ` (${this.$t('факт')})`;
    }
    if (plan) {
      if (result) {
        result += ', ';
      }
      result += this.formatDate(plan) + ` (${this.$t('план')})`;
    }
    return result;
  }
}
</script>

<style scoped>

</style>
