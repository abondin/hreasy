import {computed, ref, watch} from 'vue';
import type {Employee} from '@/services/employee.service';
import {findEmployee} from '@/services/employee.service';

export function useEmployeeProfile(employeeId: () => number | null | undefined) {
  const employee = ref<Employee | null>(null);
  const loading = ref(false);
  const error = ref<unknown>(null);

  async function load(id: number) {
    loading.value = true;
    error.value = null;
    try {
      const result = await findEmployee(id);
      employee.value = result;
    } catch (err) {
      error.value = err;
      employee.value = null;
    } finally {
      loading.value = false;
    }
  }

  watch(
    employeeId,
    id => {
      if (typeof id === 'number') {
        void load(id);
      } else {
        employee.value = null;
      }
    },
    {immediate: true}
  );

  return {
    employee,
    loading: computed(() => loading.value),
    error: computed(() => error.value),
    reload() {
      const id = employeeId();
      if (typeof id === 'number') {
        return load(id);
      }
      return Promise.resolve();
    }
  };
}
