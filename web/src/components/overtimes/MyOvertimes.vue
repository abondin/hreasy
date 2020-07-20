<!-- All vacations table-->
<template>
    <v-container fluid>
        <!-- Work in progress -->
        <v-alert
                color="red"
                dark
                icon="mdi-grill"
                border="right">
            Work in progress
        </v-alert>
        <employee-overtime-component
                :employee-id="employeeId"
                :selected-period="selectedPeriod">

        </employee-overtime-component>
    </v-container>
</template>


<script lang="ts">
    import Vue from 'vue'
    import Component from 'vue-class-component';
    import {Getter} from "vuex-class";
    import {ReportPeriod} from "@/components/overtimes/overtime.service";
    import AddOvertimeItemDialog from "@/components/overtimes/AddOvertimeItemDialog.vue";
    import EmployeeOvertimeComponent from "@/components/overtimes/EmployeeOvertimeComponent.vue";

    const namespace_auth: string = 'auth';
    @Component({
        components: {EmployeeOvertimeComponent, OvertimeAddOrEdit: AddOvertimeItemDialog}
    })
    export default class MyOvertimes extends Vue {
        @Getter("employeeId", {namespace: namespace_auth})
        employeeId!: number;

        selectedPeriod = ReportPeriod.currentPeriod();

    }
</script>

<style scoped>

</style>
