<template>
  <div class="d-flex justify-center">
    <v-card class="pa-6 skeleton-card" elevation="3">
      <v-card-title class="text-h5">
        Статус миграции компонентов
      </v-card-title>
      <v-card-text class="text-body-1">
        <p>
          Используйте этот экран как живой чек-лист: отмечайте завершённые переносы и фиксируйте проблемные зоны.
        </p>
        <v-divider class="my-4" />
        <v-list density="comfortable">
          <v-list-item
            v-for="item in migrationItems"
            :key="item.title"
          >
            <template #prepend>
              <v-icon :color="item.done ? 'success' : 'warning'">
                {{ item.done ? 'mdi-check-circle-outline' : 'mdi-progress-clock' }}
              </v-icon>
            </template>
            <v-list-item-title>
              {{ item.title }}
            </v-list-item-title>
            <v-list-item-subtitle>
              {{ item.description }}
            </v-list-item-subtitle>
          </v-list-item>
        </v-list>
      </v-card-text>
    </v-card>
  </div>
</template>

<script setup lang="ts">
import {computed} from 'vue';

interface MigrationItem {
  title: string;
  description: string;
  done: boolean;
}

const migrationItems = computed<MigrationItem[]>(() => [
  {
    title: 'Каркас приложения (App + Router + Vuetify)',
    description: 'Базовый Vue 3 Vite проект с Vuetify 3 и Pinia зарегистрирован.',
    done: true
  },
  {
    title: 'Бэк-офис (админка)',
    description: 'Перенос экранов управления сотрудниками, проектами и словарями.',
    done: false
  },
  {
    title: 'HR потоки (отпуска, овертаймы, зарплатные заявки)',
    description: 'Компоненты, завязанные на сложные таблицы и moment.js.',
    done: false
  },
  {
    title: 'Интеграции (Telegram, загрузка файлов, аватары)',
    description: 'Требуется подобрать современные аналоги для uploader/clipboard/cropper.',
    done: false
  },
  {
    title: 'I18n и локализация',
    description: 'Миграция на vue-i18n 9 с ассинхронной загрузкой словарей.',
    done: false
  }
]);
</script>
