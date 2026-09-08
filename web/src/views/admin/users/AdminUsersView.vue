<template>
  <TableFirstPageLayout test-id="admin-users-view" @activated="load">
    <TablePageCard test-id="admin-users-card">
      <HREasyTableBase
        table-class="admin-users-table text-truncate"
        :headers="headers"
        :items="filteredItems"
        height="fill"
        :fixed-header="true"
        density="compact"
        :loading="loading"
        :loading-text="t('Загрузка_данных')"
        :no-data-text="t('Отсутствуют данные')"
        :row-props="rowProps"
        hover
        @click:row="onClickRow"
      >
        <template #filters>
            <AdaptiveFilterBar :items="filterBarItems" :has-left-actions="true" :has-right-actions="false">
              <template #left-actions>
                <table-toolbar-actions
                  :disabled="loading"
                  show-refresh
                  :refresh-label="t('Обновить данные')"
                  @refresh="load"
                />
              </template>

              <template #filter-search>
                <SearchTextField
                  v-model="search"
                  v-model:settings="searchSettings"
                  :label="t('Поиск')"
                />
              </template>
            </AdaptiveFilterBar>
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
              :key="`manual-ba-${baId}`"
              size="small"
              variant="outlined"
            >
              {{ getById(businessAccounts, baId) }}
            </v-chip>
            <v-chip
              v-for="baId in derivedOnly(item.managedBas, item.accessibleBas)"
              :key="`managed-ba-${baId}`"
              size="small"
              color="grey"
              variant="tonal"
            >
              {{ getById(businessAccounts, baId) }}
            </v-chip>
          </div>
        </template>

        <template #[`item.accessibleProjects`]="{ item }">
          <div class="d-flex flex-wrap ga-1">
            <v-chip
              v-for="projectId in item.accessibleProjects"
              :key="`manual-project-${projectId}`"
              size="small"
              variant="outlined"
            >
              {{ getById(projects, projectId) }}
            </v-chip>
            <v-chip
              v-for="projectId in derivedOnly(item.managedProjects, item.accessibleProjects)"
              :key="`managed-project-${projectId}`"
              size="small"
              color="grey"
              variant="tonal"
            >
              {{ getById(projects, projectId) }}
            </v-chip>
          </div>
        </template>
      </HREasyTableBase>
    </TablePageCard>

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
  </TableFirstPageLayout>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import TablePageCard from "@/components/shared/TablePageCard.vue";
import TableFirstPageLayout from "@/components/shared/TableFirstPageLayout.vue";
import { useI18n } from "vue-i18n";
import AdaptiveFilterBar from "@/components/shared/AdaptiveFilterBar.vue";
import HREasyTableBase from "@/components/shared/HREasyTableBase.vue";
import TableToolbarActions from "@/components/shared/TableToolbarActions.vue";
import SearchTextField from "@/components/shared/SearchTextField.vue";
import { extractDataTableRow } from "@/lib/data-table";
import { errorUtils } from "@/lib/errors";
import { createSearchSettings, matchesSearch } from "@/lib/search";
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
const searchSettings = ref(createSearchSettings());
const items = ref<UserSecurityInfo[]>([]);
const current = ref<UserSecurityInfo | null>(null);
const departments = ref<DictItem[]>([]);
const projects = ref<ProjectDictDto[]>([]);
const businessAccounts = ref<DictItem[]>([]);
const roles = ref<RoleDict[]>(getAdminUserRolesDictionary(t));

const activeDepartments = computed(() => departments.value.filter((item) => item.active !== false));
const activeProjects = computed(() => projects.value.filter((item) => item.active !== false));
const headers = computed(() => [
  { title: t("ФИО"), key: "employee.name", width: "240px" },
  { title: t("Роли"), key: "roles", width: "260px" },
  { title: t("Доступные отделы"), key: "accessibleDepartments", width: "320px" },
  { title: t("Доступные бизнес аккаунты"), key: "accessibleBas", width: "320px" },
  { title: t("Доступные проекты"), key: "accessibleProjects", width: "320px" },
]);

const filterBarItems = computed(() => [
  { id: "search", minWidth: 380, active: search.value.trim().length > 0, grow: true },
]);

const filteredItems = computed(() => {
  return items.value.filter((item) => {
    const projectIds = new Set([...item.accessibleProjects, ...item.managedProjects]);
    const baIds = new Set([...item.accessibleBas, ...item.managedBas]);
    return matchesSearch(search.value, [
      item.employee.name,
      item.email,
      ...projects.value.filter((project) => projectIds.has(project.id)).map((project) => project.name),
      ...departments.value.filter((department) =>
        item.accessibleDepartments.includes(department.id)
        || item.managedDepartments.includes(department.id),
      ).map((department) => department.name),
      ...businessAccounts.value.filter((ba) => baIds.has(ba.id)).map((ba) => ba.name),
      ...roles.value.filter((role) => item.roles.includes(role.id)).map((role) => role.name),
    ], searchSettings.value);
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
  return found ? found.name : `${t("Не найден: ")}${id}`;
}

function getRoleName(roleId: string): string {
  return roles.value.find((role) => role.id === roleId)?.name ?? roleId;
}

function derivedOnly(derivedIds: number[] = [], manualIds: number[] = []): number[] {
  return derivedIds.filter((id) => !manualIds.includes(id));
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
