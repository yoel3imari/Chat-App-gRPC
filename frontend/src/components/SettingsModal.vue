<script setup lang="ts">
import { useAppStore } from '@/stores/app'
import Dialog from 'primevue/dialog'
import { ref } from 'vue'
import InputText from 'primevue/inputtext'
import InputSwitch from 'primevue/inputswitch'
import Button from 'primevue/button'
import { useAuthStore } from '@/stores/auth'

const appStore = useAppStore()
const authStore = useAuthStore()

const settings = ref({
  notifications: true,
  username: 'yusef',
  password: '',
})


const disableAccount = () => {
  console.log('Disabling account...')
  // confirm + disable logic
}
</script>

<template>
  <Dialog
    v-model:visible="appStore.settingsModalVisible"
    modal
    header="Settings"
    :style="{ width: '50vw' }"
    :breakpoints="{ '1199px': '75vw', '575px': '90vw' }"
  >
    <div class="max-w-xl mx-auto space-y-6">
      <h2 class="text-2xl font-semibold text-gray-800 dark:text-white">Settings</h2>

      <!-- Notifications Switch -->
      <div class="flex items-center justify-between">
        <label class="text-sm text-gray-700 dark:text-gray-300">Enable Browser Notifications</label>
        <InputSwitch v-model="settings.notifications" />
      </div>

      <!-- Username -->
      <div>
        <label class="block text-sm text-gray-700 dark:text-gray-300 mb-1">Username</label>
       <div class="flex items-center gap-2">
        <InputText v-model="settings.username" class="w-full h-10"  />
        <Button icon="pi pi-refresh" class="h-10" />
       </div>
      </div>

      <!-- Password -->
      <!-- <div>
        <label class="block text-sm text-gray-700 dark:text-gray-300 mb-1">Change Password</label>
        <Password v-model="settings.password" toggleMask class="w-full" :feedback="false" />
      </div> -->

      <!-- Danger Zone -->
      <div class="border-t border-gray-200 dark:border-gray-700 pt-4 space-y-3">
        <Button
          label="Disable Account"
          icon="pi pi-user-minus"
          severity="danger"
          outlined
          class="w-full"
          @click="disableAccount"
        />
        <Button
          label="Logout"
          icon="pi pi-sign-out"
          severity="secondary"
          class="w-full"
          @click="authStore.logout"
        />
      </div>
    </div>
  </Dialog>
</template>
