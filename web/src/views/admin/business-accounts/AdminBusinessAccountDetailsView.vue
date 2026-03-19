<template>
  <admin-detail-page-layout
    :back-to="{ name: 'admin-business-accounts' }"
    :back-label="t('\u0411\u0438\u0437\u043D\u0435\u0441 \u0410\u043A\u043A\u0430\u0443\u043D\u0442\u044B')"
    :title="businessAccount?.name ?? t('\u041E\u0442\u0441\u0443\u0442\u0441\u0442\u0432\u0443\u044E\u0442 \u0434\u0430\u043D\u043D\u044B\u0435')"
    :error="error"
    :show-primary-card="Boolean(businessAccount)"
    test-id="admin-business-account-details-view"
    card-test-id="admin-business-account-details-card"
  >
    <template #leading-actions>
      <v-btn icon="mdi-refresh" variant="text" :loading="loading" @click="load" />
    </template>

    <template #trailing-actions>
      <v-btn
        icon="mdi-pencil"
        color="primary"
        variant="text"
        :disabled="loading"
        data-testid="admin-business-account-edit"
        @click="editDialog = true"
      />
    </template>

    <template #summary>
      <admin-detail-summary-card
        v-if="businessAccount"
        :items="summaryItems"
      />
    </template>

    <template #content>
      <div class="admin-detail-section h-100">
        <div
          v-if="!businessAccount?.description"
          class="text-body-2 text-medium-emphasis py-2"
        >
          {{ t("\u041D\u0435 \u0437\u0430\u0434\u0430\u043D") }}
        </div>
        <div v-else>{{ businessAccount.description }}</div>
      </div>
    </template>

    <admin-managers-table
      v-if="businessAccount"
      :selected-object="{ id: businessAccount.id, type: 'business_account' }"
      :title="t('\u041C\u0435\u043D\u0435\u0434\u0436\u0435\u0440\u044B \u0431\u0438\u0437\u043D\u0435\u0441 \u0430\u043A\u043A\u0430\u0443\u043D\u0442\u0430')"
      :editable="permissions.canAdminManagers()"
      mode="compact"
      test-id="admin-business-account-managers"
    />

    <admin-business-account-positions-card
      v-if="businessAccount"
      :business-account-id="businessAccount.id"
    />

    <v-dialog v-model="editDialog" persistent max-width="720">
      <admin-business-account-form
        :input="businessAccount"
        @close="editDialog = false"
        @saved="onSaved"
      />
    </v-dialog>
  </admin-detail-page-layout>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import { useRoute } from "vue-router";
import { useI18n } from "vue-i18n";
import AdminDetailPageLayout from "@/components/shared/AdminDetailPageLayout.vue";
import AdminDetailSummaryCard, { type AdminDetailSummaryItem } from "@/components/shared/AdminDetailSummaryCard.vue";
import { errorUtils } from "@/lib/errors";
import { usePermissions } from "@/lib/permissions";
import {
  fetchAdminBusinessAccount,
  type BusinessAccountInfo,
} from "@/services/admin/admin-business-account.service";
import AdminBusinessAccountForm from "@/views/admin/business-accounts/components/AdminBusinessAccountForm.vue";
import AdminBusinessAccountPositionsCard from "@/views/admin/business-accounts/components/AdminBusinessAccountPositionsCard.vue";
import AdminManagersTable from "@/views/admin/managers/components/AdminManagersTable.vue";

const { t } = useI18n();
const permissions = usePermissions();
const route = useRoute();

const loading = ref(false);
const editDialog = ref(false);
const error = ref("");
const businessAccount = ref<BusinessAccountInfo | null>(null);

const summaryItems = computed<AdminDetailSummaryItem[]>(() => {
  if (!businessAccount.value) {
    return [];
  }

  return [
    { label: t("\u041D\u0430\u0438\u043C\u0435\u043D\u043E\u0432\u0430\u043D\u0438\u0435"), value: businessAccount.value.name },
    {
      label: t("\u0410\u0440\u0445\u0438\u0432"),
      value: businessAccount.value.archived ? t("\u0414\u0430") : t("\u041D\u0435\u0442"),
      valueClass: businessAccount.value.archived ? "text-decoration-line-through" : undefined,
    },
  ];
});

async function load(): Promise<void> {
  const businessAccountId = Number(route.params.businessAccountId);
  if (!businessAccountId) {
    error.value = t("\u041E\u0442\u0441\u0443\u0442\u0441\u0442\u0432\u0443\u044E\u0442 \u0434\u0430\u043D\u043D\u044B\u0435");
    return;
  }

  loading.value = true;
  error.value = "";
  try {
    businessAccount.value = await fetchAdminBusinessAccount(businessAccountId);
  } catch (loadError) {
    error.value = errorUtils.shortMessage(loadError);
    businessAccount.value = null;
  } finally {
    loading.value = false;
  }
}

function onSaved(): void {
  editDialog.value = false;
  void load();
}

void load();
</script>

<style scoped>
.admin-detail-section {
  min-height: 100%;
}
</style>
