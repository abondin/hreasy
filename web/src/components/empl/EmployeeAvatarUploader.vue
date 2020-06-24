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
                    trigger="#pick-avatar"
                    :labels='{submit: $tc("Применить"), cancel: $tc("Отмена")}'
                    :upload-url="getAvatarUploadUrl(employee.id)"
                    :withCredentials=true
                    :output-options="{width: 256, height: 256}"
            />
        </v-avatar>
        <div class="ml-6">
            <button id="pick-avatar">{{$tc('Загрузить фото')}}</button>
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


        private getAvatarUploadUrl(employeeId: number) {
            return employeeService.getAvatarUploadUrl(employeeId);
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
