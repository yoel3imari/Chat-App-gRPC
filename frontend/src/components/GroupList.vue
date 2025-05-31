<template>
  <div class="flex items-center mb-4">
    <InputText v-model="search" placeholder="Search..." class="w-full h-10" />
    <Divider layout="vertical"></Divider>
    <Button icon="pi pi-plus" class="h-9 shrink-0" variant="outlined"></Button>
  </div>
  <ul class="flex flex-col gap-2">
    <!-- <ChatItem
        v-for="conversation in filteredConversations"
        :key="conversation.id.toString()"
        :conversation="conversation"
        :is-active="currentConversation?.id === conversation.id"
        :current-user-id="currentUserId"
        :last-message="getLastMessage(conversation)"
        :other-user-name="conversationNames.get(conversation.id.toString()) || 'Loading...'"
        @click="handleConversationSelect(conversation)"
      /> -->
  </ul>
</template>

<script setup lang="ts">
import InputText from 'primevue/inputtext'
import { ref } from 'vue'
import Button from 'primevue/button'
import Divider from 'primevue/divider'

const search = ref('')

</script>
