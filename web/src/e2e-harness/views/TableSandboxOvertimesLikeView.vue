<template>
  <TableFirstPageLayout test-id="table-sandbox-overtimes-view">
    <TablePageCard test-id="table-sandbox-overtimes-card">
      <AdaptiveFilterBar :items="filterBarItems" :has-right-actions="true">
        <template #left-actions>
          <div class="d-flex align-center ga-2">
            <TableToolbarActions show-refresh refresh-label="Refresh Data" />
            <PeriodSwitcherControl
              label="2026-04"
              :is-current="true"
              variant="outlined"
              :width="272"
              :disabled="false"
              :period-closed="false"
              label-test-id="table-sandbox-overtimes-period-label"
              next-test-id="table-sandbox-overtimes-next-period"
            />
            <TableToolbarActions show-export export-label="Export to Excel" />
          </div>
        </template>

        <template #filter-search>
          <v-text-field
            v-model="search"
            data-testid="table-sandbox-overtimes-filter-search"
            density="compact"
            clearable
            label="Employee Name"
            prepend-inner-icon="mdi-magnify"
            variant="outlined"
            hide-details
          />
        </template>

        <template #filter-department>
          <v-autocomplete
            v-model="selectedDepartments"
            data-testid="table-sandbox-overtimes-filter-department"
            density="compact"
            clearable
            multiple
            :items="departmentOptions"
            item-title="name"
            item-value="id"
            label="Department"
            variant="outlined"
            hide-details
          >
            <template #selection="{ item, index }">
              <CollapsedSelectionContent
                :index="index"
                :total="selectedDepartments.length"
                :label="getSelectionLabel(item)"
              />
            </template>
          </v-autocomplete>
        </template>

        <template #filter-business-account>
          <v-autocomplete
            v-model="selectedBusinessAccounts"
            data-testid="table-sandbox-overtimes-filter-business-account"
            density="compact"
            clearable
            multiple
            :items="businessAccountOptions"
            item-title="name"
            item-value="id"
            label="Business Account"
            variant="outlined"
            hide-details
          >
            <template #selection="{ item, index }">
              <CollapsedSelectionContent
                :index="index"
                :total="selectedBusinessAccounts.length"
                :label="getSelectionLabel(item)"
              />
            </template>
          </v-autocomplete>
        </template>

        <template #filter-current-project>
          <v-autocomplete
            v-model="selectedCurrentProjects"
            data-testid="table-sandbox-overtimes-filter-current-project"
            density="compact"
            clearable
            multiple
            :items="projectOptions"
            item-title="name"
            item-value="id"
            label="Current Project"
            variant="outlined"
            hide-details
          >
            <template #selection="{ item, index }">
              <CollapsedSelectionContent
                :index="index"
                :total="selectedCurrentProjects.length"
                :label="getSelectionLabel(item)"
              />
            </template>
          </v-autocomplete>
        </template>

        <template #filter-overtime-project>
          <v-autocomplete
            v-model="selectedOvertimeProjects"
            data-testid="table-sandbox-overtimes-filter-overtime-project"
            density="compact"
            clearable
            multiple
            :items="projectOptions"
            item-title="name"
            item-value="id"
            label="Overtime Project"
            variant="outlined"
            hide-details
          >
            <template #selection="{ item, index }">
              <CollapsedSelectionContent
                :index="index"
                :total="selectedOvertimeProjects.length"
                :label="getSelectionLabel(item)"
              />
            </template>
          </v-autocomplete>
        </template>

        <template #filter-show-empty>
          <v-checkbox
            v-model="showEmpty"
            data-testid="table-sandbox-overtimes-filter-show-empty"
            density="compact"
            label="Employees without overtime"
            hide-details
          />
        </template>

        <template #right-actions>
          <div class="d-flex align-center ga-2">
            <v-btn
              icon="mdi-lock"
              variant="text"
              data-testid="table-sandbox-overtimes-period-toggle"
            />
            <v-btn
              color="primary"
              variant="outlined"
              size="small"
              data-testid="table-sandbox-overtimes-go-vacations"
              :to="{ name: 'table-sandbox-vacations-like' }"
            >
              Open Vacations
            </v-btn>
          </div>
        </template>
      </AdaptiveFilterBar>

      <div class="mb-3 mt-3">
        Total (filtered): 412 hours
      </div>

      <HREasyTableBase
        data-testid="overtimes-table"
        :headers="headers"
        :items="filteredRows"
        fixed-header
        :loading="false"
        loading-text="Loading data"
        no-data-text="No data"
        :sort-by="[{ key: 'totalHours', order: 'desc' }]"
        density="compact"
        hover
        height="fill"
      >
        <template #[`item.commonApprovalStatus`]="{ item }">
          <v-chip v-if="item.commonApprovalStatus === 'DECLINED'" variant="outlined">
            Declined
          </v-chip>
          <v-chip v-else-if="item.commonApprovalStatus === 'APPROVED_NO_DECLINED'" variant="outlined">
            Approved
          </v-chip>
          <v-chip v-else-if="item.commonApprovalStatus === 'APPROVED_OUTDATED'" variant="outlined">
            Updated
          </v-chip>
          <span v-else />
        </template>
      </HREasyTableBase>
    </TablePageCard>
  </TableFirstPageLayout>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import type { VDataTable } from "vuetify/components";
import AdaptiveFilterBar from "@/components/shared/AdaptiveFilterBar.vue";
import CollapsedSelectionContent from "@/components/shared/CollapsedSelectionContent.vue";
import HREasyTableBase from "@/components/shared/HREasyTableBase.vue";
import PeriodSwitcherControl from "@/components/shared/PeriodSwitcherControl.vue";
import TableFirstPageLayout from "@/components/shared/TableFirstPageLayout.vue";
import TablePageCard from "@/components/shared/TablePageCard.vue";
import TableToolbarActions from "@/components/shared/TableToolbarActions.vue";

defineOptions({
  name: "TableSandboxOvertimesLikeView",
});

type DataTableHeader = VDataTable["$props"]["headers"];
type OptionItem = { id: number; name: string };

const headers: DataTableHeader = [
  { title: "Employee", key: "name" },
  { title: "Department", key: "department" },
  { title: "Business Account", key: "businessAccount" },
  { title: "Current Project", key: "currentProject" },
  { title: "Overtime Project", key: "overtimeProject" },
  { title: "Total Hours", key: "totalHours" },
  { title: "Approval Status", key: "commonApprovalStatus" },
];

const search = ref("");
const selectedDepartments = ref<number[]>([]);
const selectedBusinessAccounts = ref<number[]>([]);
const selectedCurrentProjects = ref<number[]>([]);
const selectedOvertimeProjects = ref<number[]>([]);
const showEmpty = ref(false);

const departmentOptions: OptionItem[] = [
  { id: 1, name: "Main Office" },
  { id: 2, name: "Software Development Department" },
  { id: 3, name: "QA Department" },
  { id: 4, name: "Integration Department" },
];

const businessAccountOptions: OptionItem[] = [
  { id: 1, name: "Administrative Services" },
  { id: 2, name: "STM RND" },
  { id: 3, name: "GNIVC" },
  { id: 4, name: "MTS" },
];

const projectOptions: OptionItem[] = [
  { id: 1, name: "MAPM KKT" },
  { id: 2, name: "Administrative Services Platform" },
  { id: 3, name: "Pi.One M2M IoT Dev" },
  { id: 4, name: "Digital Currency Mining Platform" },
  { id: 5, name: "AISN3.5" },
  { id: 6, name: "Document Flow Enterprise" },
];

const filterBarItems = computed(() => [
  { id: "search", minWidth: 380, active: search.value.trim().length > 0, grow: true },
  { id: "department", minWidth: 320, active: selectedDepartments.value.length > 0 },
  {
    id: "business-account",
    minWidth: 320,
    active: selectedBusinessAccounts.value.length > 0,
  },
  {
    id: "current-project",
    minWidth: 320,
    active: selectedCurrentProjects.value.length > 0,
  },
  {
    id: "overtime-project",
    minWidth: 320,
    active: selectedOvertimeProjects.value.length > 0,
  },
  { id: "show-empty", minWidth: 260, active: showEmpty.value },
]);

const allRows = Array.from({ length: 360 }, (_, index) => {
  const id = index + 1;
  const department = departmentOptions[index % departmentOptions.length]!;
  const businessAccount = businessAccountOptions[index % businessAccountOptions.length]!;
  const currentProject = projectOptions[index % projectOptions.length]!;
  const overtimeProject = projectOptions[(index + 2) % projectOptions.length]!;

  return {
    id,
    name: `Employee ${id}`,
    department: department.name,
    businessAccount: businessAccount.name,
    currentProject: currentProject.name,
    overtimeProject: overtimeProject.name,
    totalHours: (index % 30) + 1,
    commonApprovalStatus: ["DECLINED", "APPROVED_NO_DECLINED", "APPROVED_OUTDATED", ""][index % 4]!,
  };
});

const filteredRows = computed(() => {
  const normalized = search.value.trim().toLowerCase();

  return allRows.filter((row, index) => {
    const department = departmentOptions[index % departmentOptions.length]!;
    const businessAccount = businessAccountOptions[index % businessAccountOptions.length]!;
    const currentProject = projectOptions[index % projectOptions.length]!;
    const overtimeProject = projectOptions[(index + 2) % projectOptions.length]!;

    if (selectedDepartments.value.length > 0 && !selectedDepartments.value.includes(department.id)) {
      return false;
    }
    if (
      selectedBusinessAccounts.value.length > 0 &&
      !selectedBusinessAccounts.value.includes(businessAccount.id)
    ) {
      return false;
    }
    if (
      selectedCurrentProjects.value.length > 0 &&
      !selectedCurrentProjects.value.includes(currentProject.id)
    ) {
      return false;
    }
    if (
      selectedOvertimeProjects.value.length > 0 &&
      !selectedOvertimeProjects.value.includes(overtimeProject.id)
    ) {
      return false;
    }
    if (!showEmpty.value && row.totalHours === 0) {
      return false;
    }
    if (!normalized) {
      return true;
    }
    return `${row.name} ${row.department} ${row.businessAccount} ${row.currentProject} ${row.overtimeProject}`
      .toLowerCase()
      .includes(normalized);
  });
});

function getSelectionLabel(item: unknown): string {
  if (typeof item === "string") {
    return item;
  }

  if (item && typeof item === "object") {
    const raw = "raw" in item ? item.raw : item;
    if (raw && typeof raw === "object" && "name" in raw && typeof raw.name === "string") {
      return raw.name;
    }
  }

  return String(item ?? "");
}
</script>
