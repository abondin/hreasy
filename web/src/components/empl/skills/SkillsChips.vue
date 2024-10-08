<!-- List of skills for given employee-->
<template>
  <v-container>
    <v-row no-gutters>
      <!-- All skills -->
      <v-col class="mr-5" cols="auto" v-for="g in groupedSkills" v-bind:key="g.groupId">
        {{ g.groupName }}
        <div v-for="s in g.skills" v-bind:key="s.id">
          <v-menu v-model="s.menu" bottom right transition="scale-transition" origin="top left">
            <template v-slot:activator="{ on }">
              <v-chip v-on="on" class="mr-2 mb-1" close @click:close="openDeleteSkillDialog(s)">{{ s.name }}
                ({{
                  $t('Средний рейтинг') + ': ' + (s.ratings.averageRating ? s.ratings.averageRating : $t('Нет оценок'))
                }})
              </v-chip>
            </template>
            <v-sheet>
              <v-rating half :value="s.ratings.myRating" @input="v=>updateRating(s, v)"></v-rating>
              <ul>
                <li>
                  {{ $t('Мой рейтинг') + ': ' + (s.ratings.myRating ? s.ratings.myRating : $t('Не задан')) }}
                </li>
                <li>{{ $t('Средний рейтинг') }}:
                  <span v-if="s.ratings.averageRating">{{
                      s.ratings.averageRating
                    }} ({{ $tc('ratingCounts', s.ratings.ratingsCount) }} )</span>
                  <span v-else>
                    {{ $t('Нет оценок') }}
                  </span>
                </li>
              </ul>

            </v-sheet>
          </v-menu>
        </div>
      </v-col>

      <!-- New skill -->
      <v-col cols="auto" v-if="skillsChipsEditable(employeeId)" align-self="center">
        <v-tooltip bottom>
          <template v-slot:activator="{ on: ton, attrs: tattrs}">
            <v-btn v-bind="tattrs" v-on="ton" class="mr-2" color="primary" outlined rounded icon
                   @click="openAddSkillDialog()">
              <v-icon>mdi-plus</v-icon>
            </v-btn>
          </template>
          <span>{{ $t('Добавить навык') }}</span>
        </v-tooltip>
      </v-col>

    </v-row>

    <v-dialog v-model="addSkillDialog" max-width="600">
      <add-skill-form
          :all-groups="allGroups"
          :all-shared-skills="allSharedSkills"
          :employee-id="employeeId"
          @submit="skillAdded"
          @close="addSkillDialog=false"></add-skill-form>
    </v-dialog>

    <v-dialog v-model="deleteDialog" max-width="600">
      <v-card v-if="skillToDelete">
        <v-card-title primary-title>{{ $t('Удаление') + ' ' + skillToDelete.name }}</v-card-title>
        <v-card-text>
          {{ $t('Вы уверены, что хотите удалить запись?') }}
        </v-card-text>
        <v-divider></v-divider>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn text @click="skillToDelete = null;deleteDialog=false">
            {{ $t('Нет') }}
          </v-btn>
          <v-btn color="primary" @click="deleteSkill()">
            {{ $t('Да') }}
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

  </v-container>
</template>


<script lang="ts">
import Vue from 'vue'
import Component from "vue-class-component";
import {Prop, Watch} from "vue-property-decorator";
import skillsService, {Skill} from "@/components/empl/skills/skills.service";
import {SimpleDict} from "@/store/modules/dict";
import {SharedSkillName} from "@/store/modules/dict.service";
import AddSkillForm from "@/components/empl/skills/AddSkillForm.vue";
import {Getter} from "vuex-class";
import permissionService from "@/store/modules/permission.service";

interface GroupedSkills {
  groupId: number,
  groupName: string,
  skills: SkillWithMenu[]
  menu: Map<number, boolean>,
}

interface SkillWithMenu extends Skill {
  menu: boolean
}

const namespace = 'dict';
@Component({
  components: {AddSkillForm}
})
export default class SkillsChips extends Vue {
  // Immutable property. Only for first initialization
  @Prop({required: true})
  private skills!: Skill[];

  @Prop({required: true})
  private employeeId!: number;

  @Getter("skillGroups", {namespace})
  private allGroups!: Array<SimpleDict>;

  @Getter("sharedSkills", {namespace})
  private allSharedSkills!: Array<SharedSkillName>;

  private groupedSkills: GroupedSkills[] = [];

  // Mutable list of skills. Updates on create/delete operations
  private mySkills: Skill[] = [];

  private addSkillDialog = false;
  private deleteDialog = false;
  private skillToDelete: Skill | null = null;

  public created() {
    this.mySkills = [...this.skills];
    this.groupSkills();
  }

  @Watch("skills")
  private watchSkills() {
    this.mySkills = [...this.skills];
    this.groupSkills();
  }

  private groupSkills() {
    this.groupedSkills.length = 0;
    this.mySkills.forEach(skill => {
      let group = this.groupedSkills.find(g => g.groupId == skill.group.id);
      if (group) {
        group.skills.push({...skill, menu: false});
      } else {
        group = {
          groupId: skill.group.id,
          groupName: skill.group.name,
          skills: [{...skill, menu: false}],
          menu: new Map().set(skill.id, false)
        }
        this.groupedSkills.push(group);
      }
    })
  }

  private updateRating(skill: SkillWithMenu, newValue: number) {
    skillsService.updateRating(skill.id, {rating: newValue}).then((updated) => {
      skill.menu = false;
      skill.ratings = updated.ratings;
      this.$emit("submit", updated)
    });
  }

  private openAddSkillDialog() {
    this.addSkillDialog = true;
  }

  private skillAdded(newItem: Skill) {
    this.mySkills.push(newItem);
    this.groupSkills();
  }

  private skillsChipsEditable(employeeId: number): boolean {
    return permissionService.canEditSkills(employeeId);
  }

  private openDeleteSkillDialog(skill: Skill) {
    this.skillToDelete = skill;
    this.deleteDialog=true;
  }

  private deleteSkill() {
    this.deleteDialog = false;
    if (this.skillToDelete) {
      skillsService.deleteSkill(this.employeeId, this.skillToDelete.id).then(() => {
        this.mySkills = this.mySkills.filter(s => s.id != this.skillToDelete!.id);
        this.skillToDelete = null;
        this.groupSkills();
      });
    }
  }

}
</script>
