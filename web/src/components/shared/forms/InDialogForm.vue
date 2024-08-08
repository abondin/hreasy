<template>
  <v-dialog v-if="data" v-model="data.dialog" :width="width">
    <v-form :ref="formRef">
      <v-card>
        <v-card-title>{{ print(title, $t('Форма')) }}</v-card-title>
        <v-card-text>
          <slot name="fields"></slot>
          <!-- Error block -->
          <v-alert v-if="data.error" type="error">
            {{ data.error }}
          </v-alert>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-progress-circular class="mr-2" v-if="data.loading" indeterminate></v-progress-circular>
          <v-btn @click="cancel">{{ closeButtonText || $t('Закрыть') }}</v-btn>
          <v-btn @click="submit" color="primary" :disabled="data.loading">{{
              submitButtonText || $t('Применить')
            }}
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-form>
  </v-dialog>
</template>

<script lang="ts">
import Component from "vue-class-component";
import Vue from "vue";
import {Prop} from "vue-property-decorator";
import {UiConstants} from "@/components/uiconstants";
import {InDialogActionDataContainer} from "@/components/shared/forms/InDialogActionDataContainer";

@Component
export default class InDialogForm<ID, T> extends Vue {

  @Prop({required: true})
  private data!: InDialogActionDataContainer<ID, T>;

  @Prop({required: false})
  private title?: () => string | string | undefined;

  @Prop({required: false})
  private closeButtonText?: string;
  @Prop({required: false})
  private submitButtonText?: string;

  @Prop({required: true})
  private formRef!: string;

  @Prop({required: false, default: 'md'})
  private size!: 'md' | 'lg';

  private width = 500;

  private print = UiConstants.print;

  created() {
    switch (this.size) {
      case 'lg':
        this.width = 800;
        break;
      case 'md':
        this.width = 500;
        break;
    }
  }

  private submit() {
    const form: any = this.$refs[this.formRef];
    if (form.validate()) {
      return this.data.submit(() => this.$emit('submit'));
    }
  }

  private cancel() {
    this.data.cancel();
  }

}
</script>

