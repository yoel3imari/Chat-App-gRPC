<template>
  <div class="space-y-6">
    <div>
      <h2 class="text-4xl font-bold">Users</h2>
    </div>
    <!-- Stats Cards -->
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
      <StatCard
        v-for="(st, n) in stats"
        :key="n"
        :label="st.label"
        :value="st.value"
        :icon="st.icon"
        :iconColor="st.iconColor"
      />
    </div>

    <!-- Users filter -->
    <div class="flex justify-start gap-2">
      <InputText placeholder="Search users..." class="w-72 pe-4" />
      <Button icon="pi pi-search"></Button>
    </div>

    <!-- Users Table -->
    <!-- :paginator="true" -->
    <DataTable
      class="rounded-lg"
      :value="users"
      responsiveLayout="scroll"
      :paginator="true"
      :rows="rows"
      :first="first"
      :totalRecords="total"
      :lazy="true"
      :loading="loading"
      @page="onPageChange"
    >
      <Column field="id" header="ID" />
      <Column field="username" header="Username" />
      <Column field="email" header="Email" />
      <Column field="isEmailVerified" header="Email Verified">
        <template #body="{ data }">
          <Tag
            :value="data.isEmailVerified ? 'Yes' : 'No'"
            :severity="data.isEmailVerified ? 'success' : 'warning'"
          />
        </template>
      </Column>
      <Column field="isSuspended" header="Suspended">
        <template #body="{ data }">
          <Tag
            :value="data.isSuspended ? 'Yes' : 'No'"
            :severity="data.isSuspended ? 'danger' : 'success'"
          />
        </template>
      </Column>
      <Column field="isActivated" header="Activated">
        <template #body="{ data }">
          <Tag
            :value="data.isActivated ? 'Yes' : 'No'"
            :severity="data.isActivated ? 'success' : 'secondary'"
          />
        </template>
      </Column>
      <Column header="Actions">
        <template #body="{ data }">
          <Button icon="pi pi-ellipsis-v" @click="showMenu($event, data)" class="p-button-text" />
        </template>
      </Column>
    </DataTable>

    <!-- Shared Menu -->
    <Menu ref="menu" :model="menuItems" popup />
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Tag from 'primevue/tag'
import Menu from 'primevue/menu'
import Button from 'primevue/button'
import StatCard from '@/components/StatCard.vue'
import InputText from 'primevue/inputtext'
import { useUserService } from '@/composables/useUserService'
import { useToast } from 'primevue/usetoast'
import type { User } from '@/grpc/user/user_pb'
import { useAuthStore } from '@/stores/auth'
import { useNotificationService } from '@/composables/useNotifService'

const toast = useToast()
const userService = useUserService()
const notifService = useNotificationService();
const authStore = useAuthStore()

const users = ref<User[]>([])
const stats = ref()
const total = ref(0)
const loading = ref(false)
const first = ref(0) // index of first row
const rows = ref(3) // rows per page
const selectedUser = ref<User | null>(null)
const menu = ref()

onMounted(async () => {
  loading.value = true

  try {
    // Fetch users
    const res = await userService.listUsers({ page: 1, limit: 10 })
    if (!res.success) throw new Error(res.message)
    users.value = res.users
    total.value = res.total

    // Fetch stats
    const statsRes = await userService.getUserStats({})
    if (!statsRes.success) throw new Error(statsRes.message)

    stats.value = [
      {
        label: 'Total Users',
        value: statsRes.total,
        icon: 'pi-users',
        iconColor: 'text-blue-500',
      },
      {
        label: 'Active',
        value: statsRes.active,
        icon: 'pi-check-circle',
        iconColor: 'text-lime-500',
      },
      {
        label: 'Suspended',
        value: statsRes.suspended,
        icon: 'pi-ban',
        iconColor: 'text-red-500',
      },
      {
        label: 'Unverified email',
        value: statsRes.unverified,
        icon: 'pi-envelope',
        iconColor: 'text-amber-500',
      },
    ]
  } catch (error: any) {
    toast.add({
      severity: 'error',
      detail: error.message || error,
    })
  } finally {
    loading.value = false
  }
})

const loadUsers = async () => {
  loading.value = true
  try {
    const page = first.value / rows.value + 1
    const res = await userService.listUsers({ page, limit: rows.value })

    if (!res.success) throw new Error(res.message)

    users.value = res.users
    total.value = res.total
  } catch (error: any) {
    toast.add({ severity: 'error', detail: error.message || error })
  } finally {
    loading.value = false
  }
}

const onPageChange = (event: any) => {
  first.value = event.first
  loadUsers()
}

const showMenu = (event: Event, user: User) => {
  selectedUser.value = user
  menu.value.show(event)
}

const menuItems = ref([
  // {
  //   label: 'Delete',
  //   icon: 'pi pi-trash',
  //   command: () => {
  //     if (selectedUser.value) {
  //       deleteUser(selectedUser.value)
  //     }
  //   },
  // },
  {
    label: 'Toggle suspend',
    icon: 'pi pi-ban',
    command: () => {
      if (selectedUser.value) {
        suspendUser(selectedUser.value)
      }
    },
  },
  {
    label: 'Send warning',
    icon: 'pi pi-info-circle',
    command: () => {
      if (selectedUser.value) {
        warnUser(selectedUser.value)
      }
    },
  },
])

const suspendUser = async (user: User) => {
  try {
    const res = await userService.suspendUser({
      userId: user.id.toString(),
    })

    toast.add({ severity: res.success ? 'success' : 'error', detail: res.message })

    // Optionally reload users
    await loadUsers()
  } catch (err: any) {
    toast.add({ severity: 'error', detail: err.message || err })
  }
}


const warnUser = async (user: User) => {
  try {
    if (!authStore.user) return

    await notifService.sendNotification({
      receiverId: user.id.toString(),
      senderId: authStore.user.id.toString(),
      content: 'You have violated our policy. Please adhere to the rules.',
      title: "Warning",
    })
  } catch (err: any) {
    toast.add({ severity: 'error', detail: err.message || err })
  }
}
</script>
