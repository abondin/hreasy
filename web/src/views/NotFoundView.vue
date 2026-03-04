<template>
  <div class="d-flex justify-center">
    <v-card class="pa-6 skeleton-card" elevation="3">
      <v-card-title class="text-h5"> Страница не найдена </v-card-title>
      <v-card-text class="text-body-1">
        Запрошенный маршрут отсутствует в текущем каркасе Vue 3. Возможно,
        соответствующий экран ещё не перенесён из версии Vue 2.
      </v-card-text>
      <v-card-actions>
        <v-btn color="primary" variant="flat" :to="{ name: 'profile-main' }" router>
          К профилю
        </v-btn>
      </v-card-actions>
    </v-card>
  </div>
</template>

<script setup lang="ts"></script>
