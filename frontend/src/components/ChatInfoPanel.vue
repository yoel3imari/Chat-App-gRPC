<!-- src/components/ChatInfoPanel.vue -->
<template>
  <div
    class="transition-all duration-300 ease-in-out overflow-hidden"
    :class="appStore.panelInforOpened ? 'w-72' : 'w-0'"
  >
    <div
      class="h-full bg-white dark:bg-gray-800 border-l border-gray-500 p-4 overflow-y-auto"
      :class="{ 'w-72': appStore.panelInforOpened }"
    >
      <div class="flex flex-col h-full">
        <div class="flex-1 flex flex-col items-center gap-2 mb-2">
          <img src="/images/default_avatar.jpg" class="w-24 h-24 mb-4 rounded-full" />
          <div>
            <div class="font-semibold text-xl mb-6">
              @{{ chatStore.currentChat?.username }}
            </div>
          </div>
        </div>

        <div class="flex flex-col gap-2">
          <Button
            @click="confirmReport"
            icon="pi pi-trash"
            label="Delete this conversaion"
            severity="warn"
            class="w-full"
          />
          <Button
            @click="confirmReport"
            icon="pi pi-flag"
            label="Report this user"
            severity="danger"
            class="w-full"
          />
        </div>
      </div>
    </div>
  </div>
  <ConfirmDialog />
</template>

<script setup lang="ts">
import Button from 'primevue/button'
import ConfirmDialog from 'primevue/confirmdialog'
import { useConfirm } from 'primevue/useconfirm'
import { useAppStore } from '@/stores/app'
import { useChatStore } from '@/stores/chat'

const appStore = useAppStore()
const chatStore = useChatStore()
const confirm = useConfirm()

function confirmReport() {
  confirm.require({
    message: 'Are you sure you want to report this account',
    header: 'Confirmation',
    icon: 'pi pi-exclamation-triangle',
    acceptLabel: 'Yes, report it',
    rejectLabel: "No, don't",
    acceptClass: 'p-button-danger', // Red button
    rejectClass: 'p-button-outlined p-button-secondary', // Outlined grey button
    accept: () => {
      // handle confirm
      console.log('Reported')
    },
    reject: () => {
      // handle reject
      console.log('Cancelled')
    },
  })
}
</script>
