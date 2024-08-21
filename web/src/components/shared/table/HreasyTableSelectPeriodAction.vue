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
          <v-btn v-if="!data.periodClosed" link :disabled="data.loading" @click="openClosePeriodDialog()"
                 icon>
            <v-icon>mdi-lock</v-icon>
          </v-btn>
          <v-btn v-if="data.periodClosed" link :disabled="data.loading" @click="openReopenPeriodDialog()"
                 icon>
            <v-icon>mdi-lock-open</v-icon>
          </v-btn>
        </span>
      </template>
      <span>{{
          title()
        }}</span>
    </v-tooltip>

    <in-dialog-form size="md" :data="action" form-ref="closePeriodForm" :title="$t('Подтвердите действие')">
      <template v-slot:fields>
        <p>{{ data.periodClosed ? $t('Переоткрыть период') : $t('Закрыть период') }}</p>
      </template>
    </in-dialog-form>
  </div>
</template>

<script lang="ts">
import Component from "vue-class-component";
import Vue from "vue";
import {Prop} from "vue-property-decorator";
import {
  DataContainerWithPeriodSelectionSupport
} from "@/components/shared/table/DataContainerWithPeriodSelectionSupport";
import {InDialogActionDataContainer} from "@/components/shared/forms/InDialogActionDataContainer";
import InDialogForm from "@/components/shared/forms/InDialogForm.vue";


@Component({
  components: {InDialogForm}
})
export default class HreasyTableSelectPeriodAction<T extends DataContainerWithPeriodSelectionSupport> extends Vue {
  @Prop({required: true})
  private data!: T;

  private action = new InDialogActionDataContainer<number, 'close' | 'reopen'>(this.submitAction);

  private submitAction(itemId: number | null, action: 'close' | 'reopen' | null) {
    if (action === 'close') {
      return this.data.closePeriod();
    } else if (action === 'reopen') {
      return this.data.reopenPeriod();
    }
  }

  private title() {
    return this.$t(this.data.periodClosed ? 'Переоткрыть период. Вернуть возможность вносить изменения'
        : 'Закрыть период. Запретить внесение изменений.')
  }

  private openClosePeriodDialog() {
    this.action.openDialog(null, 'close');
  }

  private openReopenPeriodDialog() {
    this.action.openDialog(null, 'reopen');
  }

}
</script>

