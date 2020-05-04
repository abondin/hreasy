<!-- Employees edit form-->
<template>
    <v-container v-if="employee">
        {{employee.displayName}}
    </v-container>
</template>


<script lang="ts">
    import Vue from 'vue'
    import Component from 'vue-class-component';
    import employeeService, {Employee} from "@/components/empl/employee.service";


    @Component
    export default class EmployeesComponent extends Vue {
        loading: boolean = false;

        private employee: Employee | null = null;

        /**
         * Lifecycle hook
         */
        created() {
            this.fetchData(+this.$attrs.id);
        }

        private fetchData(employeeId: number) {
            this.loading = true;
            return employeeService.find(employeeId)
                .then(data => {
                        this.employee = data;
                    }
                ).finally(() => {
                    this.loading = false
                });
        }

        private getAvatarUrl(employeeId: number) {
            return employeeService.getAvatarUrl(employeeId);
        }
    }
</script>

<style scoped>

</style>
