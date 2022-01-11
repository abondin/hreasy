<!-- My skills -->
<template>
  <div>
    <v-card>
      <v-card-title>
        <div>{{ $t('Мои навыки') }}</div>
      </v-card-title>

      <v-card-text>
        <skills-chips
            @submit="fetchData()"
            :skills="skills"
            :employee-id="currentUserEmployeeId"></skills-chips>
      </v-card-text>
    </v-card>
  </div>
</template>


<script lang="ts">
import Vue from 'vue'
import Component from 'vue-class-component';
import {DateTimeUtils} from "@/components/datetimeutils";
import skillsService, {Skill} from "@/components/empl/skills/skills.service";
import {Getter} from "vuex-class";
import SkillsChips from "@/components/empl/skills/SkillsChips.vue";

const namespace_auth: string = 'auth';


@Component({
  components: {SkillsChips}
})
export default class MySkills extends Vue {
  loading: boolean = false;
  skills: Skill[] = [];

  @Getter("employeeId", {namespace: namespace_auth})
  private currentUserEmployeeId!: number;

  /**
   * Lifecycle hook
   */
  created() {
    this.$store.dispatch('dict/reloadSkillGroups').then(() => {
      this.$store.dispatch('dict/reloadSharedSkills').then(() => {
        return this.fetchData();
      })
    })
  }


  private filteredItems() {
    return this.skills;
  }


  private fetchData() {
    this.loading = true;
    return skillsService.my()
        .then(data => {
              this.skills = data;
              return;
            }
        ).finally(() => {
          this.loading = false
        });
  }

  private formatDate(date: string): string | undefined {
    return DateTimeUtils.formatFromIso(date);
  }

}
</script>

<style scoped>

</style>
