<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { fetchWithAuth } from '@/utils/fetchWithAuth'
import { useAuthStore } from '@/stores/auth'
import CodeSnippetEditor from '@/components/CodeSnippetEditor.vue'

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
  codeSnippet: '',
  codeLanguage: 'plaintext',
})

const error = ref('')
const success = ref('')

// CVE lookup state
const cveLoading = ref(false)
const cveError = ref('')
interface CveInfo {
  description: string | null
  cvssScore: number | null
  cvssVector: string | null
  severity: string | null
  cweId: string | null
}
const cveInfo = ref<CveInfo | null>(null)

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

async function lookupCve() {
  const id = form.cveId.trim()
  if (!id) return
  cveLoading.value = true
  cveError.value = ''
  cveInfo.value = null

  try {
    const res = await fetchWithAuth(`http://localhost:8080/api/v1/cve?id=${encodeURIComponent(id)}`)
    const data = await res.json()
    if (res.ok && data.success) {
      const d = data.data
      cveInfo.value = {
        description: d.description,
        cvssScore: d.cvssScore,
        cvssVector: d.cvssVector,
        severity: d.severity,
        cweId: d.cweId,
      }
      // Auto-fill CWE and severity if currently empty
      if (!form.cweId && d.cweId) form.cweId = d.cweId
      if (!form.severity && d.severity) {
        const match = severityItems.find(s => s.value === d.severity.toUpperCase())
        if (match) form.severity = match.value
      }
      if (!form.description && d.description) form.description = d.description
    } else {
      cveError.value = data.message || 'CVE not found'
    }
  } catch {
    cveError.value = 'Failed to reach NVD API'
  } finally {
    cveLoading.value = false
  }
}

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
      codeSnippet: form.codeSnippet || null,
      codeLanguage: form.codeSnippet ? form.codeLanguage : null,
    }

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
      form.codeSnippet = ''
      form.codeLanguage = 'plaintext'
      cveInfo.value = null
      cveError.value = ''
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

const cvssColor = computed(() => {
  const score = cveInfo.value?.cvssScore
  if (score === null || score === undefined) return 'grey'
  if (score >= 9) return 'error'
  if (score >= 7) return 'orange'
  if (score >= 4) return 'warning'
  return 'success'
})
</script>

<template>
  <v-form @submit.prevent="handleSubmit" class="report-form mt-14">

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

    <!-- CVE ID with lookup button -->
    <div class="d-flex gap-2 align-start mb-1">
      <v-text-field
        v-model="form.cveId"
        label="CVE ID (Optional)"
        placeholder="e.g. CVE-2021-44228"
        variant="outlined"
        density="comfortable"
        hide-details
        class="flex-grow-1"
      />
      <v-btn
        variant="outlined"
        color="info"
        :loading="cveLoading"
        :disabled="!form.cveId.trim()"
        style="height: 48px; margin-top: 0"
        @click="lookupCve"
      >
        Lookup
      </v-btn>
    </div>

    <!-- CVE enrichment card -->
    <v-expand-transition>
      <v-card
        v-if="cveInfo"
        variant="tonal"
        color="info"
        class="mb-3 pa-3"
        density="compact"
      >
        <div class="d-flex align-center justify-space-between mb-1">
          <span class="text-caption text-medium-emphasis">NVD Data</span>
          <v-chip
            v-if="cveInfo.cvssScore !== null"
            :color="cvssColor"
            size="small"
            label
          >
            CVSS {{ cveInfo.cvssScore?.toFixed(1) }}
          </v-chip>
        </div>
        <div v-if="cveInfo.description" class="text-body-2 mb-1">
          {{ cveInfo.description }}
        </div>
        <div class="d-flex flex-wrap gap-2 mt-1">
          <v-chip v-if="cveInfo.cweId" size="x-small" variant="outlined">{{ cveInfo.cweId }}</v-chip>
          <v-chip v-if="cveInfo.cvssVector" size="x-small" variant="outlined" class="text-mono">
            {{ cveInfo.cvssVector }}
          </v-chip>
        </div>
      </v-card>
    </v-expand-transition>

    <v-alert v-if="cveError" type="warning" density="compact" class="mb-3" closable @click:close="cveError = ''">
      {{ cveError }}
    </v-alert>

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

    <!-- Code Snippet Editor -->
    <div class="mb-4">
      <div class="text-body-2 text-medium-emphasis mb-2">Code Snippet (Optional)</div>
      <CodeSnippetEditor
        v-model="form.codeSnippet"
        v-model:language="form.codeLanguage"
        height="220px"
      />
    </div>

    <v-btn
      type="submit"
      color="info"
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
  max-width: 560px;
  margin: 100px auto;
  padding: 30px;
  background-color: rgb(var(--v-theme-surface-variant));
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  font-family: Arial, sans-serif;
}

.text-mono {
  font-family: monospace;
}
</style>
