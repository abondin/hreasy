<!--
Navigate between employee admin pages using tabs
-->
<template>
  <div>
    <v-tabs>
      <v-tab to="/admin/employees/list">{{ $t('Сотрудники') }}</v-tab>
      <v-tab to="/admin/employees/import">{{ $t('Импорт сотрудников из Excel') }}</v-tab>
      <v-tab to="/admin/employees/kids">{{ $t('Дети') }}</v-tab>
      <v-tab to="/admin/employees/kids-import">{{ $t('Импорт детей из Excel') }}</v-tab>
    </v-tabs>
    <router-view></router-view>
  </div>
</template>

<script lang="ts">
import Vue from 'vue'
import Component from "vue-class-component";


@Component
export default class AdminEmployeeAndKidsTabs extends Vue {

}
</script>

