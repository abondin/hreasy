<template>
  <div class="profile-summary__item">
    <span class="profile-summary__label">{{ label }}:</span>
    <span class="profile-summary__value">
      <slot />
    </span>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  label: string;
}>();
</script>

<style>
.profile-summary__item {
  font-size: 0.92rem;
  color: rgba(0, 0, 0, 0.72);
  margin-bottom: 4px;
}

.profile-summary__label {
  font-weight: 600;
  margin-right: 4px;
}

.profile-summary__value {
  color: rgba(0, 0, 0, 0.86);
}
</style>
