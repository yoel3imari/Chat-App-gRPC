<script setup lang="ts">
import { useAuthStore } from '@/stores/auth';


const {user} = useAuthStore()
</script>

<template>
  <div class="flex items-center justify-center h-full">
    <h1
      class="text-6xl text-center flex flex-wrap items-center justify-center gap-2 font-bold bg-gradient-to-r from-lime-100 to-lime-600 bg-clip-text text-transparent"
    >
      <span>Hello, {{ user?.username }}</span>
    </h1>
  </div>
</template>
