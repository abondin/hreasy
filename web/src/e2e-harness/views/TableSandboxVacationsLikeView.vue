<template>
  <TableFirstPageLayout test-id="table-sandbox-vacations-view">
    <v-tabs v-model="selectedTab" class="flex-shrink-0" data-testid="table-sandbox-vacations-tabs">
      <v-tab :value="0" data-testid="table-sandbox-vacations-tab-list">
        All Vacations
      </v-tab>
      <v-tab :value="1" data-testid="table-sandbox-vacations-tab-summary">
        Summary
      </v-tab>
      <v-tab :value="2" data-testid="table-sandbox-vacations-tab-timeline">
        Timeline
      </v-tab>
    </v-tabs>

    <div class="table-sandbox-vacations-content mt-4 d-flex flex-column flex-grow-1 min-h-0">
      <TablePageCard test-id="table-sandbox-vacations-card">
        <AdaptiveFilterBar :items="filterBarItems" :has-right-actions="true">
          <template #left-actions>
            <v-btn-group divided variant="text">
              <v-btn
                data-testid="table-sandbox-vacations-refresh"
                icon="mdi-refresh"
                size="small"
                density="comfortable"
              />
              <v-btn
                data-testid="table-sandbox-vacations-export"
                icon="mdi-file-excel"
                size="small"
                density="comfortable"
              />
            </v-btn-group>
          </template>

          <template #filter-year>
            <v-select
              v-model="selectedYear"
              data-testid="table-sandbox-vacations-filter-year"
              :items="yearOptions"
              label="Year"
              variant="outlined"
              density="compact"
              hide-details
            />
          </template>

          <template #filter-date>
            <v-text-field
              v-model="selectedDateRangeLabel"
              data-testid="table-sandbox-vacations-filter-date"
              label="Vacation Start Date"
              variant="outlined"
              density="compact"
              readonly
              hide-details
            />
          </template>

          <template #filter-search>
            <v-text-field
              v-model="search"
              data-testid="table-sandbox-vacations-filter-search"
              label="Search"
              prepend-inner-icon="mdi-magnify"
              variant="outlined"
              density="compact"
              clearable
              hide-details
            />
          </template>

          <template #filter-status>
            <v-select
              v-model="selectedStatuses"
              data-testid="table-sandbox-vacations-filter-status"
              :items="statusOptions"
              item-title="title"
              item-value="value"
              label="Status"
              variant="outlined"
              density="compact"
              clearable
              multiple
              hide-details
            >
              <template #selection="{ item, index }">
                <CollapsedSelectionContent
                  :index="index"
                  :total="selectedStatuses.length"
                  :label="getSelectionLabel(item)"
                />
              </template>
            </v-select>
          </template>

          <template #filter-project>
            <v-autocomplete
              v-model="selectedProjects"
              data-testid="table-sandbox-vacations-filter-project"
              :items="projectOptions"
              item-title="name"
              item-value="id"
              label="Current Project"
              variant="outlined"
              density="compact"
              clearable
              multiple
              hide-details
            >
              <template #selection="{ item, index }">
                <CollapsedSelectionContent
                  :index="index"
                  :total="selectedProjects.length"
                  :label="getSelectionLabel(item)"
                />
              </template>
            </v-autocomplete>
          </template>

          <template #filter-department>
            <v-autocomplete
              v-model="selectedDepartments"
              data-testid="table-sandbox-vacations-filter-department"
              :items="departmentOptions"
              item-title="name"
              item-value="id"
              label="Department"
              variant="outlined"
              density="compact"
              clearable
              multiple
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
              data-testid="table-sandbox-vacations-filter-business-account"
              :items="businessAccountOptions"
              item-title="name"
              item-value="id"
              label="Business Account"
              variant="outlined"
              density="compact"
              clearable
              multiple
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

          <template #filter-role>
            <v-autocomplete
              v-model="selectedProjectRoles"
              data-testid="table-sandbox-vacations-filter-role"
              :items="projectRoleOptions"
              label="Project Role"
              variant="outlined"
              density="compact"
              clearable
              multiple
              hide-details
            >
              <template #selection="{ item, index }">
                <CollapsedSelectionContent
                  :index="index"
                  :total="selectedProjectRoles.length"
                  :label="getSelectionLabel(item)"
                />
              </template>
            </v-autocomplete>
          </template>

          <template #right-actions>
            <div class="d-flex align-center ga-2">
              <v-btn
                color="primary"
                variant="outlined"
                prepend-icon="mdi-plus"
                size="small"
                data-testid="table-sandbox-vacations-add"
              >
                Add Vacation
              </v-btn>
              <v-btn
                color="primary"
                variant="outlined"
                size="small"
                data-testid="table-sandbox-vacations-go-echo"
                :to="{ name: 'table-sandbox-overtimes-like' }"
              >
                Open Overtimes
              </v-btn>
            </div>
          </template>
        </AdaptiveFilterBar>

        <VacationsListTab
          v-if="selectedTab === 0"
          class="flex-grow-1 min-h-0"
          :loading="false"
          :headers="headers"
          :items="filteredRows"
          :on-row-click="onVacationRowClick"
          :copy-to-clipboard="copyToClipboard"
        />

        <VacationsSummaryTab
          v-else-if="selectedTab === 1"
          class="flex-grow-1 min-h-0"
          :loading="false"
          :headers="summaryHeaders"
          :items="summaryRows"
          :on-row-click="onSummaryRowClick"
        />

        <VacationsTimelineTab
          v-else
          class="flex-grow-1 min-h-0"
          :items="filteredRows"
          :year="selectedYear"
          @year-navigation="selectedYear = $event"
        />
      </TablePageCard>
    </div>
  </TableFirstPageLayout>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import type { VDataTable } from "vuetify/components";
import AdaptiveFilterBar from "@/components/shared/AdaptiveFilterBar.vue";
import CollapsedSelectionContent from "@/components/shared/CollapsedSelectionContent.vue";
import TableFirstPageLayout from "@/components/shared/TableFirstPageLayout.vue";
import TablePageCard from "@/components/shared/TablePageCard.vue";
import type { EmployeeVacationSummary } from "@/components/vacations/employeeVacationSummaryService";
import { employeeVacationSummaryMapper } from "@/components/vacations/employeeVacationSummaryService";
import VacationsListTab from "@/views/vacations/components/VacationsListTab.vue";
import VacationsSummaryTab from "@/views/vacations/components/VacationsSummaryTab.vue";
import VacationsTimelineTab from "@/views/vacations/components/VacationsTimelineTab.vue";
import type { Vacation, VacationStatus } from "@/services/vacation.service";

defineOptions({
  name: "TableSandboxVacationsLikeView",
});

type DataTableHeader = VDataTable["$props"]["headers"];
type OptionItem = { id: number; name: string };
type StatusOption = { value: VacationStatus; title: string };

const headers: DataTableHeader = [
  { title: "Employee", key: "employeeDisplayName" },
  { title: "Current Project", key: "employeeCurrentProject.name" },
  { title: "Project Role", key: "employeeCurrentProject.role" },
  { title: "Year", key: "year" },
  { title: "Start", key: "startDate" },
  { title: "End", key: "endDate" },
  { title: "Days", key: "daysNumber" },
  { title: "Status", key: "status" },
  { title: "Document", key: "documents" },
  { title: "Notes", key: "notes" },
];

const summaryHeaders: DataTableHeader = [
  { title: "Employee", key: "employeeDisplayName" },
  { title: "Current Project", key: "employeeCurrentProject.name" },
  { title: "Project Role", key: "employeeCurrentProject.role" },
  { title: "Year", key: "year" },
  { title: "Vacations", key: "vacationsNumber" },
  { title: "Total Days", key: "vacationsTotalDays" },
  { title: "Upcoming Vacation", key: "upcomingVacation" },
];

const yearOptions = [2025, 2026, 2027];
const selectedTab = ref(0);
const selectedYear = ref(2026);
const search = ref("");
const selectedStatuses = ref<VacationStatus[]>(["PLANNED", "TAKEN", "REQUESTED"]);
const selectedProjects = ref<number[]>([]);
const selectedDepartments = ref<number[]>([]);
const selectedBusinessAccounts = ref<number[]>([]);
const selectedProjectRoles = ref<string[]>([]);
const selectedDateRangeLabel = ref("from 2026-01-01 to 2026-12-31");

const statusOptions: StatusOption[] = [
  { value: "PLANNED", title: "Planned" },
  { value: "TAKEN", title: "Taken" },
  { value: "REQUESTED", title: "Requested" },
  { value: "COMPENSATION", title: "Compensation" },
  { value: "CANCELED", title: "Canceled" },
  { value: "REJECTED", title: "Rejected" },
];

const projectOptions: OptionItem[] = [
  { id: 1, name: "MAPM KKT" },
  { id: 2, name: "Administrative Services Platform" },
  { id: 3, name: "Pi.One M2M IoT Dev" },
  { id: 4, name: "Digital Currency Mining Platform" },
  { id: 5, name: "AISN3.5" },
  { id: 6, name: "Document Flow Enterprise" },
];

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

const projectRoleOptions = [
  "HR",
  "Backend Java",
  "Backend C#",
  "System Analyst",
  "Frontend React",
  "Teamlead Backend Java",
  "Analyst",
];

const filterBarItems = computed(() => [
  { id: "year", minWidth: 140, active: Boolean(selectedYear.value) },
  { id: "date", minWidth: 320, active: true },
  { id: "search", minWidth: 380, active: search.value.trim().length > 0, grow: true },
  { id: "status", minWidth: 260, active: selectedStatuses.value.length > 0 },
  { id: "project", minWidth: 240, active: selectedProjects.value.length > 0 },
  { id: "department", minWidth: 240, active: selectedDepartments.value.length > 0 },
  {
    id: "business-account",
    minWidth: 240,
    active: selectedBusinessAccounts.value.length > 0,
  },
  { id: "role", minWidth: 240, active: selectedProjectRoles.value.length > 0 },
]);

const lastNames = [
  "Абрамов",
  "Грошев",
  "Грязнов",
  "Губина",
  "Денисенко",
  "Ермаков",
  "Зуев",
  "Ильин",
  "Корнев",
  "Лавров",
];

const firstNames = [
  "Александр",
  "Валерия",
  "Кирилл",
  "Никита",
  "Анна",
  "Мария",
  "Дмитрий",
  "Ирина",
  "Сергей",
  "Юлия",
];

const middleNames = [
  "Андреевич",
  "Сергеевна",
  "Олегович",
  "Викторович",
  "Ильдаровна",
  "Павлович",
  "Игоревна",
  "Александрович",
  "Юрьевна",
  "Николаевич",
];

const allRows: Vacation[] = Array.from({ length: 960 }, (_, index) => {
  const id = index + 1;
  const year = 2026;
  const project = projectOptions[index % projectOptions.length]!;
  const department = departmentOptions[index % departmentOptions.length]!;
  const businessAccount = businessAccountOptions[index % businessAccountOptions.length]!;
  const role = projectRoleOptions[index % projectRoleOptions.length]!;
  const employeeName = `${lastNames[index % lastNames.length]} ${firstNames[index % firstNames.length]} ${middleNames[index % middleNames.length]}`;
  const month = (index % 12) + 1;
  const day = (index % 20) + 1;
  const duration = (index % 14) + 3;
  const selectedStatusCycle: VacationStatus[] = ["PLANNED", "TAKEN", "REQUESTED"];
  const status = selectedStatusCycle[index % selectedStatusCycle.length]!;
  const dateMonth = String(month).padStart(2, "0");
  const dateDay = String(day).padStart(2, "0");
  const endDay = String(Math.min(day + duration, 28)).padStart(2, "0");

  return {
    id,
    employee: id,
    employeeDisplayName: employeeName,
    employeeCurrentProject: {
      id: project.id,
      name: project.name,
      role,
    },
    year,
    startDate: `${year}-${dateMonth}-${dateDay}`,
    endDate: `${year}-${dateMonth}-${endDay}`,
    plannedStartDate: `${year}-${dateMonth}-${dateDay}`,
    plannedEndDate: `${year}-${dateMonth}-${endDay}`,
    notes: `${department.name} / ${businessAccount.name} / note ${(index % 7) + 1}`,
    canceled: false,
    status,
    documents: `VAC-${10000 + id}`,
    daysNumber: duration,
  };
});

const filteredRows = computed(() => {
  const normalized = search.value.trim().toLowerCase();

  return allRows.filter((row) => {
    if (row.year !== selectedYear.value) {
      return false;
    }

    if (selectedStatuses.value.length > 0 && !selectedStatuses.value.includes(row.status)) {
      return false;
    }

    if (
      selectedProjects.value.length > 0 &&
      !selectedProjects.value.includes(row.employeeCurrentProject?.id ?? -1)
    ) {
      return false;
    }

    if (
      selectedProjectRoles.value.length > 0 &&
      !selectedProjectRoles.value.includes(row.employeeCurrentProject?.role ?? "")
    ) {
      return false;
    }

    if (!normalized) {
      return true;
    }

    return [
      row.employeeDisplayName,
      row.employeeCurrentProject?.name,
      row.employeeCurrentProject?.role,
      row.documents,
      row.notes,
      row.status,
    ]
      .join(" ")
      .toLowerCase()
      .includes(normalized);
  });
});

const summaryRows = computed<EmployeeVacationSummary[]>(() =>
  employeeVacationSummaryMapper.map(filteredRows.value),
);

async function copyToClipboard(): Promise<void> {
  return Promise.resolve();
}

function onVacationRowClick() {
  return;
}

function onSummaryRowClick() {
  return;
}

function getSelectionLabel(item: unknown): string {
  if (typeof item === "string") {
    return item;
  }

  if (item && typeof item === "object") {
    const raw = "raw" in item ? item.raw : item;
    if (raw && typeof raw === "object") {
      if ("title" in raw && typeof raw.title === "string") {
        return raw.title;
      }
      if ("name" in raw && typeof raw.name === "string") {
        return raw.name;
      }
    }
  }

  return String(item ?? "");
}
</script>

<style scoped>
.table-sandbox-vacations-content {
  min-height: 0;
}
</style>
