<template>
  <div>
    <v-tabs>
      <v-tab to="/salaries/requests/increases">{{ $t('Повышения') }}</v-tab>
      <v-tab to="/salaries/requests/bonuses">{{ $t('Бонусы') }}</v-tab>
    </v-tabs>
    <router-view></router-view>
  </div>
</template>

<script lang="ts">
import Component from "vue-class-component";
import {Vue} from "vue-property-decorator";


@Component({
  components: {}
})
export default class SalaryRequestsMain extends Vue {
}
</script>

