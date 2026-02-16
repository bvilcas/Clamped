<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { fetchWithAuth } from '@/utils/fetchWithAuth'
import { useAuthStore } from '@/stores/auth'
const router = useRouter()
const authStore = useAuthStore()

interface ProjectWithRole {
  id: string
  name: string
  description: string
  myRole: string
  createdAt: string
}

const projects = ref<ProjectWithRole[]>([])

onMounted(async () => {
  try {
    const res = await fetchWithAuth('http://localhost:8080/api/v1/userprojects/all')

    if (res.ok) {
      const data = await res.json()
      console.log('PROJECT RESPONSE:', data)
      console.log('FIRST PROJECT:', data.data?.[0])
      projects.value = data.data || []
    }
  } catch (err: unknown) {
    if (err instanceof Error) {
      console.error('Failed to load projects:', err.message)
    }
    authStore.logout()
  }
})
</script>

<template>
  <div class="projects-page">
    <div class="project-title-row">
      <h1 class="project-title">My Projects</h1>
    </div>

    <p v-if="projects.length === 0" class="empty">No projects found.</p>

    <ul v-else class="project-list">
      <li
        v-for="p in projects"
        :key="p.id"
        class="project-card"
        @click="router.push(`/project/${p.id}`)"
      >
        <h3>{{ p.name }}</h3>
        <p>{{ p.description || 'No description provided.' }}</p>
        <div class="meta">
          <span><strong>Role:</strong> {{ p.myRole }}</span>
          <span v-if="p.createdAt">
            <strong>Created:</strong> {{ new Date(p.createdAt).toLocaleDateString() }}
          </span>
        </div>
      </li>
    </ul>
  </div>
</template>

<style scoped>
.projects-page {
  padding: 2rem;
  color: rgb(var(--v-theme-on-surface));
}

.project-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1.5rem;
}

.project-title {
  font-size: 1.7rem;
  font-weight: 700;
  margin: 0;
  color: rgb(var(--v-theme-on-surface));
}

.project-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 1rem;
}

.project-card {
  background: rgb(var(--v-theme-surface));
  border: 1px solid rgb(var(--v-theme-outline));
  border-radius: 10px;
  padding: 1rem;
  box-shadow: 0 2px 6px rgba(0,0,0,0.05);
  display: flex;
  flex-direction: column;
  gap: 0.6rem;
  cursor: pointer;
  transition: transform 0.15s ease, box-shadow 0.15s ease;
}

.project-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.project-card h3 {
  font-size: 1.2rem;
  margin: 0;
}

.project-card p {
  font-size: 0.95rem;
  color: rgb(var(--v-theme-on-surface-variant));
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
