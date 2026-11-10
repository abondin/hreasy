<template>
  <span class="d-inline-flex align-center flex-wrap ga-1">
    <span class="text-break">{{ email || t('Не задан') }}</span>
    <v-btn
      v-if="email"
      :icon="copiedEmail === email ? 'mdi-check' : 'mdi-content-copy'"
      variant="text"
      size="x-small"
      :aria-label="`${copyLabel(email)}: ${email}`"
      @click.stop="copy(email)"
    />
    <v-menu v-if="additionalEmails.length" :close-on-content-click="false">
      <template #activator="{ props: menuProps }">
        <v-chip
          v-bind="menuProps"
          size="small"
          :aria-label="t('Дополнительные почтовые адреса')"
          data-testid="employee-additional-emails"
          @click.stop
        >+{{ additionalEmails.length }}</v-chip>
      </template>
      <v-list data-testid="employee-emails-list" max-width="480">
        <v-list-item v-for="address in emails" :key="address">
          <template #title><span class="text-wrap text-break">{{ address }}</span></template>
          <template #append>
            <v-btn
              :icon="copiedEmail === address ? 'mdi-check' : 'mdi-content-copy'"
              variant="text"
              size="small"
              :aria-label="`${copyLabel(address)}: ${address}`"
              @click.stop="copy(address)"
            />
          </template>
        </v-list-item>
      </v-list>
    </v-menu>
    <v-snackbar v-model="copyFailed">{{ t('Не удалось скопировать адрес') }}</v-snackbar>
  </span>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import { useI18n } from "vue-i18n";

const props = withDefaults(defineProps<{
  email?: string | null;
  additionalEmails?: string[];
}>(), { email: null, additionalEmails: () => [] });

const { t } = useI18n();
const emails = computed(() => [props.email, ...props.additionalEmails]
  .filter((email): email is string => Boolean(email)));
const copiedEmail = ref<string | null>(null);
const copyFailed = ref(false);

function copyLabel(email?: string | null) {
  return t(email && copiedEmail.value === email ? 'Скопировано в буфер обмена' : 'Скопировать в буфер обмена');
}

async function copy(email?: string | null) {
  if (!email) return;
  try {
    await navigator.clipboard.writeText(email);
    copiedEmail.value = email;
  } catch {
    copyFailed.value = true;
  }
}
</script>
