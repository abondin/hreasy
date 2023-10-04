<template>
  <v-form ref="adminUpdateForm" v-if="data.updateBody">
    <v-card>
      <v-card-title>{{ print(updateTitle, $t('Изменение')) }}</v-card-title>
      <v-card-text>
        <slot name="fields"></slot>
        <!-- Error block -->
        <v-alert v-if="data.actionError" type="error">
          {{ data.actionError }}
        </v-alert>
      </v-card-text>
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-progress-circular class="mr-2" v-if="data.loading" indeterminate></v-progress-circular>
        <v-btn @click="data.closeUpdateDialog()">{{ $t('Закрыть') }}</v-btn>
        <v-btn @click="submitUpdateForm" color="primary" :disabled="data.loading || itemReadonly()">{{
            $t('Изменить')
          }}
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-form>
</template>

<script lang="ts">
import Component from "vue-class-component";
import Vue from "vue";
import {Prop} from "vue-property-decorator";
import TableComponentDataContainer, {
  CreateBody,
  Filter,
  UpdateBody,
  WithId
} from "@/components/shared/table/TableComponentDataContainer";
import {UiConstants} from "@/components/uiconstants";

@Component
export default class HreasyTableUpdateForm<T extends WithId, M extends UpdateBody, C extends CreateBody, F extends Filter<T>> extends Vue {

  @Prop({required: true})
  private data!: TableComponentDataContainer<T, M, C, F>;

  @Prop({required: false})
  private updateTitle?: () => string | string | undefined;


  private submitUpdateForm() {
    const form: any = this.$refs.adminUpdateForm;
    if (form.validate()) {
      return this.data.submitUpdateForm();
    }
  }

  private itemReadonly() {
    return !this.data
        || !this.data.updateCommitAllowed()

  }

  private print = UiConstants.print;
}
</script>

