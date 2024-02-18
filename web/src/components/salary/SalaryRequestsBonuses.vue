<template>
  <salary-requests mode="bonuses"></salary-requests>
</template>

<script lang="ts">
import Component from "vue-class-component";
import {Vue} from "vue-property-decorator";
import SalaryRequests from "@/components/salary/SalaryRequests.vue";


@Component({
  components: {SalaryRequests}
})
export default class SalaryRequestsBonuses extends Vue {
}
</script>

