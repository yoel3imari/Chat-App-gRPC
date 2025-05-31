<!-- src/components/MessageBubble.vue -->
<template>
  <div class="w-full" :class="['mb-2 flex', isOwn ? 'justify-end' : 'justify-start']">
    <div
      :class="[
        'rounded-lg px-2.5 py-2 text-sm max-w-[70%]',
        isOwn ? 'bg-lime-500/25 text-white' : 'bg-gray-100 text-gray-900',
      ]"
    >
      <!-- Username -->
      <div v-if="!isOwn" class="text-xs font-semibold mb-1 text-gray-600">
        {{ username }}
      </div>

      <!-- Message content -->
      <div class="message-content">
        <slot></slot>
      </div>

      <!-- Message status and time -->
      <div
        class="flex items-center justify-end gap-2 mt-1"
        :class="isOwn ? 'text-lime-500' : 'text-gray-500'"
      >
        <span v-if="isEdited" class="text-xs italic">edited</span>
        <span v-if="time" class="text-xs">{{ time }}</span>
        <span v-if="isOwn && status" class="text-xs">
          <i :class="getStatusIcon(status)" class="mr-1" style="font-size: 8px; font-weight: 900;"></i>
          <!-- {{ status }} -->
        </span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'

const props = defineProps<{
  isOwn?: boolean
  time?: string
  seen?: boolean
  messageId?: string | number
  username?: string
  isEdited?: boolean
  status?: string
}>()

const getStatusIcon = (status: string) => {
  switch (status) {
    case 'sent':
      return 'pi pi-check'
    case 'delivered':
      return 'pi pi-check-double'
    case 'read':
      return 'pi pi-check-double text-blue-500'
    default:
      return ''
  }
}

// Debug log when component is mounted
onMounted(() => {
  console.log('MessageBubble mounted:', props)
})
</script>

<style scoped>
.message-content {
  word-break: break-word;
}
</style>
