<!-- Employees edit form-->
<template>
    <v-container v-if="employee">
        <!-- Work in progress -->
        <v-alert
                color="red"
                dark
                icon="mdi-grill"
                border="right">
            Work in progress
        </v-alert>

        <v-card>
            <v-row>
                <v-col cols="12">
                    <v-banner>
                        <router-link :to="{name:'employees'}">
                            <span>
                            <v-icon>arrow_back_ios</v-icon>
                            {{$tc('Список сотрудников')}}
                            </span>
                        </router-link>
                    </v-banner>
                </v-col>
            </v-row>
            <v-row align="start">
                <v-col cols="4" lg="2">
                    <div style="max-width: 164px; margin-left: 10px" align="center">
                        <v-avatar
                                class="profile hr-cropper"
                                color="grey"
                                size="164">
                            <v-img v-if="emplAvatar" :src="emplAvatar"></v-img>
                            <avatar-cropper
                                    output-mime="image/jpeg"
                                    @uploaded="handleUploaded"
                                    trigger="#pick-avatar"
                                    :labels='{submit: $tc("Применить"), cancel: $tc("Отмена")}'
                                    :upload-url="getAvatarUploadUrl(employee.id)"
                                    :withCredentials=true
                                    :output-options="{width: 256, height: 256}"
                            />
                        </v-avatar>
                        <p>
                            <button id="pick-avatar">{{$tc('Загрузить фото')}}</button>
                        </p>
                    </div>
                </v-col>
                <v-col cols="8" lg="10">
                    <v-list-item>
                        <v-list-item-content>
                            <v-list-item-title class="title">{{employee.displayName}}</v-list-item-title>
                            <v-list-item-subtitle>{{$tc('Отдел')}} :
                                {{employee.department?employee.department.name:$tc("Не задан")}}
                            </v-list-item-subtitle>
                            <v-list-item-subtitle>{{$tc('Текущий проект')}} :
                                {{employee.currentProject?employee.currentProject.name:$tc("Не задан")}}
                            </v-list-item-subtitle>
                            <v-list-item-subtitle>
                                {{$tc('Почтовый адрес')}} : {{employee.email?employee.email:$tc("Не задан")}}
                            </v-list-item-subtitle>
                        </v-list-item-content>
                    </v-list-item>
                </v-col>

            </v-row>
        </v-card>

    </v-container>
</template>

<style lang="scss">
    .hr-cropper .avatar-cropper {
        .avatar-cropper-close {
            color: white;
        }

        .avatar-cropper-container {
            background: black;

            .avatar-cropper-footer {
                .avatar-cropper-btn {
                    margin: 5px;
                    background: darken(blue, 3);

                    &:hover {
                        background: blue;
                    }
                }
            }

        }
    }
</style>

<script lang="ts">
    import Vue from 'vue'
    import Component from 'vue-class-component';
    import employeeService, {Employee} from "@/components/empl/employee.service";
    import AvatarCropper from "vue-avatar-cropper"


    @Component({
        components: {"avatar-cropper": AvatarCropper}
    })
    export default class EmployeesComponent extends Vue {
        loading: boolean = false;

        private employee: Employee | null = null;
        private emplAvatar: String | null = null;

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
                        if (this.employee.hasAvatar) {
                            this.emplAvatar = employeeService.getAvatarUrl(this.employee.id);
                        }
                    }
                ).finally(() => {
                    this.loading = false
                });
        }

        private handleUploaded(resp: any) {
            if (this.employee) {
                this.emplAvatar = employeeService.getAvatarUrl(this.employee.id) + '?' + Math.random();
            }
        }


        private getAvatarUploadUrl(employeeId: number) {
            return employeeService.getAvatarUploadUrl(employeeId);
        }
    }
</script>

<style scoped>

</style>
