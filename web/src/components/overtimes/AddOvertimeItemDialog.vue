<!--
Simple dialog to add or update single overtime item

Emits:

1) 'submit' when item added/updated
2) 'close' when dialog closed

 -->

<template>
    <v-dialog
            max-width="800"
            v-model="dialog">
        <template v-slot:activator="{on, attrs}">
            <v-btn color="primary"
                   v-bind="attrs"
                   v-on="on">{{$t('Добавить')}}
            </v-btn>
        </template>
        <v-form v-if="item"
                :ref="`overtime-item-update-${employeeId}-${period.periodId()}`">
            <v-card>
                <v-card-title>{{$t('Учёт сверхурочных за день')}}</v-card-title>
                <v-card-text>
                    <v-select
                            v-model="item.projectId"
                            :items="allProjects.filter(p=>p.active)"
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
                    <v-btn @click="closeDialog">{{$t('Закрыть')}}</v-btn>
                    <v-btn @click="submit" color="primary">{{$t('Добавить')}}</v-btn>
                </v-card-actions>
            </v-card>
        </v-form>
    </v-dialog>
</template>


<script lang="ts">
    import Vue from 'vue'
    import Component from 'vue-class-component';
    import {Prop, Watch} from "vue-property-decorator";
    import overtimeService, {OvertimeItem, ReportPeriod} from "@/components/overtimes/overtime.service";
    import {SimpleDict} from "@/store/modules/dict";


    @Component
    export default class AddOvertimeItemDialog extends Vue {

        @Prop({required: true})
        employeeId!: number;

        @Prop({required: true})
        period!: ReportPeriod;

        @Prop({required: true})
        allProjects!: SimpleDict[];

        item!: OvertimeItem;

        private dialog = false;

        /**
         * Lifecycle hook
         */
        created() {
            this.resetItem();
        }

        @Watch("dialog")
        private watch() {
            if (this.dialog) {
                this.resetItem();
            }
        }

        private submit() {
            const form: any = this.$refs[`overtime-item-update-${this.employeeId}-${this.period.periodId()}`];
            if (form.validate()) {
                return overtimeService.addItem(this.employeeId, this.period.periodId(), this.item).then((report) => {
                    this.$emit('submit', report);
                    this.closeDialog();
                });
            }
        }

        private closeDialog() {
            this.dialog = false;
        }

        private resetItem() {
            const def = {
                date: new Date().toISOString().substr(0, 10),
                projectId: undefined,
                hours: 8,
                notes: undefined
            };
            if (this.item) {
                // Be careful. shallow copy. Doesn't work with nested objects
                this.item = Object.assign({}, def);
            } else {
                this.item = def;
            }
        }


    }
</script>

<style scoped>

</style>
