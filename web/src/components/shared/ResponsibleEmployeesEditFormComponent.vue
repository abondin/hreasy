<template>
  <v-card class="mt-2 mb-2">
    <v-card-subtitle class="d-flex">
      {{ $t('Ответственные сотрудники') }}
      <v-spacer></v-spacer>
      <v-tooltip bottom :disabled="input.filter(r=>!r.employee.active).length>0">
        <template v-slot:activator="{ on: ton, attrs: tattrs}">
          <v-btn @click="addResponsible()"
                 v-bind="tattrs" v-on="ton" icon color="primary" outlined>
            <v-icon>mdi-plus</v-icon>
          </v-btn>
        </template>
        <span>{{ $t('Добавить ответсвенного') }}</span>
      </v-tooltip>
    </v-card-subtitle>
    <v-card-text>
      <v-row v-for="(r, index) in input" v-bind:key="r.employee.id">
        <v-col>
          <v-autocomplete
              :rules="[v=>(v && v.active)  || $t('Обязательное поле')]"
              v-model="r.employee"
              return-object
              :items="allEmployees.map(e=>{ return {name:e.displayName, id: e.id, active:true};})"
              item-value="id"
              item-text="name"
              :label="$t('Сотрудник')"
          ></v-autocomplete>
        </v-col>
        <v-col>
          <v-autocomplete
              :rules="[v=>(v && v.length>0)  || $t('Обязательное поле')]"
              multiple
              v-model="r.types"
              :items="types"
              :label="$t('Зона ответсвенности')"
          ></v-autocomplete>
        </v-col>
        <v-col class="col-auto">
          <v-tooltip bottom>
            <template v-slot:activator="{ on: ton, attrs: tattrs}">
              <v-btn @click="removeResponsible(r, index)"
                     v-bind="tattrs" v-on="ton" icon>
                <v-icon>mdi-close</v-icon>
              </v-btn>
            </template>
            <span>{{ $t('Убрать ответсвенного') }}</span>
          </v-tooltip>
        </v-col>
      </v-row>
    </v-card-text>
  </v-card>
</template>

<script lang="ts">

import Component from "vue-class-component";
import Vue from 'vue'
import {Prop} from "vue-property-decorator";
import {BusinessAccountResponsibleEmployee} from "@/components/admin/business_account/admin.ba.service";
import {Employee} from "@/components/empl/employee.service";

interface ResponsibleType {
  value: string,
  text: string
}

@Component
export default class ResponsibleEmployeesChips extends Vue {

  @Prop()
  private input!: Array<BusinessAccountResponsibleEmployee>;

  @Prop({required: true})
  private allEmployees!: Employee[];

  private types: Array<ResponsibleType> = []

  /**
   * Lifecycle hook
   */
  created() {
    this.types.length = 0;
    ['technical', 'organization'].forEach(t => this.types.push({
      value: t,
      text: this.$tc('RESPONSIBLE_EMPLOYEE_TYPE.' + t)
    }))
  }

  removeResponsible(r: BusinessAccountResponsibleEmployee, index: number) {
    this.input.splice(index, 1);
  }

  addResponsible() {
    this.input.unshift(
        {
          employee: {name: '', id: -1, active: false},
          types: this.types.map(t=>t.value),
        } as BusinessAccountResponsibleEmployee);
  }

}

</script>
