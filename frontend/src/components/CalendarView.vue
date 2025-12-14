<template>
  <div>
    <div style="display:flex;gap:10px;align-items:center;margin-bottom:12px;">
      <button @click="prevMonth">Prev</button>
      <div>{{ year }} - {{ month }}</div>
      <button @click="nextMonth">Next</button>
    </div>
    <div style="display:grid;grid-template-columns:repeat(7,1fr);gap:6px">
      <div v-for="d in days" :key="d.date" style="border:1px solid #ddd;padding:8px;min-height:64px;">
        <div style="font-weight:600">{{ new Date(d.date).getDate() }}</div>
        <div style="font-size:12px;color:#666">{{ weekdayName(d.weekday) }}</div>
        <div style="margin-top:6px;color:#0b7;">{{ formatHours(d.seconds) }}</div>
        <div v-if="d.entryCount>0" style="background:#007bff;color:#fff;border-radius:10px;padding:2px 6px;display:inline-block;font-size:12px;margin-top:6px">{{ d.entryCount }} entries</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

const props = defineProps({ userId: { type: Number, required: true } })
const now = new Date()
const year = ref(now.getFullYear())
const month = ref(now.getMonth() + 1)
const days = ref([])

function load() {
  axios.get(`/api/users/${props.userId}/calendar`, { params: { year: year.value, month: month.value }})
    .then(r => { days.value = r.data.days })
    .catch(e => { console.error(e) })
}

onMounted(load)

function prevMonth() { if (month.value===1) { month.value=12; year.value-- } else month.value--; load() }
function nextMonth() { if (month.value===12) { month.value=1; year.value++ } else month.value++; load() }
function formatHours(seconds) { return (seconds/3600).toFixed(2) + ' h' }
function weekdayName(n) { // n is 1-7
  const names = ['Mon','Tue','Wed','Thu','Fri','Sat','Sun']; return names[n-1] || '' }
</script>
