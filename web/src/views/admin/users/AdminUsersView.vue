<template>
  <v-container class="py-6" data-testid="admin-users-view">
    <v-card data-testid="admin-users-card">
      <HREasyTableBase
        table-class="admin-users-table text-truncate"
        :headers="headers"
        :items="filteredItems"
        height="70vh"
        :fixed-header="true"
        density="compact"
        :loading="loading"
        :loading-text="t('\u0417\u0430\u0433\u0440\u0443\u0437\u043a\u0430_\u0434\u0430\u043d\u043d\u044b\u0445')"
        :no-data-text="t('\u041e\u0442\u0441\u0443\u0442\u0441\u0442\u0432\u0443\u044e\u0442 \u0434\u0430\u043d\u043d\u044b\u0435')"
        :row-props="rowProps"
        hover
        @click:row="onClickRow"
      >
        <template #filters>
          <v-card-title class="d-flex ga-2 align-center flex-wrap">
            <v-btn
              icon="mdi-refresh"
              variant="text"
              :loading="loading"
              data-testid="admin-users-refresh"
              @click="load"
            />
          </v-card-title>

          <v-card-text class="pb-0">
            <v-row density="comfortable">
              <v-col cols="12" lg="4">
                <v-text-field
                  v-model="search"
                  :label="t('\u041f\u043e\u0438\u0441\u043a')"
                  append-inner-icon="mdi-magnify"
                  variant="outlined"
                  density="compact"
                  hide-details
                  clearable
                />
              </v-col>
            </v-row>
          </v-card-text>
        </template>

        <template #before-table>
          <v-alert v-if="error" type="error" variant="tonal" border="start" class="mb-3">
            {{ error }}
          </v-alert>
        </template>

        <template #[`item.employee.name`]="{ item }">
          <span>{{ item.employee.name }}</span>
        </template>

        <template #[`item.roles`]="{ item }">
          <div class="d-flex flex-wrap ga-1">
            <v-chip
              v-for="roleId in item.roles"
              :key="roleId"
              size="small"
              variant="outlined"
            >
              {{ getRoleName(roleId) }}
            </v-chip>
          </div>
        </template>

        <template #[`item.accessibleDepartments`]="{ item }">
          <div class="d-flex flex-wrap ga-1">
            <v-chip
              v-for="departmentId in item.accessibleDepartments"
              :key="departmentId"
              size="small"
              variant="outlined"
            >
              {{ getById(departments, departmentId) }}
            </v-chip>
          </div>
        </template>

        <template #[`item.accessibleBas`]="{ item }">
          <div class="d-flex flex-wrap ga-1">
            <v-chip
              v-for="baId in item.accessibleBas"
              :key="baId"
              size="small"
              variant="outlined"
            >
              {{ getById(businessAccounts, baId) }}
            </v-chip>
          </div>
        </template>

        <template #[`item.accessibleProjects`]="{ item }">
          <div class="d-flex flex-wrap ga-1">
            <v-chip
              v-for="projectId in item.accessibleProjects"
              :key="projectId"
              size="small"
              variant="outlined"
            >
              {{ getById(projects, projectId) }}
            </v-chip>
          </div>
        </template>
      </HREasyTableBase>
    </v-card>

    <v-dialog v-model="dialog" persistent max-width="860">
      <admin-user-roles-form
        :input="current"
        :all-projects="activeProjects"
        :all-departments="activeDepartments"
        :all-bas="businessAccounts"
        :all-roles="roles"
        @close="dialog = false"
        @saved="onSaved"
      />
    </v-dialog>
  </v-container>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import { useI18n } from "vue-i18n";
import HREasyTableBase from "@/components/shared/HREasyTableBase.vue";
import { extractDataTableRow } from "@/lib/data-table";
import { errorUtils } from "@/lib/errors";
import type { DictItem } from "@/services/dict.service";
import {
  fetchBusinessAccounts,
  fetchDepartments,
} from "@/services/dict.service";
import { fetchProjects, type ProjectDictDto } from "@/services/projects.service";
import {
  getAdminUserRolesDictionary,
  listAdminUsers,
  type RoleDict,
  type UserSecurityInfo,
} from "@/services/admin/admin-user.service";
import AdminUserRolesForm from "@/views/admin/users/components/AdminUserRolesForm.vue";

const { t } = useI18n();

const loading = ref(false);
const dialog = ref(false);
const error = ref("");
const search = ref("");
const items = ref<UserSecurityInfo[]>([]);
const current = ref<UserSecurityInfo | null>(null);
const departments = ref<DictItem[]>([]);
const projects = ref<ProjectDictDto[]>([]);
const businessAccounts = ref<DictItem[]>([]);
const roles = ref<RoleDict[]>(getAdminUserRolesDictionary(t));

const activeDepartments = computed(() => departments.value.filter((item) => item.active !== false));
const activeProjects = computed(() => projects.value.filter((item) => item.active !== false));
const headers = computed(() => [
  { title: t("\u0424\u0418\u041e"), key: "employee.name", width: "240px" },
  { title: t("\u0420\u043e\u043b\u0438"), key: "roles", width: "260px" },
  { title: t("\u0414\u043e\u0441\u0442\u0443\u043f\u043d\u044b\u0435 \u043e\u0442\u0434\u0435\u043b\u044b"), key: "accessibleDepartments", width: "320px" },
  { title: t("\u0414\u043e\u0441\u0442\u0443\u043f\u043d\u044b\u0435 \u0431\u0438\u0437\u043d\u0435\u0441 \u0430\u043a\u043a\u0430\u0443\u043d\u0442\u044b"), key: "accessibleBas", width: "320px" },
  { title: t("\u0414\u043e\u0441\u0442\u0443\u043f\u043d\u044b\u0435 \u043f\u0440\u043e\u0435\u043a\u0442\u044b"), key: "accessibleProjects", width: "320px" },
]);

const filteredItems = computed(() => {
  const query = search.value.trim().toLowerCase();
  return items.value.filter((item) => {
    if (!query) {
      return true;
    }

    const matchingProjectIds = projects.value
      .filter((project) => project.name.toLowerCase().includes(query))
      .map((project) => project.id);
    const matchingDepartmentIds = departments.value
      .filter((department) => department.name.toLowerCase().includes(query))
      .map((department) => department.id);
    const matchingBaIds = businessAccounts.value
      .filter((ba) => ba.name.toLowerCase().includes(query))
      .map((ba) => ba.id);
    const matchingRoleIds = roles.value
      .filter((role) => role.name.toLowerCase().includes(query))
      .map((role) => role.id);

    return item.employee.name.toLowerCase().includes(query)
      || item.accessibleProjects.some((id) => matchingProjectIds.includes(id))
      || item.accessibleDepartments.some((id) => matchingDepartmentIds.includes(id))
      || item.accessibleBas.some((id) => matchingBaIds.includes(id))
      || item.roles.some((id) => matchingRoleIds.includes(id));
  });
});

function rowProps() {
  return { class: "cursor-pointer" };
}

function getById(
  source: Array<{ id: number; name: string }>,
  id?: number,
): string {
  if (!id) {
    return "-";
  }
  const found = source.find((item) => item.id === id);
  return found ? found.name : `${t("\u041d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d: ")}${id}`;
}

function getRoleName(roleId: string): string {
  return roles.value.find((role) => role.id === roleId)?.name ?? roleId;
}

async function load(): Promise<void> {
  loading.value = true;
  error.value = "";
  try {
    const [users, departmentItems, projectItems, baItems] = await Promise.all([
      listAdminUsers(),
      fetchDepartments(),
      fetchProjects(),
      fetchBusinessAccounts(),
    ]);
    items.value = users;
    departments.value = departmentItems;
    projects.value = projectItems;
    businessAccounts.value = baItems;
    roles.value = getAdminUserRolesDictionary(t);
  } catch (loadError) {
    error.value = errorUtils.shortMessage(loadError);
  } finally {
    loading.value = false;
  }
}

function onClickRow(_event: Event, row: unknown): void {
  const item = extractDataTableRow<UserSecurityInfo>(row);
  if (!item) {
    return;
  }
  current.value = item;
  dialog.value = true;
}

function onSaved(): void {
  dialog.value = false;
  void load();
}

void load();
</script>

<style scoped>
.admin-users-table :deep(tbody tr:hover) {
  cursor: pointer;
}
</style>
