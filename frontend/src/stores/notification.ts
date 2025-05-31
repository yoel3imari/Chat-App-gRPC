import type { Notification } from "@/grpc/notification/notification_pb";
import { defineStore } from "pinia";
import { ref } from "vue";

export const useNotifStore = defineStore("notification", () => {

  const notifications = ref<Notification[]>([]);
  const unreadCount = ref(0)
  function pushNotif (n: Notification) {
    notifications.value.push(n)
  }

  return {
    notifications,
    unreadCount,
    pushNotif,
  }
})
