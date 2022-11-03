<!--
    Component to show and update employee avatar
-->
<template>
  <div class="d-flex flex-column" style="width: 164px">
    <v-overlay :value="fullscreen" @click="fullscreen = false">
      <v-card @click.stop="fullscreen = false">
        <v-img max-height="500" max-width="500" v-if="emplAvatar" :src="emplAvatar"></v-img>
        <v-btn
            absolute
            dark
            fab
            top
            right
        >
          <v-icon>mdi-close</v-icon>
        </v-btn>
      </v-card>
    </v-overlay>
    <v-avatar height="164px" width="164px"
              class="profile hr-cropper"
              color="grey">
      <v-img v-if="emplAvatar" :src="emplAvatar" @click.stop="fullscreen = !fullscreen"></v-img>
      <avatar-cropper
          output-mime="image/jpeg"
          @error="handleError"
          @uploaded="handleUploaded"
          :trigger="'#pick-avatar-'+employee.id"
          :labels='{submit: $tc("Применить"), cancel: $tc("Отмена")}'
          :upload-url="getAvatarUploadUrl()"
          :withCredentials=true
          :output-options="{width: 1024, height: 1024}"
      />
    </v-avatar>
    <div class="align-self-center">
      <v-btn x-small :disabled="!canUpdateAvatar()" :id="'pick-avatar-'+employee.id">{{ $tc('Загрузить фото') }}
      </v-btn>
    </div>
    <div class="error" v-if="uploadError">{{ uploadError }}</div>
  </div>
</template>

<script lang="ts">
import Component from "vue-class-component";
import AvatarCropper from "vue-avatar-cropper";
import Vue from "vue";
import employeeService, {Employee} from "./employee.service";
import {Prop, Watch} from "vue-property-decorator";
import permissionService from "@/store/modules/permission.service";

@Component({
  components: {"avatar-cropper": AvatarCropper}
})
export default class EmployeeAvatarUploader extends Vue {
  private emplAvatar: string | null = null;
  private uploadError: string | null = null;

  @Prop({required: true})
  employee!: Employee;

  // Show image in fullscreen
  private fullscreen = false;

  /**
   * Lifecycle hook
   */
  created() {
    if (this.employee.hasAvatar) {
      this.emplAvatar = employeeService.getAvatarUrl(this.employee.id);
    }
  }

  @Watch("employee")
  private watchEmployee(){
    if (this.employee && this.employee.hasAvatar) {
      this.emplAvatar = employeeService.getAvatarUrl(this.employee.id);
    } else {
      this.emplAvatar = null;
    }
  }

  private handleUploaded() {
    if (this.employee) {
      this.emplAvatar = employeeService.getAvatarUrl(this.employee.id) + '?' + Math.random();
    }
  }

  private handleError(message: string) {
    this.uploadError = message;
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
