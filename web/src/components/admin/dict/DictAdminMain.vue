<!--
Navigate between dictionaries admin pages using tabs
-->
<template>
  <div>
    <v-tabs>
      <v-tab to="/admin/dicts/organizations">{{ $t('Организации') }}</v-tab>
      <v-tab to="/admin/dicts/departments">{{ $t('Подразделения') }}</v-tab>
      <v-tab to="/admin/dicts/positions">{{ $t('Позиции') }}</v-tab>
      <v-tab to="/admin/dicts/levels">{{ $t('Уровни') }}</v-tab>
      <v-tab to="/admin/dicts/offices">{{ $t('Офисы') }}</v-tab>
      <v-tab to="/admin/dicts/office_locations">{{ $t('Кабинеты') }}</v-tab>
      <v-tab to="/admin/dicts/office_maps">{{ $t('Карты офисов и кабинетов') }}</v-tab>
    </v-tabs>
    <router-view></router-view>
  </div>
</template>

<script>
import Component from "vue-class-component";
import Vue from "vue";

@Component
export default class DictAdminMain extends Vue {
}
</script>

<style scoped>

</style>
