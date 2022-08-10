<template>
  <span>
  <v-chip small class="mr-2"
          v-for="r in value" v-bind:key="r.employee.id">
    <v-tooltip bottom>
      <template v-slot:activator="{ on: ton, attrs: tattrs}">
          <div v-bind="tattrs" v-on="ton" class="col-auto">
            {{ r.employee.name }}
         </div>
      </template>
      {{$t('Зона ответсвенности')}}
      <u>
        <li v-for="t in r.types" v-bind:key="t">{{$t('RESPONSIBLE_EMPLOYEE_TYPE.'+t)}}</li>
      </u>
    </v-tooltip>
  </v-chip>
    </span>
</template>

<script lang="ts">

import Component from "vue-class-component";
import Vue from 'vue'
import {Prop} from "vue-property-decorator";
import {BusinessAccountResponsibleEmployee} from "@/components/admin/business_account/admin.ba.service";


@Component
export default class ResponsibleEmployeesChips extends Vue {

  @Prop()
  private value!: Array<BusinessAccountResponsibleEmployee>;


}

</script>
