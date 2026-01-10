import { reactive, ref } from "vue";
import { useI18n } from "vue-i18n";
import type { MyVacation, RequestOrUpdateMyVacation } from "@/services/vacation.service";
import {
  calculateVacationDays,
  formatDateOnly,
  getStartOfYear,
  parseDateOnly,
} from "@/lib/vacation-dates";
import { formatDate } from "@/lib/datetime";
import {
  requestVacation,
  updatePlanningVacation,
} from "@/services/vacation.service";
import { errorUtils } from "@/lib/errors";

export type RequestVacationFormData = {
  year: number;
  dates: string[];
  notes: string;
  daysNumber: number;
};

export function useRequestVacationAction() {
  const { t } = useI18n();
  const open = ref(false);
  const loading = ref(false);
  const error = ref("");
  const daysNumberSetManually = ref(false);
  const daysNotIncludedInVacations = ref<string[]>([]);
  const vacationId = ref<number | null>(null);

  const formData = reactive<RequestVacationFormData>({
    year: new Date().getFullYear(),
    dates: [],
    notes: "",
    daysNumber: 14,
  });

  function setDaysNotIncluded(days: string[]) {
    daysNotIncludedInVacations.value = days;
  }

  function openRequestVacationDialog(year: number) {
    vacationId.value = null;
    daysNumberSetManually.value = false;
    formData.year = year;
    const start = getStartOfYear(year);
    const end = new Date(start.getTime());
    end.setDate(end.getDate() + 14);
    formData.dates = [formatDateOnly(start), formatDateOnly(end)];
    formData.notes = "";
    formData.daysNumber = 14;
    updateDaysNumber();
    open.value = true;
  }

  function openUpdateVacationDialog(vacation: MyVacation) {
    vacationId.value = vacation.id;
    daysNumberSetManually.value = false;
    formData.year = vacation.year;
    formData.dates = [vacation.startDate, vacation.endDate];
    formData.notes = vacation.notes;
    formData.daysNumber = vacation.daysNumber;
    updateDaysNumber();
    open.value = true;
  }

  function closeDialog() {
    open.value = false;
    error.value = "";
    loading.value = false;
  }

  function datesUpdated() {
    if (formData.dates.length < 2) {
      return;
    }
    const start = parseDateOnly(formData.dates[0]);
    const end = parseDateOnly(formData.dates[1]);
    if (start && end && end < start) {
      formData.dates = [formatDateOnly(end), formatDateOnly(start)];
    }
    updateDaysNumber();
  }

  function updateDaysNumber() {
    if (formData.dates.length < 2) {
      return;
    }
    const [start, end] = formData.dates;
    formData.daysNumber = calculateVacationDays(
      start,
      end,
      daysNotIncludedInVacations.value,
    );
  }

  function formattedDates(): string {
    if (formData.dates.length < 2) {
      return "";
    }
    const start = formatDate(formData.dates[0]);
    const end = formatDate(formData.dates[1]);
    return start && end ? `${start} - ${end}` : "";
  }

  async function submit(): Promise<void> {
    if (loading.value) {
      return;
    }
    if (formData.dates.length < 2) {
      error.value = t("Выберите даты");
      return;
    }

    const [startDate, endDate] = formData.dates;
    const payload: RequestOrUpdateMyVacation = {
      year: formData.year,
      startDate,
      endDate,
      notes: formData.notes,
      daysNumber: formData.daysNumber,
    };

    loading.value = true;
    error.value = "";
    try {
      if (vacationId.value) {
        await updatePlanningVacation(vacationId.value, payload);
      } else {
        await requestVacation(payload);
      }
      closeDialog();
    } catch (err) {
      error.value = errorUtils.shortMessage(err);
    } finally {
      loading.value = false;
    }
  }

  return {
    open,
    loading,
    error,
    formData,
    daysNumberSetManually,
    openRequestVacationDialog,
    openUpdateVacationDialog,
    closeDialog,
    datesUpdated,
    formattedDates,
    updateDaysNumber,
    submit,
    setDaysNotIncluded,
  };
}

export type RequestVacationAction = ReturnType<typeof useRequestVacationAction>;
