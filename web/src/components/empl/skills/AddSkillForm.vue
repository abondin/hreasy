<!-- Add new employee skill form-->
<template>
  <v-card>
    <v-card-title>{{ $t('Добавление навыка') }}</v-card-title>
    <v-card-text>
      <v-form ref="addSkillForm">
        <!-- Group -->
        <v-autocomplete
            autofocus
            v-model="addSkillForm.groupId"
            :items="allGroups"
            item-text="name"
            item-value="id"
            :label="$t('Группа')"
            :rules="[v=>(v ? true:false || $t('Обязательное поле'))]"
        ></v-autocomplete>

        <v-combobox
            @input.native="e => addSkillForm.name = e.target.value"
            :label="$t('Название навыка')"
            :items="filteredSkills(allSharedSkills)"
            :rules="[v=>(v && v.trim() && v.length <= 255 || $t('Обязательное поле. Не более N символов', {n:255}))]"
            v-model="addSkillForm.name">
        </v-combobox>

        <v-container class="pa-0 ma-0">
          <v-row no-gutters align="center" justify="center">
            <v-col no-gutters md="auto">
              <div class="v-label pr-5">{{ $t('Уровень') }}</div>
            </v-col>
            <v-col no-gutters>
              <v-rating
                  large
                  v-model="addSkillForm.ratingValue" empty-icon="$ratingFull"></v-rating>
            </v-col>
          </v-row>
        </v-container>

        <v-textarea
            v-model="addSkillForm.ratingNotes"
            :rules="[v=>(!v ||  v.length <= 1024 || $t('Не более N символов', {n:1024}))]"
            :label="$t('Примечание к уровню')">
        </v-textarea>
      </v-form>

      <v-alert v-if="createdSkill" type="success">
        {{ $t('Навык успешно создан', {skill: this.createdSkill}) }}
      </v-alert>
      <!-- Error block -->
      <v-alert v-if="error" type="error">
        {{ error }}
      </v-alert>
    </v-card-text>
    <v-card-actions>
      <v-checkbox v-model="addMore" :label="$t('Добавить ещё')"></v-checkbox>
      <v-spacer></v-spacer>
      <v-btn @click="closeDialog()">{{ $t('Закрыть') }}</v-btn>
      <v-btn @click="submit()" ref="submitButton" color="primary">{{ $t('Добавить') }}</v-btn>
    </v-card-actions>
  </v-card>
</template>


<script lang="ts">
import Vue from 'vue'
import Component from "vue-class-component";
import {Prop, Watch} from "vue-property-decorator";
import {errorUtils} from "@/components/errors";
import skillsService, {AddSkillBody} from "@/components/empl/skills/skills.service";
import {SimpleDict} from "@/store/modules/dict";
import {SharedSkillName} from "@/store/modules/dict.service";


class SkillForm {
  public groupId: number | null = null;
  public name = '';
  public ratingValue: number | null = null;
  public ratingNotes = '';
}

@Component(
    {}
)

export default class AddSkillForm extends Vue {
  loading = false;

  private addSkillForm = new SkillForm();

  private error: string | null = null;

  @Prop({required: true})
  private allGroups!: SimpleDict[];

  @Prop({required: true})
  private employeeId!: number;

  @Prop({required: true})
  private allSharedSkills!: SharedSkillName[];

  private addMore = true;

  private createdSkill: string | null = null;


  @Watch("input")
  private watch() {
    this.reset();
  }

  private created() {
    this.reset();
  }

  private reset(groupId?: number, createdSkill?: string) {
    this.createdSkill = createdSkill ? createdSkill : null;
    this.addSkillForm.groupId = groupId ? groupId : null;
    this.addSkillForm.name = '';
    this.addSkillForm.ratingValue = null;
    this.addSkillForm.ratingNotes = '';
  }

  private closeDialog() {
    this.reset();
    this.$nextTick(function () {
      this.$emit('close');
    })
  }

  private submit() {
    this.createdSkill = null;
    this.error = null;

    const form = this.$refs.addSkillForm as HTMLFormElement;
    if (form.validate()) {
      const body = {
        groupId: this.addSkillForm.groupId!,
        name: this.addSkillForm.name.trim(),
        rating: this.addSkillForm.ratingValue ? {
          rating: this.addSkillForm.ratingValue,
          notes: this.addSkillForm.ratingNotes
        } : null
      } as AddSkillBody;
      return skillsService.addSkill(this.employeeId, body)
          .then((result) => {
            form.reset();
            this.$nextTick(() => this.reset(body.groupId, body.name));
            this.$emit('submit', result);
            if (!this.addMore) {
              this.$emit('close');
            }
          })
          .catch(error => {
            this.error = errorUtils.shortMessage(error);
          });
    }
  }

  private filteredSkills(): string[] {
    return this.allSharedSkills.filter((v) => {
      return this.addSkillForm.groupId && v.groupId == this.addSkillForm.groupId
    }).map(v => v.name);
  }

}
</script>
