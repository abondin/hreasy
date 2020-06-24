<!--
    Component to show and update employee avatar
-->
<template>
    <div>
        <v-avatar
                class="profile hr-cropper"
                color="grey"
                size="164">
            <v-img v-if="emplAvatar" :src="emplAvatar"></v-img>
            <avatar-cropper
                    output-mime="image/jpeg"
                    @error="handleError"
                    @uploaded="handleUploaded"
                    :trigger="'#pick-avatar-'+employee.id"
                    :labels='{submit: $tc("Применить"), cancel: $tc("Отмена")}'
                    :upload-url="getAvatarUploadUrl()"
                    :withCredentials=true
                    :output-options="{width: 256, height: 256}"
            />
        </v-avatar>
        <div class="ml-5" >
            <v-btn x-small :disabled="!canUpdateAvatar()" :id="'pick-avatar-'+employee.id">{{$tc('Загрузить фото')}}</v-btn>
        </div>
        <div class="error" v-if="uploadError">{{uploadError}}</div>
    </div>
</template>

<script lang="ts">
    import Component from "vue-class-component";
    import AvatarCropper from "vue-avatar-cropper";
    import Vue from "vue";
    import employeeService, {Employee} from "./employee.service";
    import {Prop} from "vue-property-decorator";
    import permissionService from "@/store/modules/permission.service";

    @Component({
        components: {"avatar-cropper": AvatarCropper}
    })
    export default class EmployeeAvatarUploader extends Vue {
        private emplAvatar: String | null = null;
        private uploadError: String | null = null;

        @Prop({required: true})
        employee!: Employee;

        /**
         * Lifecycle hook
         */
        created() {
            if (this.employee.hasAvatar) {
                this.emplAvatar = employeeService.getAvatarUrl(this.employee.id);
            }
        }

        private handleUploaded(resp: any) {
            if (this.employee) {
                this.emplAvatar = employeeService.getAvatarUrl(this.employee.id) + '?' + Math.random();
            }
        }

        private handleError(error: Error | String) {
            this.uploadError = error.toString();
        }


        private getAvatarUploadUrl() {
            return employeeService.getAvatarUploadUrl(this.employee.id);
        }

        private canUpdateAvatar() {
            return permissionService.canUpdateAvatar(this.employee.id);
        }
    }
</script>

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