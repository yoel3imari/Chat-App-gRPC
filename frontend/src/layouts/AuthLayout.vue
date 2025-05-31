<template>
  <div
    class="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-100 via-purple-100 to-pink-100 dark:from-gray-800 dark:via-gray-900 dark:to-black p-4"
  >
  <div>

  </div>
    <Card class="w-full max-w-md shadow-xl rounded-xl overflow-hidden bg-white dark:bg-gray-700">
      <template #header>
        <div class="p-6 text-center">
          <h1 class="text-2xl font-bold text-gray-800 dark:text-gray-100">{{ title }}</h1>
          <p v-if="subtitle" class="text-sm text-gray-500 dark:text-gray-400 mt-1">
            {{ subtitle }}
          </p>
        </div>
      </template>
      <template #content>
        <div class="px-6 pb-6">
          <slot></slot>
        </div>
      </template>
      <template #footer>
        <div class="px-6 pb-4 text-center text-sm text-gray-500 dark:text-gray-400">
          <slot name="footer"></slot>
        </div>
      </template>
    </Card>
  </div>
</template>

<script setup lang="ts">
import Card from 'primevue/card'

// Props to customize the title/subtitle per page (Login/Register)
defineProps({
  title: {
    type: String,
    default: 'Authentication',
  },
  subtitle: {
    type: String,
    default: '',
  },
})
</script>
