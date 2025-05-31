<script setup lang="ts">
import { useAppStore } from '@/stores/app'
import type { User } from '../grpc/user/user_pb'
import Avatar from 'primevue/avatar';

defineProps<{
  user: User
}>()

const appStore = useAppStore()
</script>

<template>
  <div class="flex items-center justify-between px-4 py-3 border-b border-gray-500">
    <div class="flex justify-center items-center gap-3">
      <Avatar image="/images/default_avatar.jpg" class="bg-primary" size="large" shape="circle" />
      <div class="flex flex-col justify-center">
        <h3 class="font-semibold text-lg">{{ user.username }}</h3>
        <span class="text-xs">Offline</span>
      </div>
    </div>

    <div>
      <i
        @click="() => (appStore.panelInforOpened = !appStore.panelInforOpened)"
        style="font-size: 1.25rem"
        class="pi pi-info-circle cursor-pointer p-2 flex items-center justify-center hover:bg-gray-500 rounded-full"
      ></i>
    </div>
  </div>
</template>
