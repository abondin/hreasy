<!--
Simple form to add or update single overtime item

Emits:

1) 'submit' when item added/updated
2) 'close' when submit or cancel button pressed

 -->

<template>
    <v-form
            :ref="`overtime-item-update-${employeeId}-${period}`">
        <v-card>
            <v-card-title>{{$t('Учёт сверхурочных за день')}}</v-card-title>
            <v-card-text>
                <v-select
                        v-model="item.projectId"
                        :items="allProjects"
                        item-value="id"
                        item-text="name"
                        :rules="[v => !!v || $t('Проект обязателен')]"
                        :label="$t('Проект')"
                        required
                ></v-select>


                <!-- TODO: Simple date picker integration in form. Weird that Vuetify does not provide easy way to do it-->
                <v-text-field
                        :label="$t('Дата')"
                        v-model="item.date"
                        :rules="[v=>(!!v || $t('Дата обязательна')), v=>(Date.parse(v) > 0 || $t('Дата в формате ГГГГ-ММ-ДД'))]">
                </v-text-field>

                <v-slider
                        :label="$t('Часы')"
                        min="1"
                        max="24"
                        step="1"
                        thumbLabel="always"
                        v-model="item.hours"
                        :rules="[v=>(!!v || $t('Часы обязательны'))]">
                </v-slider>

                <!-- TODO: Add max length -->
                <v-textarea
                        v-model="item.notes"
                        :label="$t('Комментарий')">
                </v-textarea>
            </v-card-text>
            <v-card-actions>
                <v-spacer></v-spacer>
                <v-btn @click="resetOrClose">{{$t('Закрыть')}}</v-btn>
                <v-btn @click="submit" color="primary">{{$t('Добавить')}}</v-btn>
            </v-card-actions>
        </v-card>
    </v-form>
</template>


<script lang="ts">
    import Vue from 'vue'
    import Component from 'vue-class-component';
    import {Prop} from "vue-property-decorator";
    import overtimeService, {OvertimeItem} from "@/components/overtimes/overtime.service";
    import {SimpleDict} from "@/store/modules/dict";


    @Component
    export default class OvertimeAddOrEdit extends Vue {

        @Prop({required: true})
        employeeId!: number;

        @Prop({required: true})
        period!: number;

        @Prop({required: true})
        allProjects!: SimpleDict[];

        item!: OvertimeItem;

        @Prop({required: false})
        inputItem!: OvertimeItem;

        created() {
            this.item = this.inputItem ? this.inputItem : this.default();
        }

        private default(): OvertimeItem {
            return {
                date: new Date().toISOString().substr(0, 10),
                projectId: undefined,
                hours: 8,
                notes: undefined
            };
        }

        private submit() {
            const form: any = this.$refs[`overtime-item-update-${this.employeeId}-${this.period}`];
            if (form.validate()) {
                return overtimeService.addItem(this.employeeId, this.period, this.item).then((report)=>{
                    this.$emit('submit', report);
                    this.resetOrClose();
                });
            }
        }

        private resetOrClose(){
            const form: any = this.$refs[`overtime-item-update-${this.employeeId}-${this.period}`];
            this.item = this.default();
            this.$emit('close');
        }

    }
</script>

<style scoped>

</style>
