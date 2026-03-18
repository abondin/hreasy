<template>
  <v-container class="py-6" data-testid="admin-business-account-details-view">
    <div class="mx-auto" style="max-width: 1360px;">
      <v-alert
        v-if="error"
        type="error"
        variant="tonal"
        border="start"
        class="mb-4"
      >
        {{ error }}
      </v-alert>

      <v-card v-if="businessAccount" data-testid="admin-business-account-details-card">
        <v-card-title class="d-flex align-center ga-2 flex-wrap px-6 pt-6 pb-2">
          <v-btn
            icon="mdi-arrow-left"
            variant="text"
            :to="{ name: 'admin-business-accounts' }"
          />
          <v-btn icon="mdi-refresh" variant="text" :loading="loading" @click="load" />
          <span :class="{ 'text-decoration-line-through': businessAccount.archived }">
            {{ businessAccount.name }}
          </span>
          <v-spacer />
          <v-btn
            icon="mdi-pencil"
            color="primary"
            variant="text"
            :disabled="loading"
            data-testid="admin-business-account-edit"
            @click="editDialog = true"
          />
        </v-card-title>

        <v-card-text class="px-6 pb-6 pt-0">
          <v-row align="start" class="ga-4">
            <v-col cols="12" lg="auto">
              <v-card variant="outlined" min-width="320">
                <v-list density="comfortable">
                  <v-list-item>
                    <v-list-item-title>{{ t("\u041d\u0430\u0438\u043c\u0435\u043d\u043e\u0432\u0430\u043d\u0438\u0435") }}</v-list-item-title>
                    <v-list-item-subtitle>{{ businessAccount.name }}</v-list-item-subtitle>
                  </v-list-item>
                  <v-list-item>
                    <v-list-item-title>{{ t("\u0410\u0440\u0445\u0438\u0432") }}</v-list-item-title>
                    <v-list-item-subtitle>
                      {{ businessAccount.archived ? t("\u0414\u0430") : t("\u041d\u0435\u0442") }}
                    </v-list-item-subtitle>
                  </v-list-item>
                </v-list>
              </v-card>
            </v-col>

            <v-col cols="12" lg>
              <v-card variant="outlined" class="h-100">
                <v-card-title class="text-subtitle-1">
                  {{ t("\u041e\u043f\u0438\u0441\u0430\u043d\u0438\u0435") }}
                </v-card-title>
                <v-card-text>
                  <v-alert
                    v-if="!businessAccount.description"
                    type="info"
                    variant="tonal"
                    border="start"
                  >
                    {{ t("\u041d\u0435 \u0437\u0430\u0434\u0430\u043d") }}
                  </v-alert>
                  <div v-else>{{ businessAccount.description }}</div>
                </v-card-text>
              </v-card>
            </v-col>
          </v-row>
        </v-card-text>
      </v-card>

      <admin-managers-table
        v-if="businessAccount"
        class="mt-4"
        :selected-object="{ id: businessAccount.id, type: 'business_account' }"
        :title="t('\u041c\u0435\u043d\u0435\u0434\u0436\u0435\u0440\u044b \u0431\u0438\u0437\u043d\u0435\u0441 \u0430\u043a\u043a\u0430\u0443\u043d\u0442\u0430')"
        :editable="permissions.canAdminManagers()"
        mode="compact"
        test-id="admin-business-account-managers"
      />

      <admin-business-account-positions-card
        v-if="businessAccount"
        class="mt-4"
        :business-account-id="businessAccount.id"
      />

      <v-dialog v-model="editDialog" persistent max-width="720">
        <admin-business-account-form
          :input="businessAccount"
          @close="editDialog = false"
          @saved="onSaved"
        />
      </v-dialog>
    </div>
  </v-container>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { useRoute } from "vue-router";
import { useI18n } from "vue-i18n";
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

async function load(): Promise<void> {
  const businessAccountId = Number(route.params.businessAccountId);
  if (!businessAccountId) {
    error.value = t("\u041e\u0442\u0441\u0443\u0442\u0441\u0442\u0432\u0443\u044e\u0442 \u0434\u0430\u043d\u043d\u044b\u0435");
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
