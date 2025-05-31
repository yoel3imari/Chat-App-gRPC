<template>
  <div class="flex items-start p-4 bg-white dark:bg-gray-800 rounded-xl shadow dark:shadow-md space-x-4">
    <i :class="['pi', icon, iconColor]" style="font-size: 2rem;"></i>
    <div>
      <div class="text-sm text-gray-500 dark:text-gray-400">{{ label }}</div>
      <div class="text-3xl font-bold">{{ value }}</div>
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  label: string
  value: string | number
  icon: string
  iconColor?: string
}>()
</script>
