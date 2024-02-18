<template>
  <salary-requests mode="increases"></salary-requests>
</template>

<script lang="ts">
import Component from "vue-class-component";
import {Vue} from "vue-property-decorator";
import SalaryRequests from "@/components/salary/SalaryRequests.vue";


@Component({
  components: {SalaryRequests}
})
export default class SalaryRequestsIncreases extends Vue {
}
</script>

