<!-- Dialog to create or update employee -->
<template>
  <v-form ref="tsRecordEditForm">
    <v-card>
      <v-card-title>
        <span v-if="input.isNew()">{{ $t('Фиксация рабочего времени') }}</span>
        <span v-else>{{ $t('Изменение рабочего времени') }}</span>
        <v-spacer></v-spacer>
        <v-btn icon @click="closeDialog()">
          <v-icon>mdi-close</v-icon>
        </v-btn>
      </v-card-title>
      <v-card-text>
        <v-container>
          <v-row>
            <!--<editor-fold desc="1 row (display name)">-->
            {{ input.selectedItem }}
            <!--</editor-fold>-->
          </v-row>
        </v-container>
        <!-- Error block -->
        <v-alert v-if="input.error" type="error">
          {{ input.error }}
        </v-alert>
      </v-card-text>
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn @click="closeDialog()">{{ $t('Закрыть') }}</v-btn>
        <v-btn @click="submit()" color="primary">{{ input.isNew ? $t('Создать') : $t('Изменить') }}</v-btn>
      </v-card-actions>
    </v-card>
  </v-form>
</template>


<script lang="ts">
import Vue from 'vue'
import Component from "vue-class-component";
import {Prop} from "vue-property-decorator";
import MyDateFormComponent from "@/components/shared/MyDateFormComponent.vue";
import logger from "@/logger";
import {errorUtils} from "@/components/errors";
import {DateTimeUtils} from "@/components/datetimeutils";
import {TimesheetHours} from "@/components/ts/timesheet.service";


export class EditTimesheetRecordData {
  private dialog = false;
  private selectedItem: TimesheetHours | null = null;
  private _error: string | null = null;
  private loading = false;

  public isNew(): boolean {
    return this.selectedItem == null || this.selectedItem.id == null;
  }

  public show(value: TimesheetHours) {
    this.selectedItem = value;
    this.dialog = true;
  }

  public close() {
    this._error = null;
    this.selectedItem = null;
    this.dialog = false;
  }

  public set error(error: any) {
    this._error = errorUtils.shortMessage(error);
  }

  public get error() {
    return this._error;
  }
}

@Component(
    {components: {MyDateFormComponent}}
)
export default class TimesheetRecordEditForm extends Vue {
  @Prop({required: true})
  private input!: EditTimesheetRecordData;


  private closeDialog() {
    this.input.close();
  }

  private submit() {
    const form: any = this.$refs.tsRecordEditForm;
    if (form.validate()) {
      logger.log("TODO")
    }
    this.closeDialog();
  }

  private validateDate(formattedDate: string, allowEmpty = true): boolean {
    return DateTimeUtils.validateFormattedDate(formattedDate, allowEmpty);
  }

}
</script>
