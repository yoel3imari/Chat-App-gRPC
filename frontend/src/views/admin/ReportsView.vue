<template>
  <div class="space-y-4">
    <!-- Header and Global Search -->
    <div class="flex justify-start gap-2">
        <InputText placeholder="Search users..." class="w-72 pe-4" />
        <Button icon="pi pi-search" ></Button>
    </div>

    <!-- DataTable -->
    <!-- :filters="filters" -->
    <DataTable
      :value="reports"
      :globalFilterFields="['reporter', 'reported', 'status']"
      filterDisplay="menu"
      :paginator="true"
      :rows="5"
      :rowHover="true"
      class="w-full"
    >
      <Column field="id" header="ID" />
      <Column field="reporter" header="Reporter" />
      <Column field="reported" header="Reported" />
      <Column field="status" header="Status">
        <template #body="{ data }">
          <Tag
            :value="capitalize(data.status)"
            :severity="getStatusSeverity(data.status)"
          />
        </template>
      </Column>
      <Column header="Actions">
        <template #body="{ data }">
          <Button icon="pi pi-ellipsis-v" class="p-button-text" @click="openMenu($event, data)" />
        </template>
      </Column>
    </DataTable>

    <!-- Actions Menu -->
    <Menu ref="menu" :model="menuItems" popup />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import InputText from 'primevue/inputtext'
import Tag from 'primevue/tag'
import Menu from 'primevue/menu'
import Button from 'primevue/button'

const reports = ref([
  { id: 1, reporter: 'Alice', reported: 'Bob', status: 'pending' },
  { id: 2, reporter: 'Yusef', reported: 'John', status: 'resolved' },
  { id: 3, reporter: 'Eva', reported: 'Mike', status: 'dismissed' }
])

const menu = ref()
const selectedReport = ref(null)

const menuItems = [
  { label: 'View Details', icon: 'pi pi-eye', command: () => console.log('View', selectedReport.value) },
  { label: 'Mark as Resolved', icon: 'pi pi-check', command: () => console.log('Resolve', selectedReport.value) },
  { label: 'Dismiss Report', icon: 'pi pi-times', command: () => console.log('Dismiss', selectedReport.value) }
]

const openMenu = (event: Event, report: any) => {
  selectedReport.value = report
  menu.value.show(event)
}

const getStatusSeverity = (status: string) => {
  switch (status) {
    case 'pending':
      return 'warning'
    case 'resolved':
      return 'success'
    case 'dismissed':
      return 'danger'
    default:
      return 'info'
  }
}

const capitalize = (s: string) => s.charAt(0).toUpperCase() + s.slice(1)
</script>
