<template>
  <div class="d-flex justify-center">
    <v-card class="pa-6 skeleton-card" elevation="3">
      <v-card-title class="text-h5">
        Добро пожаловать в каркас Vue 3
      </v-card-title>
      <v-card-text class="text-body-1">
        <p>
          Этот временный каркас создан для портирования HREasy на Vue 3 + Vite + Vuetify 3.
          Постепенно переносите существующие экраны из версии Vue 2,
          заменяя их новыми <code>setup</code>-компонентами и Pinia сторами.
        </p>
        <p class="mb-0">
          Начните с создания composable-слоя для работы с API и авторизацией, затем переключайте реальные страницы.
        </p>
      </v-card-text>
      <v-card-actions>
        <v-btn color="primary" variant="flat" :to="{ name: 'legacy-status' }">
          Посмотреть прогресс миграции
        </v-btn>
      </v-card-actions>
    </v-card>
  </div>
</template>

<script setup lang="ts">
</script>
