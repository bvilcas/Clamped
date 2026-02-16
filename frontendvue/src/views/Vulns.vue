<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { fetchWithAuth } from '@/utils/fetchWithAuth'
import { useAuthStore } from '@/stores/auth'
import type { VulnerabilityDTO } from '@/dto/vulnerability-dto'
const authStore = useAuthStore()

const reported = ref<VulnerabilityDTO[]>([])
const assigned = ref<VulnerabilityDTO[]>([])
const verified = ref<VulnerabilityDTO[]>([])

onMounted(async () => {
  try {
    const base = 'http://localhost:8080/api/v1/uservulns'
    const [rep, asg, ver] = await Promise.all([
      fetchWithAuth(`${base}/reported`),
      fetchWithAuth(`${base}/assigned`),
      fetchWithAuth(`${base}/verified`),
    ])

    if (rep.ok) reported.value = (await rep.json()).data || []
    if (asg.ok) assigned.value = (await asg.json()).data || []
    if (ver.ok) verified.value = (await ver.json()).data || []
  } catch (err) {
    console.error('Failed to fetch vulnerabilities:', err)
    authStore.logout()
  }
})
</script>

<template>
  <div class="vulns-page">
    <div class="vuln-title-row">
      <h1 class="vuln-title">My Vulnerabilities</h1>
    </div>

    <section class="vuln-section">
      <h2>Reported by Me</h2>
      <p v-if="reported.length === 0" class="empty">None found.</p>
      <ul v-else class="vuln-list">
        <li v-for="v in reported" :key="v.id" class="vuln-card">
          <h3>{{ v.title }}</h3>
          <p>{{ v.description || 'No description provided.' }}</p>
          <div class="meta">
            <span><strong>Severity:</strong> {{ v.severity }}</span>
            <span><strong>Status:</strong> {{ v.status }}</span>
            <span v-if="v.dueAt"><strong>Due:</strong> {{ new Date(v.dueAt).toLocaleDateString() }}</span>
          </div>
        </li>
      </ul>
    </section>

    <section class="vuln-section">
      <h2>Assigned to Me</h2>
      <p v-if="assigned.length === 0" class="empty">None found.</p>
      <ul v-else class="vuln-list">
        <li v-for="v in assigned" :key="v.id" class="vuln-card">
          <h3>{{ v.title }}</h3>
          <p>{{ v.description || 'No description provided.' }}</p>
          <div class="meta">
            <span><strong>Severity:</strong> {{ v.severity }}</span>
            <span><strong>Status:</strong> {{ v.status }}</span>
            <span v-if="v.dueAt"><strong>Due:</strong> {{ new Date(v.dueAt).toLocaleDateString() }}</span>
          </div>
        </li>
      </ul>
    </section>

    <section class="vuln-section">
      <h2>Verified by Me</h2>
      <p v-if="verified.length === 0" class="empty">None found.</p>
      <ul v-else class="vuln-list">
        <li v-for="v in verified" :key="v.id" class="vuln-card">
          <h3>{{ v.title }}</h3>
          <p>{{ v.description || 'No description provided.' }}</p>
          <div class="meta">
            <span><strong>Severity:</strong> {{ v.severity }}</span>
            <span><strong>Status:</strong> {{ v.status }}</span>
            <span v-if="v.dueAt"><strong>Due:</strong> {{ new Date(v.dueAt).toLocaleDateString() }}</span>
          </div>
        </li>
      </ul>
    </section>
  </div>
</template>

<style scoped>
.vulns-page {
  padding: 2rem;
  color: rgb(var(--v-theme-on-surface));
}

.vuln-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1.5rem;
}

.vuln-title {
  font-size: 1.7rem;
  font-weight: 700;
  margin: 0;
  color: rgb(var(--v-theme-on-surface));
}

.vuln-section {
  margin-bottom: 2rem;
}

.vuln-section h2 {
  font-size: 1.3rem;
  margin-bottom: 0.8rem;
  color: rgb(var(--v-theme-info));
}

.vuln-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 1rem;
}

.vuln-card {
  background: rgb(var(--v-theme-surface));
  border: 1px solid rgb(var(--v-theme-outline));
  border-radius: 10px;
  padding: 1rem;
  box-shadow: 0 2px 6px rgba(0,0,0,0.05);
  display: flex;
  flex-direction: column;
  gap: 0.6rem;
  transition: transform 0.15s ease;
}

.vuln-card:hover {
  transform: translateY(-3px);
}

.vuln-card h3 {
  font-size: 1.1rem;
  margin: 0;
}

.vuln-card p {
  font-size: 0.95rem;
  color: rgb(var(--v-theme-on-surface-variant));
  flex-grow: 1;
}

.meta {
  font-size: 0.85rem;
  color: rgb(var(--v-theme-secondary));
  display: flex;
  flex-direction: column;
  gap: 0.2rem;
}

.empty {
  font-size: 0.95rem;
  color: rgb(var(--v-theme-secondary));
}
</style>
