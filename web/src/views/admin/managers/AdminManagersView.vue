<template>
  <TableFirstPageLayout test-id="admin-managers-view">
    <admin-managers-table class="h-100"
      :title="t('Менеджеры')"
      :editable="permissions.canAdminManagers()"
      test-id="admin-managers"
    />
  </TableFirstPageLayout>
</template>

<script setup lang="ts">
import { useI18n } from "vue-i18n";
import TableFirstPageLayout from "@/components/shared/TableFirstPageLayout.vue";
import { usePermissions } from "@/lib/permissions";
import AdminManagersTable from "@/views/admin/managers/components/AdminManagersTable.vue";

const { t } = useI18n();
const permissions = usePermissions();
</script>
