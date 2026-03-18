<template>
  <v-container class="py-6" data-testid="admin-managers-view">
    <admin-managers-table
      :title="t('\u041c\u0435\u043d\u0435\u0434\u0436\u0435\u0440\u044b')"
      :editable="permissions.canAdminManagers()"
      test-id="admin-managers"
    />
  </v-container>
</template>

<script setup lang="ts">
import { useI18n } from "vue-i18n";
import { usePermissions } from "@/lib/permissions";
import AdminManagersTable from "@/views/admin/managers/components/AdminManagersTable.vue";

const { t } = useI18n();
const permissions = usePermissions();
</script>
