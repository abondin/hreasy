<template>
  <v-chip-group>
    <v-chip v-for="chip in chipProps()" :key="chip.id" :color="chip.color" @click="$router.push(chip.url)">
      {{ chip.title }}
    </v-chip>
  </v-chip-group>
</template>

<script lang="ts">
import Component from "vue-class-component";
import Vue from "vue";
import {SalaryRequestLink, SalaryRequestLinkType} from "@/components/salary/salary.service";
import {Prop} from "vue-property-decorator";
import {ReportPeriod} from "@/components/overtimes/overtime.service";

interface ChipProps {
  id: number,
  title: string,
  color: string,
  url: string
}

@Component({
  components: {}
})
export default class SalaryRequestLinksChips extends Vue {
  @Prop({required: true})
  private data!: SalaryRequestLink[];

  @Prop({required: true, default: []})
  private allowedTypes!: SalaryRequestLinkType[];

  chipProps(): ChipProps[] {
    return this.data.filter((link) => this.allowedTypes.indexOf(link.type) >= 0).map(l => this.fromLink(l));
  }

  private fromLink(link: SalaryRequestLink): ChipProps {
    const linkedRequestPeriod = ReportPeriod.fromPeriodId(link.linkedRequest.period);
    let title;
    let color = 'default';
    switch (link.type) {
      case SalaryRequestLinkType.RESCHEDULED:
        if (link.initiator) {
          title = this.$t('Перенос решения на ПЕРИОД', {period: linkedRequestPeriod.toString()}).toString()
          color = 'error';
        } else {
          title = this.$t('Перенесено с').toString() + ' ' + linkedRequestPeriod.toString();
          color = 'default';
        }
        break;
      case SalaryRequestLinkType.MULTISTAGE:
        title = this.$t('Связанный запрос').toString() + ' ' + linkedRequestPeriod.toString();
        color = 'secondary';
        break;
      default:
        throw new Error('Unknown link type');
    }
    return {
      id: link.id,
      title: title,
      color: color,
      url: `/salaries/requests/${linkedRequestPeriod.id}/${link.linkedRequest.id}`
    }
  }

}
</script>
