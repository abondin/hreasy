import { shallowMount } from "@vue/test-utils";
import { expect, it, vi } from "vitest";
import ProjectTransferRequestDialog from "@/components/project/ProjectTransferRequestDialog.vue";

vi.mock("vue-i18n", () => ({ useI18n: () => ({ t: (key: string) => key }) }));
vi.mock("@/stores/auth", () => ({ useAuthStore: () => ({ employeeId: 10 }) }));
vi.mock("@/lib/permissions", () => ({
  usePermissions: () => ({ canUpdateCurrentProjectGlobally: () => false }),
}));

it("uses server decision permission for the approval prompt, including for the request author", async () => {
  const request = {
    id: 1, employeeId: 20, fromProjectId: 30, fromProjectName: "Source",
    toProjectId: 40, toProjectName: "Target", requestedProjectRole: null,
    createdBy: 10, createdByDisplayName: "Alex Morgan",
    approverEmployeeId: 50, approverDisplayName: "Jordan Lee",
    createdAt: "2026-09-28T09:00:00Z", expiresAt: "2026-10-12T09:00:00Z",
    canMakeDecision: false,
  };
  const wrapper = shallowMount(ProjectTransferRequestDialog, {
    props: { modelValue: true, employeeName: "Employee Example", request },
  });
  const info = () => wrapper.get('v-alert-stub[type="info"]').text();
  expect(info()).toBe("Заявка на перевод ожидает согласования.");
  await wrapper.setProps({ request: { ...request, canMakeDecision: true } });
  expect(info()).toBe("Вам необходимо согласовать перевод сотрудника. Одобрите или отклоните заявку через меню «Действия».");
  wrapper.unmount();
});
