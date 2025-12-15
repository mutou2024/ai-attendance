import { createApp } from 'vue'
import App from './App.vue'
import axios from 'axios'

// When running frontend on port 80 and backend on 8080, use absolute backend URL
axios.defaults.baseURL = process.env.VUE_APP_API_BASE || 'http://localhost:8080'

createApp(App).mount('#app')
