<!-- Chips to download and upload tech profiles documents -->
<template>
  <v-container>
    <v-row no-gutters>
      <!-- All profiles -->
      <v-col class="mr-5" cols="auto" v-if="techProfiles && techProfiles.length>0">
        <div v-for="p in techProfiles" v-bind:key="p.filename">
          <v-chip class="mr-2" close @click:close="openDeleteTechProfileDialog(p)">
            <a :href="getTechProfileDownloadUrl(p)" download>{{ p.filename }}</a>
          </v-chip>
        </div>
      </v-col>

      <v-col cols="auto" class="mt-5" v-if="canUploadTechProfiles()">
        <v-tooltip bottom>
          <template v-slot:activator="{ on: ton, attrs: tattrs}">
            <v-btn v-bind="tattrs" v-on="ton" class="mr-2" color="primary" outlined rounded icon
                   @click="this.uploadTechprofileDialog=true">
              <v-icon>mdi-plus</v-icon>
            </v-btn>
          </template>
          <span>{{ $t('Загрузить квалификационную карточку') }}</span>
        </v-tooltip>
      </v-col>


    </v-row>


    <!--    <v-dialog v-model="addSkillDialog" max-width="600">-->
    <!--      <add-skill-form-->
    <!--          :all-groups="allGroups"-->
    <!--          :all-shared-skills="allSharedSkills"-->
    <!--          :employee-id="employeeId"-->
    <!--          @submit="skillAdded"-->
    <!--          @close="addSkillDialog=false"></add-skill-form>-->
    <!--    </v-dialog>-->

    <!--    <v-dialog v-model="deleteDialog" max-width="600">-->
    <!--      <v-card v-if="skillToDelete">-->
    <!--        <v-card-title primary-title>{{ $t('Удаление') + ' ' + skillToDelete.name }}</v-card-title>-->
    <!--        <v-card-text>-->
    <!--          {{ $t('Вы уверены что хотите удалить запись?') }}-->
    <!--        </v-card-text>-->
    <!--        <v-divider></v-divider>-->
    <!--        <v-card-actions>-->
    <!--          <v-spacer></v-spacer>-->
    <!--          <v-btn text @click="skillToDelete = null;deleteDialog=false">-->
    <!--            {{ $t('Нет') }}-->
    <!--          </v-btn>-->
    <!--          <v-btn color="primary" @click="deleteSkill()">-->
    <!--            {{ $t('Да') }}-->
    <!--          </v-btn>-->
    <!--        </v-card-actions>-->
    <!--      </v-card>-->
    <!--    </v-dialog>-->

  </v-container>
</template>

<script lang="ts">
import Component from "vue-class-component";
import Vue from "vue";
import techprofileService, {TechProfile} from "../techprofile/techprofile.service";
import {Prop} from "vue-property-decorator";
import MyFileUploader from "@/components/shared/MyFileUploader.vue";
import permissionService from "@/store/modules/permission.service";
import logger from "@/logger";

@Component({
  components: {MyFileUploader}
})
export default class TechProfilesChips extends Vue {


  @Prop({required: true})
  employeeId!: number;

  uploadTechprofileDialog = false;
  deleteTechprofileDialog = false;

  techProfiles: TechProfile[] = [];

  private getTechProfileUploadUrl() {
    return techprofileService.getTechProfileUploadUrl(this.employeeId);
  }

  private getTechProfileDownloadUrl(profile: TechProfile) {
    return techprofileService.getTechProfileDownloadUrl(this.employeeId, profile.accessToken, profile.filename,);
  }

  private canDownloadTechProfiles(): boolean {
    return permissionService.canDownloadTechProfiles(this.employeeId);
  }

  private canUploadTechProfiles(): boolean {
    return permissionService.canUploadTechProfiles(this.employeeId);
  }


  public loadTechProfiles(): any {
    this.techProfiles.length = 0;

    return techprofileService.find(this.employeeId).then(techProfiles => {
      this.techProfiles = techProfiles;
      return techProfiles;
    });
  }


  private openDeleteTechProfileDialog(techProfile: TechProfile) {
    //TODO
    logger.debug(techProfile.filename);
  }

}

</script>

