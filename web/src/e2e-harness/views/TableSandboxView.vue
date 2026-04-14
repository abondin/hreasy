<template>
  <TableFirstPageLayout test-id="table-sandbox-view">
    <div class="d-flex flex-column h-100 min-h-0">
      <div class="d-flex align-center justify-space-between ga-4 flex-shrink-0">
        <v-tabs
          v-model="activeTab"
          color="primary"
          data-testid="table-sandbox-tabs"
        >
          <v-tab value="directory" data-testid="table-sandbox-tab-directory">
            Directory
          </v-tab>
          <v-tab value="archive" data-testid="table-sandbox-tab-archive">
            Archive
          </v-tab>
        </v-tabs>

        <v-btn
          variant="outlined"
          size="small"
          data-testid="table-sandbox-go-echo"
          :to="{ name: 'table-sandbox-echo' }"
        >
          Open Echo
        </v-btn>
      </div>

      <v-window v-model="activeTab" class="flex-grow-1 min-h-0">
        <v-window-item value="directory" class="h-100">
          <div class="h-100 pt-4">
            <TablePageCard test-id="table-sandbox-directory-card">
              <HREasyTableBase
                data-testid="table-sandbox-directory-table"
                table-class="table-sandbox-directory-table"
                :headers="directoryHeaders"
                :items="filteredDirectoryRows"
                :height="'fill'"
                :fixed-header="true"
                density="compact"
                :loading="loading"
                :loading-text="'Р—Р°РіСЂСѓР·РєР° РґР°РЅРЅС‹С…'"
                :no-data-text="'РќРµС‚ РґР°РЅРЅС‹С…'"
                :hover="true"
              >
                <template #filters>
                  <AdaptiveFilterBar
                    :items="filterBarItems"
                    :has-left-actions="true"
                    :has-right-actions="true"
                  >
                    <template #left-actions>
                      <v-btn size="small" variant="text">
                        Left Action
                      </v-btn>
                    </template>

                    <template #filter-search>
                      <v-text-field
                        v-model="search"
                        data-testid="table-sandbox-filter-search"
                        label="Search"
                        prepend-inner-icon="mdi-magnify"
                        variant="outlined"
                        density="compact"
                        clearable
                        hide-details
                      />
                    </template>

                    <template #filter-department>
                      <v-autocomplete
                        v-model="departments"
                        data-testid="table-sandbox-filter-department"
                        :items="departmentOptions"
                        label="Department"
                        variant="outlined"
                        density="compact"
                        clearable
                        hide-details
                        multiple
                        item-title="title"
                        item-value="value"
                      />
                    </template>

                    <template #filter-status>
                      <v-autocomplete
                        v-model="statuses"
                        data-testid="table-sandbox-filter-status"
                        :items="statusOptions"
                        label="Status"
                        variant="outlined"
                        density="compact"
                        clearable
                        hide-details
                        multiple
                        item-title="title"
                        item-value="value"
                      />
                    </template>

                    <template #filter-project>
                      <v-autocomplete
                        v-model="projects"
                        data-testid="table-sandbox-filter-project"
                        :items="projectOptions"
                        label="Project"
                        variant="outlined"
                        density="compact"
                        clearable
                        hide-details
                        multiple
                        item-title="title"
                        item-value="value"
                      />
                    </template>

                    <template #right-actions>
                      <v-btn
                        size="small"
                        variant="text"
                        data-testid="table-sandbox-toggle-banner"
                        @click="bannerVisible = !bannerVisible"
                      >
                        Toggle Banner
                      </v-btn>
                      <v-btn
                        size="small"
                        variant="text"
                        data-testid="table-sandbox-reload"
                        @click="reloadAsync"
                      >
                        Reload
                      </v-btn>
                    </template>
                  </AdaptiveFilterBar>
                </template>

                <template #before-table>
                  <v-expand-transition>
                    <v-alert
                      v-if="bannerVisible"
                      data-testid="table-sandbox-banner"
                      class="mb-3"
                      type="info"
                      variant="tonal"
                      density="compact"
                    >
                      Sandbox banner changes the DOM above the table and must not break height calculation.
                    </v-alert>
                  </v-expand-transition>
                </template>
              </HREasyTableBase>
            </TablePageCard>
          </div>
        </v-window-item>

        <v-window-item value="archive" class="h-100">
          <div class="h-100 pt-4">
            <TablePageCard test-id="table-sandbox-archive-card">
              <HREasyTableBase
                data-testid="table-sandbox-archive-table"
                table-class="table-sandbox-archive-table"
                :headers="archiveHeaders"
                :items="archiveRows"
                :height="'fill'"
                :fixed-header="true"
                density="compact"
                :loading="loading"
                :loading-text="'Р—Р°РіСЂСѓР·РєР° РґР°РЅРЅС‹С…'"
                :no-data-text="'РќРµС‚ РґР°РЅРЅС‹С…'"
                :hover="true"
              />
            </TablePageCard>
          </div>
        </v-window-item>
      </v-window>
    </div>
  </TableFirstPageLayout>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from "vue";
import type { VDataTable } from "vuetify/components";
import AdaptiveFilterBar, { type AdaptiveFilterBarItem } from "@/components/shared/AdaptiveFilterBar.vue";
import HREasyTableBase from "@/components/shared/HREasyTableBase.vue";
import TableFirstPageLayout from "@/components/shared/TableFirstPageLayout.vue";
import TablePageCard from "@/components/shared/TablePageCard.vue";

defineOptions({
  name: "TableSandboxView",
});

type DataTableHeader = VDataTable["$props"]["headers"];
type SandboxStatus = "Active" | "Paused" | "Draft";
type SandboxRow = {
  id: number;
  name: string;
  department: string;
  project: string;
  status: SandboxStatus;
  owner: string;
};

const activeTab = ref<"directory" | "archive">("directory");
const loading = ref(true);
const bannerVisible = ref(true);
const search = ref("");
const departments = ref<string[]>([]);
const statuses = ref<SandboxStatus[]>([]);
const projects = ref<string[]>([]);
const directoryRows = ref<SandboxRow[]>([]);
const archiveRows = ref<SandboxRow[]>([]);

let reloadTimer: number | null = null;
let loadVersion = 0;

const directoryHeaders: DataTableHeader = [
  { title: "Name", key: "name", width: "260px" },
  { title: "Department", key: "department", width: "240px" },
  { title: "Project", key: "project", width: "240px" },
  { title: "Status", key: "status", width: "160px" },
  { title: "Owner", key: "owner", width: "220px" },
];

const archiveHeaders: DataTableHeader = [
  { title: "Archived Name", key: "name", width: "280px" },
  { title: "Department", key: "department", width: "240px" },
  { title: "Status", key: "status", width: "180px" },
  { title: "Owner", key: "owner", width: "220px" },
];

const departmentOptions = computed(() =>
  uniqueOptions(directoryRows.value.map((row) => row.department)),
);
const statusOptions = computed(() =>
  uniqueOptions(directoryRows.value.map((row) => row.status)),
);
const projectOptions = computed(() =>
  uniqueOptions(directoryRows.value.map((row) => row.project)),
);

const filterBarItems = computed<AdaptiveFilterBarItem[]>(() => [
  { id: "search", minWidth: 360, active: search.value.trim().length > 0, grow: true },
  { id: "department", minWidth: 300, active: departments.value.length > 0 },
  { id: "status", minWidth: 260, active: statuses.value.length > 0 },
  { id: "project", minWidth: 320, active: projects.value.length > 0 },
]);

const filteredDirectoryRows = computed(() => {
  const normalizedSearch = search.value.trim().toLowerCase();

  return directoryRows.value.filter((row) => {
    if (departments.value.length > 0 && !departments.value.includes(row.department)) {
      return false;
    }
    if (statuses.value.length > 0 && !statuses.value.includes(row.status)) {
      return false;
    }
    if (projects.value.length > 0 && !projects.value.includes(row.project)) {
      return false;
    }

    if (!normalizedSearch) {
      return true;
    }

    const haystack = `${row.name} ${row.department} ${row.project} ${row.status} ${row.owner}`.toLowerCase();
    return haystack.includes(normalizedSearch);
  });
});

onMounted(() => {
  scheduleLoad();
});

onBeforeUnmount(() => {
  clearReloadTimer();
});

function reloadAsync() {
  scheduleLoad();
}

function scheduleLoad() {
  clearReloadTimer();
  loading.value = true;
  loadVersion += 1;
  const currentVersion = loadVersion;

  reloadTimer = window.setTimeout(() => {
    if (currentVersion !== loadVersion) {
      return;
    }

    directoryRows.value = buildRows("Directory", 180);
    archiveRows.value = buildRows("Archive", 120);
    loading.value = false;
    reloadTimer = null;
  }, 180);
}

function clearReloadTimer() {
  if (reloadTimer != null) {
    window.clearTimeout(reloadTimer);
    reloadTimer = null;
  }
}

function uniqueOptions(values: string[]) {
  return Array.from(new Set(values)).map((value) => ({ title: value, value }));
}

function buildRows(prefix: string, count: number): SandboxRow[] {
  const statuses: SandboxStatus[] = ["Active", "Paused", "Draft"];
  return Array.from({ length: count }, (_, index) => {
    const id = index + 1;
    return {
      id,
      name: `${prefix} Row ${id}`,
      department: `Department ${(id % 7) + 1}`,
      project: `Project ${(id % 9) + 1}`,
      status: statuses[id % statuses.length]!,
      owner: `Owner ${(id % 11) + 1}`,
    };
  });
}
</script>
