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

                    <v-text-field
                            :label="$t('Дата')"
                            v-model="item.date"
                            :rules="[v=>(!!v || $t('Дата обязательна')), v=>(Date.parse(v) > 0 || $t('Дата в формате ГГГГ-ММ-ДД'))]">
                        <template v-slot:prepend>
                            <v-btn x-small icon @click="item.date = prevDay(item.date)">
                                <v-icon>mdi-chevron-left</v-icon>
                            </v-btn>
                        </template>
                        <template v-slot:append>
                            <v-btn x-small icon @click="item.date = nextDay(item.date)">
                                <v-icon>mdi-chevron-right</v-icon>
                            </v-btn>
                        </template>
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
                    <v-checkbox v-model="addMore" :label="$t('Добавить ещё')"></v-checkbox>
                    <v-spacer></v-spacer>
                    <v-btn @click="closeDialog">{{$t('Закрыть')}}</v-btn>
                    <v-btn @click="addMore ? submitAndNext() : submit()" color="primary">{{$t('Сохранить')}}</v-btn>
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
    import moment from "moment";


    @Component
    export default class AddOvertimeItemDialog extends Vue {

        @Prop({required: true})
        employeeId!: number;

        @Prop({required: true})
        period!: ReportPeriod;

        @Prop({required: true})
        allProjects!: SimpleDict[];

        item: OvertimeItem = this.default();

        private dialog = false;

        private addMore = false;

        @Watch("dialog")
        private watch() {
            if (this.dialog) {
                this.resetItem(undefined, undefined, undefined);
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

        private submitAndNext() {
            const form: any = this.$refs[`overtime-item-update-${this.employeeId}-${this.period.periodId()}`];
            if (form.validate()) {
                return overtimeService.addItem(this.employeeId, this.period.periodId(), this.item).then((report) => {
                    this.$emit('submit', report);
                    this.$nextTick(() => {
                        this.resetItem(this.item.projectId, this.nextDay(this.item.date), this.item.hours);
                    });
                });
            }
        }

        private closeDialog() {
            this.dialog = false;
        }

        private resetItem(projectId: number | undefined = undefined, date: string | undefined = undefined,
                          hours: number | undefined) {
            const def = this.default(projectId, date, hours);
            this.item = def;
        }

        private default(projectId: number | undefined = undefined,
                        date: string | undefined = undefined,
                        hours: number = 4): OvertimeItem {
            return {
                date: date ? date : this.dateToString(new Date()),
                projectId: projectId,
                hours: hours,
                notes: undefined
            };
        }

        private nextDay(date: string): string {
            const day = new Date(date);
            let m = moment(day);
            m = m.add(1, "days");
            return this.dateToString(m.toDate());
        }

        private prevDay(date: string): string {
            const day = new Date(date);
            let m = moment(day);
            m = m.add(-1, "days");
            return this.dateToString(m.toDate());
        }

        private dateToString(date: Date) {
            return date.toISOString().substr(0, 10);
        }

    }
</script>

<style scoped>

</style>