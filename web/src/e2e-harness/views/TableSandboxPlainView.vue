<template>
  <TableFirstPageLayout test-id="table-sandbox-plain-view">
    <div class="d-flex flex-column h-100 min-h-0">
      <div class="d-flex align-center justify-space-between ga-4 flex-shrink-0">
        <div class="text-body-1 font-weight-medium">
          Plain Table Sandbox
        </div>

        <v-btn
          variant="outlined"
          size="small"
          data-testid="table-sandbox-plain-go-echo"
          :to="{ name: 'table-sandbox-echo' }"
        >
          Open Echo
        </v-btn>
      </div>

      <div class="h-100 pt-4">
        <TablePageCard test-id="table-sandbox-plain-card">
          <HREasyTableBase
            data-testid="table-sandbox-plain-table"
            table-class="table-sandbox-plain-table"
            :headers="headers"
            :items="rows"
            :height="'fill'"
            :fixed-header="true"
            density="compact"
            :hover="true"
          >
            <template #filters>
              <AdaptiveFilterBar :items="filterBarItems">
                <template #filter-search>
                  <v-text-field
                    v-model="search"
                    data-testid="table-sandbox-plain-filter-search"
                    label="Search"
                    prepend-inner-icon="mdi-magnify"
                    variant="outlined"
                    density="compact"
                    clearable
                    hide-details
                  />
                </template>
              </AdaptiveFilterBar>
            </template>
          </HREasyTableBase>
        </TablePageCard>
      </div>
    </div>
  </TableFirstPageLayout>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import type { VDataTable } from "vuetify/components";
import AdaptiveFilterBar from "@/components/shared/AdaptiveFilterBar.vue";
import HREasyTableBase from "@/components/shared/HREasyTableBase.vue";
import TableFirstPageLayout from "@/components/shared/TableFirstPageLayout.vue";
import TablePageCard from "@/components/shared/TablePageCard.vue";

defineOptions({
  name: "TableSandboxPlainView",
});

type DataTableHeader = VDataTable["$props"]["headers"];
type PlainRow = {
  id: number;
  name: string;
  department: string;
  project: string;
  owner: string;
};

const search = ref("");

const headers: DataTableHeader = [
  { title: "Name", key: "name", width: "260px" },
  { title: "Department", key: "department", width: "240px" },
  { title: "Project", key: "project", width: "240px" },
  { title: "Owner", key: "owner", width: "220px" },
];

const filterBarItems = computed(() => [
  { id: "search", minWidth: 360, active: search.value.trim().length > 0, grow: true },
]);

const allRows: PlainRow[] = Array.from({ length: 180 }, (_, index) => {
  const id = index + 1;
  return {
    id,
    name: `Plain Row ${id}`,
    department: `Department ${(id % 7) + 1}`,
    project: `Project ${(id % 9) + 1}`,
    owner: `Owner ${(id % 11) + 1}`,
  };
});

const rows = computed(() => {
  const normalized = search.value.trim().toLowerCase();
  if (!normalized) {
    return allRows;
  }

  return allRows.filter((row) => {
    return `${row.name} ${row.department} ${row.project} ${row.owner}`.toLowerCase().includes(normalized);
  });
});
</script>
