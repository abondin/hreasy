<!-- My skills -->
<template>
  <div>
    <v-card>
      <v-card-title>
        <div>{{ $t('Мои навыки') }}</div>
        <v-spacer></v-spacer>
        <v-tooltip bottom>
          <template v-slot:activator="{ on: ton, attrs: tattrs}">
            <div v-bind="tattrs" v-on="ton" class="col-auto">
              <v-btn text color="primary" :disabled="loading" @click="openAddSkillDialog()" icon>
                <v-icon>mdi-plus</v-icon>
              </v-btn>
            </div>
          </template>
          <span>{{ $t('Добавить навык') }}</span>
        </v-tooltip>
      </v-card-title>

      <v-card-text>
        <div v-for="s in skills" v-bind:key="s.id">
          {{ s.name }}
        </div>
      </v-card-text>
    </v-card>
    <v-dialog v-model="addSkillDialog" max-width="600">
      <add-skill-form
          :all-groups="allSkillGroups"
          :all-shared-skills="allSharedSkills"
          @close="addSkillDialog=false;fetchData()"></add-skill-form>
    </v-dialog>
  </div>
</template>


<script lang="ts">
import Vue from 'vue'
import Component from 'vue-class-component';
import {DateTimeUtils} from "@/components/datetimeutils";
import skillsService, {Skill} from "@/components/empl/skills/skills.service";
import AddSkillForm from "@/components/empl/skills/AddSkillForm.vue";
import {Getter} from "vuex-class";
import {SimpleDict} from "@/store/modules/dict";
import {SharedSkillName} from "@/store/modules/dict.service";

const namespace: string = 'dict';

@Component({
  components: {AddSkillForm}
})
export default class MySkills extends Vue {
  loading: boolean = false;
  skills: Skill[] = [];

  private addSkillDialog = false;

  @Getter("skillGroups", {namespace})
  private allSkillGroups!: Array<SimpleDict>;

  @Getter("sharedSkills", {namespace})
  private allSharedSkills!: Array<SharedSkillName>;

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

  public openAddSkillDialog() {
    this.addSkillDialog = true;
  }

  private formatDate(date: string): string | undefined {
    return DateTimeUtils.formatFromIso(date);
  }

}
</script>

<style scoped>

</style>
