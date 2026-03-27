<template>
  <div class="vacations-timeline">
    <div ref="timelineRef" class="timeline"></div>
  </div>
</template>

<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref, watch } from "vue";
import { useI18n } from "vue-i18n";
import { DataSet } from "vis-data";
import { Timeline } from "vis-timeline/standalone";
import type { DataGroup, DataItem, TimelineOptions } from "vis-timeline";
import type { Vacation, VacationStatus } from "@/services/vacation.service";
import { formatDate } from "@/lib/datetime";
import { addDays, getEndOfYear, getStartOfYear } from "@/lib/vacation-dates";

const props = defineProps<{
  vacations: Vacation[];
  year: number;
}>();

const emit = defineEmits<{ (event: "year-navigation", year: number): void }>();

const { t } = useI18n();
const timelineRef = ref<HTMLElement | null>(null);
const timeline = ref<Timeline | null>(null);
const items = new DataSet<DataItem>();
const groups = new DataSet<DataGroup>();

function initTimeline() {
  if (!timelineRef.value) {
    return;
  }
  const options: TimelineOptions = {
    stack: false,
    groupOrder: (a: { content: string }, b: { content: string }) =>
      a.content.localeCompare(b.content),
    start: getStartOfYear(props.year),
    end: getEndOfYear(props.year),
    editable: false,
    zoomable: true,
    zoomKey: "ctrlKey",
    orientation: "both",
    multiselect: false,
    selectable: false,
    tooltip: { followMouse: true },
    locale: "ru",
  };
  timeline.value = new Timeline(timelineRef.value, items, groups, options);
  timeline.value.on("rangechanged", handleRangeChange);
  updateTimelineData();
}

function updateTimelineData() {
  if (!timeline.value) {
    return;
  }
  items.clear();
  groups.clear();
  groups.add(createEmployeeGroups());
  items.add(createVacationItems());
  timeline.value.setWindow(getStartOfYear(props.year), getEndOfYear(props.year), {
    animation: false,
  });
}

function createEmployeeGroups() {
  const employeeIds = Array.from(
    new Set(props.vacations.map((vacation) => vacation.employee)),
  );
  return employeeIds.map((employeeId) => {
    const vacation = props.vacations.find(
      (item) => item.employee === employeeId,
    );
    return {
      id: employeeId,
      content: vacation?.employeeDisplayName || t("unknown_employee"),
    };
  });
}

function createVacationItems() {
  return props.vacations.map((vacation, index) => ({
    id: index,
    group: vacation.employee,
    content: "",
    start: new Date(vacation.startDate),
    end: vacation.endDate
      ? addDays(new Date(vacation.endDate), 1)
      : undefined,
    title: vacationTitle(vacation),
    style: `background-color: ${getStatusColor(vacation.status)}`,
  }));
}

function vacationTitle(vacation: Vacation) {
  return `${vacation.employeeDisplayName}<br>${formatDateRange(
    vacation.startDate,
    vacation.endDate,
  )}<br>${t(`VACATION_STATUS_ENUM.${vacation.status}`)}`;
}

function formatDateRange(startDate?: string, endDate?: string): string {
  const start = formatDate(startDate);
  const end = formatDate(endDate);
  return end ? `${start} - ${end}` : start;
}

function getStatusColor(status: VacationStatus): string {
  switch (status) {
    case "PLANNED":
      return "#00ACC1";
    case "TAKEN":
      return "#43A047";
    case "COMPENSATION":
      return "#1E88E5";
    case "CANCELED":
      return "#E53935";
    case "REJECTED":
      return "#757575";
    case "REQUESTED":
      return "#FFB74D";
    default:
      return "";
  }
}

function handleRangeChange() {
  if (!timeline.value) {
    return;
  }
  const window = timeline.value.getWindow();
  const start = window.start as Date;
  const end = window.end as Date;
  const middle = new Date((start.getTime() + end.getTime()) / 2);
  const middleYear = middle.getFullYear();
  if (middleYear !== props.year) {
    emit("year-navigation", middleYear);
  }
}

onMounted(() => {
  initTimeline();
});

onBeforeUnmount(() => {
  if (timeline.value) {
    timeline.value.destroy();
    timeline.value = null;
  }
});

watch(
  () => [props.vacations, props.year],
  () => updateTimelineData(),
  { deep: true },
);
</script>

<style>
@import "vis-timeline/styles/vis-timeline-graph2d.min.css";
</style>

<style scoped>
.vacations-timeline {
  width: 100%;
}

.timeline {
  width: 100%;
}
</style>
