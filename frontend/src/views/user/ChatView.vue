<template>
  <!-- <MainLayout> -->
    <div class="flex h-screen w-full">
      <ChatWindow />
      <ChatInfoPanel />
    </div>
  <!-- </MainLayout> -->
</template>

<script setup lang="ts">
import ChatInfoPanel from '@/components/ChatInfoPanel.vue'
import ChatWindow from '@/components/ChatWindow.vue'
// onMounted(() => {
//   appStore.setActivePanel("chat")
// })
</script>

<style lang="scss"></style>
