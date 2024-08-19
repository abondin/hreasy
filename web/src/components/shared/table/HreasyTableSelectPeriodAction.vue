<template>
  <div class="flex-column">
    <v-btn @click.stop="data.decrementPeriod()" text x-small class="pl-0 ml-0">
      <v-icon>mdi-chevron-left</v-icon>
    </v-btn>
    <span class="ml-1 mr-2">
            {{ data.selectedPeriod }}
            <v-icon small v-if="data.periodClosed" color="primary"
                    :title="$t('Период закрыт для внесения изменений')">mdi-lock</v-icon>
          </span>
    <v-btn @click.stop="data.incrementPeriod()" text x-small class="pr-0 mr-0">
      <v-icon>mdi-chevron-right</v-icon>
    </v-btn>
    <v-tooltip bottom>
      <template v-slot:activator="{ on: ton, attrs: tattrs}">
        <span v-bind="tattrs" v-on="ton" class="ml-0 pl-0 mt-0 pt-0">
          <v-btn v-if="!data.periodClosed" link :disabled="data.loading" @click="data.closePeriod"
                 icon>
            <v-icon>mdi-lock</v-icon>
          </v-btn>
          <v-btn v-if="data.periodClosed" link :disabled="data.loading" @click="data.reopenPeriod"
                 icon>
            <v-icon>mdi-lock-open</v-icon>
          </v-btn>
        </span>
      </template>
      <span>{{
          $t(data.periodClosed ? 'Переоткрыть период. Вернуть возможность вносить изменения'
              : 'Закрыть период. Запретить внесение изменений.')
        }}</span>
    </v-tooltip>
  </div>
</template>

<script lang="ts">
import Component from "vue-class-component";
import Vue from "vue";
import {Prop} from "vue-property-decorator";
import {
  DataContainerWithPeriodSelectionSupport
} from "@/components/shared/table/DataContainerWithPeriodSelectionSupport";

@Component
export default class HreasyTableSelectPeriodAction<T extends DataContainerWithPeriodSelectionSupport> extends Vue {
  @Prop({required: true})
  private data!: T;


}
</script>

