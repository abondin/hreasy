<template>
  <div>
    <v-card v-if="!preview" outlined>
      <v-card-title class="subtitle-2 grey--text text--darken-1 align-center">
        {{ label }}
        <v-spacer />
        <div class="preview-toggle" @click.stop>
          <v-switch
            v-model="preview"
            :label="$t('Предпросмотр')"
            inset
            dense
            hide-details
          />
        </div>
      </v-card-title>
      <v-divider />
      <v-card-text :style="contentStyle">
        <v-textarea
          :value="localValue"
          @input="onInput"
          rows="6"
          :aria-label="label"
          :placeholder="placeholder"
          :hint="hint"
          :rules="rules"
          :counter="counter"
          persistent-hint
        />
      </v-card-text>
    </v-card>

    <v-card v-else outlined>
      <v-card-title class="subtitle-2 grey--text text--darken-1 align-center">
        {{ label }}
        <v-spacer />
        <div class="preview-toggle" @click.stop>
          <v-switch
            v-model="preview"
            :label="$t('Предпросмотр')"
            inset
            dense
            hide-details
          />
        </div>
      </v-card-title>
      <v-divider />
      <v-card-text :style="contentStyle">
        <MarkdownTextRenderer :content="localValue" />
      </v-card-text>
      <div v-if="reservePreviewDetails" class="details-reserve"></div>
    </v-card>
  </div>
</template>

<script lang="ts">
import Vue from 'vue'
import Component from 'vue-class-component'
import { Prop, Watch } from 'vue-property-decorator'
import MarkdownTextRenderer from '@/components/shared/MarkdownTextRenderer.vue'

@Component({ components: { MarkdownTextRenderer } })
export default class MarkdownTextEditor extends Vue {
  @Prop({ type: String, default: '' }) readonly value!: string;
  @Prop({ type: String, default: 'Комментарий в Markdown' }) readonly label!: string;
  @Prop({ type: String, default: 'Введите текст: **жирный**, `код`, [ссылка](https://...)' }) readonly placeholder!: string;
  @Prop({ type: String, default: 'Поддерживается Markdown: заголовки, списки, ссылки, код.' }) readonly hint!: string;
  @Prop({ type: Array, default: () => [] }) readonly rules!: Array<(v: any) => boolean | string>;
  @Prop({ type: [Number, Boolean], default: undefined }) readonly counter!: number | boolean | undefined;
  @Prop({ type: Number, default: 240 }) readonly minHeight!: number;
  @Prop({ type: Boolean, default: false }) readonly reserveDetailsSpace!: boolean;

  // Declare as public class fields for Vue reactivity (TS useDefineForClassFields)
  preview = false;
  localValue = '';

  created() {
    this.localValue = this.value || ''
  }

  get contentStyle() {
    return { minHeight: this.minHeight + 'px' }
  }

  get reservePreviewDetails() {
    if (!this.reserveDetailsSpace) return false
    const hasRules = Array.isArray(this.rules) && this.rules.length > 0
    const hasHint = !!this.hint
    const hasCounter = this.counter !== undefined && this.counter !== false
    return hasRules || hasHint || hasCounter
  }

  @Watch('value')
  onValueChanged(v: string) {
    this.localValue = v
  }

  onInput(v: string) {
    this.localValue = v
    this.$emit('input', v)
    this.$emit('change', v)
  }
}
</script>

<style scoped>
/* No extra styles; relies on Vuetify */
.preview-toggle {
  display: flex;
  align-items: center;
}
.preview-toggle .v-input--switch {
  margin-top: 0;
}
.details-reserve {
  min-height: 22px; /* reserve space to match textarea details */
}
</style>
