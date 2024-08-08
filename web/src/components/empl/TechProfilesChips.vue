<!-- Chips to download and upload tech profiles documents -->
<template>
  <span>
    <span v-if="downloadPermissionDenied"><v-icon>mdi-lock-alert</v-icon> {{ $t('Не достаточно прав') }}</span>
  <v-container v-else>
    <v-row no-gutters>
      <!-- All profiles -->
      <v-col class="mr-5" cols="auto" v-if="techProfiles && techProfiles.length>0">
        <div v-for="p in techProfiles" v-bind:key="p.filename">
          <v-tooltip bottom>
            <template v-slot:activator="{ on: ton, attrs: tattrs}">
              <v-chip v-bind="tattrs" v-on="ton" class="mr-2" :close="canUploadTechProfiles() && !loading"
                      @click:close="openDeleteTechProfileDialog(p)">
                <a :href="getTechProfileDownloadUrl(p)" download class="tech-profile-filename-link">{{ p.filename }}</a>
              </v-chip>
            </template>
            <span>{{ $t('Скачать документ') }}  '{{ p.filename }}'</span>
          </v-tooltip>
        </div>
      </v-col>

      <v-col cols="auto" v-if="canUploadTechProfiles()">
        <v-tooltip bottom>
          <template v-slot:activator="{ on: ton, attrs: tattrs}">
            <v-btn v-bind="tattrs" v-on="ton" color="primary" outlined rounded icon
                   @click="uploadTechprofileDialog=true" :disabled="loading">
              <v-icon>mdi-plus</v-icon>
            </v-btn>
          </template>
          <span>{{ $t('Загрузить квалификационную карточку') }}</span>
        </v-tooltip>
      </v-col>


    </v-row>


    <v-dialog v-model="uploadTechprofileDialog" max-width="600">
      <my-file-uploader :file-id="'techprofile-'+{employeeId}" :post-action="getTechProfileUploadUrl()"
                        @close="techProfileUploaded()"></my-file-uploader>
    </v-dialog>

    <v-dialog v-model="deleteTechprofileDialog" max-width="600">
      <v-card v-if="profileToDelete">
        <v-card-title primary-title>{{ $t('Удаление') + ' ' + profileToDelete.filename }}</v-card-title>
        <v-card-text>
          {{ $t('Вы уверены, что хотите удалить запись?') }}
        </v-card-text>
        <v-alert type="error" v-if="deleteError">{{ deleteError }}</v-alert>
        <v-divider></v-divider>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn text @click="profileToDelete = null;deleteTechprofileDialog=false">
            {{ $t('Нет') }}
          </v-btn>
          <v-btn color="primary" @click="deleteTechProfile()">
            {{ $t('Да') }}
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

  </v-container>
    </span>
</template>

<style lang="css">
.tech-profile-filename-link {
  white-space: nowrap;
  overflow: hidden;
  max-width: 200px;
  text-overflow: ellipsis;
}
</style>

<script lang="ts">
import Component from "vue-class-component";
import Vue from "vue";
import techprofileService, {TechProfile} from "../techprofile/techprofile.service";
import {Prop} from "vue-property-decorator";
import MyFileUploader from "@/components/shared/MyFileUploader.vue";
import permissionService from "@/store/modules/permission.service";
import {errorUtils} from "@/components/errors";

@Component({
  components: {MyFileUploader}
})
export default class TechProfilesChips extends Vue {


  @Prop({required: true})
  employeeId!: number;

  uploadTechprofileDialog = false;
  deleteTechprofileDialog = false;

  techProfiles: TechProfile[] = [];
  profileToDelete: TechProfile | null = null;
  deleteError: string | null = null;

  /**
   * UI code provide only basic security validation based on user permissions without employee organization hierarchy.
   * UI can allow current user to download profile, but backend restrict it for example because current user has
   * PM permission, but not for project of the employee
   */
  downloadPermissionDenied = false;


  loading = false;

  private getTechProfileUploadUrl() {
    return techprofileService.getTechProfileUploadUrl(this.employeeId);
  }

  private getTechProfileDownloadUrl(profile: TechProfile) {
    return techprofileService.getTechProfileDownloadUrl(this.employeeId, profile.accessToken, profile.filename,);
  }

  private canUploadTechProfiles(): boolean {
    return permissionService.canUploadTechProfiles(this.employeeId);
  }


  public loadTechProfiles(): any {
    this.techProfiles.length = 0;
    this.loading = true;
    this.downloadPermissionDenied = false;
    return techprofileService.find(this.employeeId).then(techProfiles => {
      this.techProfiles = techProfiles;
      return techProfiles;
    }).catch(error => {
      if (errorUtils.isAccessDenied(error)) {
        // it is normal situation if user with permission, based on project/department tries to see profiles
        // of employee from another project/department
        this.downloadPermissionDenied = true;
      } else {
        throw error;
      }
    }).finally(() => {
      this.loading = false;
    });
  }


  private openDeleteTechProfileDialog(techProfile: TechProfile) {
    this.profileToDelete = techProfile;
    this.deleteTechprofileDialog = true;
  }

  private techProfileUploaded() {
    this.uploadTechprofileDialog = false;
    this.loadTechProfiles();
  }

  private deleteTechProfile() {
    this.deleteError = null;
    this.loading = true;
    if (this.profileToDelete) {
      return techprofileService.delete(this.employeeId, this.profileToDelete.filename)
          .then(() => {
                this.deleteTechprofileDialog = false;
                this.loadTechProfiles();
              },
              error => {
                this.deleteError = errorUtils.shortMessage(error);
              }).finally(() => {
            this.loading = false;
          });
    }
  }

}

</script>

