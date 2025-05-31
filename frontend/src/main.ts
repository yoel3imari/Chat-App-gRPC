import './assets/main.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'
import PrimeVue from 'primevue/config'
import Aura from '@primeuix/themes/aura'
import ToastService from 'primevue/toastservice';
import ConfirmationService from 'primevue/confirmationservice'
import Tooltip from 'primevue/tooltip'


const app = createApp(App)
app.use(PrimeVue, {
  theme: {
    preset: Aura,
    options: {
      // darkModeSelector: '.my-app-dark',
    }
  },
})
app.use(ConfirmationService)
app.directive('tooltip', Tooltip);
app.use(ToastService)
app.use(createPinia())
app.use(router)

app.mount('#app')