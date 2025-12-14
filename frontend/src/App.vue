<template>
  <div style="padding:20px;font-family:Arial,Helvetica,sans-serif">
    <h1>AI Attendance — Demo</h1>
    <CalendarView :userId="1" />
  </div>
</template>

<script setup>
import CalendarView from './components/CalendarView.vue'
</script>
