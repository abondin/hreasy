<template>
  <v-card class="legacy-feature-card" color="warning" variant="tonal">
    <v-card-title class="d-flex align-center">
      <v-icon v-if="iconToUse" :icon="iconToUse" class="mr-2" />
      <span>{{ title }}</span>
    </v-card-title>
    <v-card-text>
      <slot>
        <p v-if="description" class="text-body-2 text-medium-emphasis mb-0">
          {{ description }}
        </p>
      </slot>
    </v-card-text>
    <v-card-actions v-if="href">
      <v-btn
        :href="href"
        target="_blank"
        rel="noopener"
        variant="text"
        color="warning"
        prepend-icon="mdi-open-in-new"
      >
        {{ ctaLabel }}
      </v-btn>
    </v-card-actions>
  </v-card>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { useI18n } from "vue-i18n";

const props = defineProps<{
  title: string;
  description?: string;
  href?: string;
  icon?: string;
  ctaLabelKey?: string;
}>();

const { t } = useI18n();

const ctaLabel = computed(() =>
  t(props.ctaLabelKey ?? "Открыть_legacy_версию"),
);
const iconToUse = computed(() => props.icon ?? "mdi-progress-clock");
</script>

<style scoped>
.legacy-feature-card {
  border-radius: 16px;
}
</style>
