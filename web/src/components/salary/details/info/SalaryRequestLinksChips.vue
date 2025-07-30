<template>
  <v-chip-group class="add-link-chip-parent mt-0 mb-0">

    <v-tooltip v-for="chip in chipProps()" :key="chip.link.id" bottom color="white">
      <template v-slot:activator="{ on: ton, attrs: tattrs}">
        <div v-bind="tattrs" v-on="ton" class="d-inline">
          <v-chip class="mt-0 mb-0" :color="chip.color"
                  :small="small" :outlined="small"
                  :close="Boolean(deleteLinkAction)"
                  @click:close="openDeleteLinkDialog(chip.link.id)"
          >
            {{ chip.title }}
          </v-chip>
        </div>
      </template>
      <div>
        <dl class="info-dl text--primary text-wrap ml-lg-5">
          <dt>{{ $t('Создана') }}:</dt>
          <dd>{{ chip.link.createdBy.name }} ({{ formatDateFromDateTime(chip.link.createdAt) }})</dd>
          <dt v-if="chip.link.comment">{{ $t('Примечание') }}:</dt>
          <dd v-if="chip.link.comment">{{ chip.link.comment }}</dd>
        </dl>
      </div>
    </v-tooltip>

    <v-chip class="add-link-chip mt-0 mb-0" v-if="addLinkAction" @click="openAddLinkDialog()" :small="small"
            color="primary" text>
      <v-icon :small="small">mdi-plus</v-icon>
    </v-chip>


    <in-dialog-form v-if="addLinkAction" size="lg" form-ref="addLinkForm" :data="addLinkAction"
                    :title="$t('Добавить связанный запрос')"
                    v-on:submit="emitReload">
      <template v-slot:fields>
        <v-textarea
            v-model="addLinkAction.formData.comment"
            :rules="[v=>(!v || v.length <= 4096 || $t('Не более N символов', {n:4096}))]"
            :label="$t('Примечание')">
        </v-textarea>
      </template>
    </in-dialog-form>
    <in-dialog-form v-if="deleteLinkAction" size="md" form-ref="deleteLinkForm" :data="deleteLinkAction"
                    :title="$t('Удалить запрос')"
                    v-on:submit="emitReload()">
      <template v-slot:fields>
        <v-card-text>
          {{ $t('Вы уверены, что хотите удалить связь?') }}
        </v-card-text>
      </template>
    </in-dialog-form>
  </v-chip-group>
</template>

<script lang="ts">
import Component from "vue-class-component";
import Vue from "vue";
import {SalaryRequestLink, SalaryRequestLinkType} from "@/components/salary/salary.service";
import {Prop} from "vue-property-decorator";
import {ReportPeriod} from "@/components/overtimes/overtime.service";
import {
  SalaryRequestAddLinkAction,
  SalaryRequestAddLinkFormData
} from "@/components/salary/details/info/salary-request.addlink.action";
import InDialogForm from "@/components/shared/forms/InDialogForm.vue";
import {SalaryRequestDeleteLinkAction} from "@/components/salary/details/info/salary-request.deletelink.action";
import {DateTimeUtils} from "@/components/datetimeutils";

interface ChipProps {
  link: SalaryRequestLink,
  title: string,
  color: string,
  url: string
}

@Component({
  components: {InDialogForm}
})
export default class SalaryRequestLinksChips extends Vue {
  @Prop({required: true})
  private data!: SalaryRequestLink[];
  @Prop({required: false})
  private addLinkAction?: SalaryRequestAddLinkAction;

  @Prop({required: false})
  private deleteLinkAction?: SalaryRequestDeleteLinkAction;


  @Prop({required: false, default: () => []})
  private allowedTypes!: SalaryRequestLinkType[];

  @Prop({required: false, default: false})
  private small!: boolean;

  @Prop({required: false})
  private source?: number;

  @Prop({required: false})
  private destination?: number;

  formatDateFromDateTime = (v: string | undefined) => DateTimeUtils.formatDateFromIsoDateTime(v);

  chipProps(): ChipProps[] {
    return this.data.filter(l => this.allowedTypes.length == 0 || this.allowedTypes.indexOf(l.type) >= 0).map(l => this.fromLink(l));
  }

  private fromLink(link: SalaryRequestLink): ChipProps {
    const linkedRequestPeriod = ReportPeriod.fromPeriodId(link.linkedRequest.period);
    let title;
    let color = 'default';
    switch (link.type) {
      case SalaryRequestLinkType.RESCHEDULED:
        if (link.initiator) {
          title = this.source ? this.$t('Перенесено на').toString() : this.$t('Перенос решения на ПЕРИОД', {period: linkedRequestPeriod.toString()}).toString()
          color = 'error';
        } else {
          title = this.source ? this.$t('Перенесено с').toString() : this.$t('Перенесено с').toString() + ' ' + linkedRequestPeriod.toString();
          color = 'default';
        }
        break;
      case SalaryRequestLinkType.MULTISTAGE:
        title = this.source ? this.$t('Связанный').toString() : this.$t('Связанный запрос').toString() + ' ' + linkedRequestPeriod.toString();
        color = 'secondary';
        break;
      default:
        throw new Error('Unknown link type');
    }
    return {
      link: link,
      title: title,
      color: color,
      url: `/salaries/requests/${linkedRequestPeriod.id}/${link.linkedRequest.id}`,
    }
  }

  private openAddLinkDialog() {
    if (this.addLinkAction) {
      const form: SalaryRequestAddLinkFormData = {
        source: this.source || null,
        destination: this.destination || null,
        type: SalaryRequestLinkType.MULTISTAGE,
        comment: null
      }
      return this.addLinkAction.openDialog(null, form);
    }
  }

  private openDeleteLinkDialog(linkId: number) {
    if (this.deleteLinkAction) {
      return this.deleteLinkAction.openDialog(linkId);
    }
  }

  private emitReload() {
    this.$emit('updated');
  }

}
</script>

<style scoped>
.add-link-chip {
  display: none;
}

.add-link-chip-parent:hover .add-link-chip {
  display: inline;
}

.info-dl {
  display: grid;
  grid-template-columns: max-content auto;

  > dt {
    font-weight: bolder;
    max-width: 300px;
    grid-column-start: 1;
  }

  > dd {
    grid-column-start: 2;
    margin-left: 10px;
    max-width: 400px;
  }
}
</style>
