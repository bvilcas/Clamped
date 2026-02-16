<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { fetchWithAuth } from '@/utils/fetchWithAuth'
import { useAuthStore } from '@/stores/auth'
const props = defineProps<{
  projectId?: string
  onSuccess?: () => void
}>()

const route = useRoute()
const authStore = useAuthStore()

const routeProjectId = route.params.projectId as string | undefined
const resolvedProjectId = ref(props.projectId || routeProjectId || '')

interface ProjectItem {
  id: string
  name: string
}

const projects = ref<ProjectItem[]>([])

const form = reactive({
  title: '',
  description: '',
  cveId: '',
  cweId: '',
  severity: '',
  status: '',
  dueAt: '',
  repository: '',
  commitHash: '',
})

const error = ref('')
const success = ref('')

onMounted(async () => {
  if (!resolvedProjectId.value) {
    try {
      const res = await fetchWithAuth('http://localhost:8080/api/v1/userprojects/all')
      if (res.ok) {
        const data = await res.json()
        projects.value = data.data || []
      }
    } catch (err: unknown) {
      if (err instanceof Error) {
        console.error('Failed to load projects:', err.message)
      }
      authStore.logout()
    }
  }
})

const handleSubmit = async () => {
  error.value = ''
  success.value = ''

  if (!resolvedProjectId.value) {
    error.value = 'Please select a project first.'
    return
  }

  try {
    const payload = {
      ...form,
      dueAt: form.dueAt
        ? new Date(form.dueAt + 'T00:00:00').toISOString()
        : null,
    }

    console.log('Sending payload:', payload)
    console.log('Sending token:', localStorage.getItem('accessToken'))

    const res = await fetchWithAuth(
      `http://localhost:8080/api/v1/vulnerabilities/report/${resolvedProjectId.value}`,
      {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(payload),
      }
    )

    if (res.ok) {
      success.value = 'Vulnerability reported successfully!'
      form.title = ''
      form.description = ''
      form.cveId = ''
      form.cweId = ''
      form.severity = ''
      form.status = ''
      form.dueAt = ''
      form.repository = ''
      form.commitHash = ''
      if (props.onSuccess) props.onSuccess()
    } else {
      const data = await res.json()
      error.value = data.message || 'Failed to report vulnerability'
    }
  } catch (err: unknown) {
    if (err instanceof Error) {
      console.error('Report fetch failed:', err.message)
    }
    error.value = 'Network or authentication error'
    authStore.logout()
  }
}

const severityItems = [
  { title: 'Low', value: 'LOW' },
  { title: 'Medium', value: 'MEDIUM' },
  { title: 'High', value: 'HIGH' },
  { title: 'Critical', value: 'CRITICAL' },
]

const statusItems = [
  { title: 'Reported', value: 'REPORTED' },
  { title: 'In Progress', value: 'IN_PROGRESS' },
  { title: 'Patched', value: 'PATCHED' },
  { title: 'Under Review', value: 'UNDER_REVIEW' },
  { title: 'Verified', value: 'VERIFIED' },
]

const projectItems = computed(() =>
  projects.value.map(p => ({ title: p.name, value: p.id }))
)
</script>

<template>
  <v-form @submit.prevent="handleSubmit" class="report-form">

    <!-- PROJECT DROPDOWN (ONLY WHEN NOT PRESELECTED) -->
    <v-select
      v-if="!props.projectId"
      v-model="resolvedProjectId"
      :items="projectItems"
      label="Project *"
      variant="outlined"
      density="comfortable"
      required
    />

    <v-text-field
      v-model="form.title"
      label="Title *"
      variant="outlined"
      density="comfortable"
      required
    />

    <v-text-field
      v-model="form.description"
      label="Description (Optional)"
      variant="outlined"
      density="comfortable"
    />

    <v-text-field
      v-model="form.cveId"
      label="CVE ID (Optional)"
      variant="outlined"
      density="comfortable"
    />

    <v-text-field
      v-model="form.cweId"
      label="CWE ID (Optional)"
      variant="outlined"
      density="comfortable"
    />

    <v-select
      v-model="form.severity"
      :items="severityItems"
      label="Severity *"
      variant="outlined"
      density="comfortable"
      required
    />

    <v-select
      v-model="form.status"
      :items="statusItems"
      label="Status *"
      variant="outlined"
      density="comfortable"
      required
    />

    <v-text-field
      v-model="form.dueAt"
      label="Due At *"
      type="date"
      variant="outlined"
      density="comfortable"
      required
    />

    <v-text-field
      v-model="form.repository"
      label="Repository (Optional)"
      variant="outlined"
      density="comfortable"
    />

    <v-text-field
      v-model="form.commitHash"
      label="Commit Hash (Optional)"
      variant="outlined"
      density="comfortable"
    />

    <v-btn
      type="submit"
      color="primary"
      block
      size="large"
      class="mt-2"
    >
      Submit
    </v-btn>

    <v-alert v-if="error" type="error" density="compact" class="mt-3">
      {{ error }}
    </v-alert>
    <v-alert v-if="success" type="success" density="compact" class="mt-3">
      {{ success }}
    </v-alert>
  </v-form>
</template>

<style scoped>
.report-form {
  max-width: 400px;
  margin: 100px auto;
  padding: 30px;
  background-color: rgb(var(--v-theme-surface-variant));
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  font-family: Arial, sans-serif;
}
</style>
