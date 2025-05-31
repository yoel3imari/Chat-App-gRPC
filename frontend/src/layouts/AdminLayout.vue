<template>
  <div class="flex h-screen overflow-hidden">
    <!-- Mobile menu backdrop -->
    <div
      class="md:hidden fixed inset-0 z-30 bg-black bg-opacity-50"
      v-if="sidebarOpen"
      @click="sidebarOpen = false"
    />

    <!-- Sidebar -->
    <aside
      :class="[
        'fixed h-full z-40 md:static md:translate-x-0 transform transition-transform duration-300 ease-in-out',
        sidebarOpen ? 'translate-x-0' : '-translate-x-full',
      ]"
      class="w-60 bg-white dark:bg-gray-800 border-r border-gray-500 p-4"
    >
      <div class="text-xl font-bold mb-12">Admin Dashboard</div>
      <ul class="space-y-8">
        <li v-for="item in menu" :key="item.label">
          <RouterLink
            :to="item.to"
            @click="sidebarOpen = false"
            class="flex items-center gap-2 text-sm text-gray-700 dark:text-gray-200 hover:text-blue-600"
          >
            <i :class="item.icon" class="text-2xl" style="font-size: 1.5rem" />
            <span class="">{{ item.label }}</span>
          </RouterLink>
        </li>
      </ul>
    </aside>

    <!-- Main content -->
    <div class="flex-1 flex flex-col">
      <!-- Header -->
      <header
        class="h-16 bg-white dark:bg-gray-800 dark:text-gray-200 border-b border-gray-500 flex items-center justify-between px-6"
      >
        <!-- Sidebar toggle (mobile) -->
        <div class="flex items-center justify-between gap-4">
          <div class="md:hidden">
            <Button icon="pi pi-bars" class="p-button-text p-button-sm" @click="toggleSidebar" />
          </div>
          <span class="text-sm text-gray-700 dark:text-gray-200">Hello, {{ user.name }}</span>
        </div>
        <div class="flex items-center gap-4">
          <Button
            icon="pi pi-sign-out"
            label="Logout"
            @click="auth.logout"
            class="p-button-sm p-button-text text-red-500"
          />
        </div>
      </header>

      <!-- Page content -->
      <main class="flex-1 p-6 bg-gray-50 dark:bg-gray-900 overflow-auto">
        <RouterView />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { RouterLink, RouterView } from 'vue-router'
import Button from 'primevue/button'
import { ref } from 'vue'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()

const sidebarOpen = ref(false)


const toggleSidebar = () => {
  console.log('toggling')

  sidebarOpen.value = !sidebarOpen.value
}

const user = {
  name: 'Youssef',
}

const menu = [
  { label: 'Overview', to: '/admin/dashboard', icon: 'pi pi-home' },
  { label: 'Users', to: '/admin/dashboard/users', icon: 'pi pi-users' },
  { label: 'Reports', to: '/admin/dashboard/reports', icon: 'pi pi-flag' },
  { label: 'Settings', to: '/admin/dashboard/settings', icon: 'pi pi-cog' },
]

</script>
