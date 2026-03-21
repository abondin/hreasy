<!--
  Shared detail-page layout for admin entities with back navigation, primary
  summary/content card, and stacked secondary sections.
-->
<template>
  <v-container class="py-6" :data-testid="testId">
      <v-btn
        variant="text"
        prepend-icon="mdi-arrow-left"
        :to="backTo"
      >
        {{ backLabel }}
      </v-btn>

      <v-alert
        v-if="error"
        type="error"
        variant="tonal"
        border="start"
        class="mt-4"
      >
        {{ error }}
      </v-alert>

      <v-card v-if="showPrimaryCard" class="mt-4 pa-6" :data-testid="cardTestId">
        <div class="d-flex align-start ga-4 flex-wrap mb-5">
          <div class="flex-grow-1 min-width-0">
            <div v-if="kicker" class="text-overline text-medium-emphasis">
              {{ kicker }}
            </div>
            <div class="text-h6 font-weight-semibold">
              {{ title }}
            </div>
            <div v-if="subtitle" class="text-body-2 text-medium-emphasis mt-1">
              {{ subtitle }}
            </div>
          </div>

          <div class="d-flex align-center ga-2">
            <slot name="leading-actions" />
            <slot name="trailing-actions" />
          </div>
        </div>

        <v-row align="start" class="ga-4">
          <v-col cols="12" lg="auto">
            <slot name="summary" />
          </v-col>

          <v-col cols="12" lg>
            <slot name="content" />
          </v-col>
        </v-row>
      </v-card>

      <div v-if="$slots.default" class="mt-5 d-flex flex-column ga-5">
        <slot />
      </div>
  </v-container>
</template>

<script setup lang="ts">
import type { RouteLocationRaw } from "vue-router";

withDefaults(defineProps<{
  backTo: RouteLocationRaw;
  backLabel: string;
  title: string;
  kicker?: string;
  subtitle?: string;
  error?: string;
  testId: string;
  cardTestId?: string;
  showPrimaryCard?: boolean;
}>(), {
  error: "",
  kicker: "",
  subtitle: "",
  cardTestId: undefined,
  showPrimaryCard: true,
});
</script>
