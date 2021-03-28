<!-- List of skills for given employee-->
<template>
  <v-container>
    <v-row no-gutters>
      <v-col class="mr-5" cols="auto" v-for="g in groupedSkills" v-bind:key="g.groupId">
        {{ g.groupName }}
        <div v-for="s in g.skills" v-bind:key="s.id">
          <v-menu v-model="s.menu" bottom right transition="scale-transition" origin="top left">
            <template v-slot:activator="{ on }">
              <v-chip v-on="on" class="mr-2">{{ s.name }} ({{ s.averageRating }})
              </v-chip>
            </template>
            <v-sheet>
              <v-rating half :value="s.averageRating" @input="v=>updateRating(s, v)"></v-rating>
              <span v-if="s.averageRating">{{ $t('Текущий рейтинг') }}: {{
                  s.averageRating
                }} ({{ $tc('ratingCounts', s.ratingsCount) }} )</span>
            </v-sheet>
          </v-menu>
        </div>
      </v-col>
    </v-row>
  </v-container>
</template>


<script lang="ts">
import Vue from 'vue'
import Component from "vue-class-component";
import {Prop, Watch} from "vue-property-decorator";
import skillsService, {Skill} from "@/components/empl/skills/skills.service";

interface GroupedSkills {
  groupId: number,
  groupName: string,
  skills: SkillWithMenu[]
  menu: Map<number, boolean>,
}

interface SkillWithMenu extends Skill {
  menu: boolean
}

@Component(
    {}
)
export default class AddSkillForm extends Vue {
  @Prop({required: true})
  private skills!: Skill[];

  private groupedSkills: GroupedSkills[] = [];

  public created() {
    this.groupSkills();
  }

  @Watch("skills")
  private watchSkills() {
    this.groupSkills();
  }

  private groupSkills() {
    this.groupedSkills.length = 0;
    this.skills.forEach(skill => {
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
    skillsService.updateRating(skill.id, {rating: newValue}).then(() => {
      skill.menu = false;
      this.$emit("submit")
    });
  }
}
</script>
