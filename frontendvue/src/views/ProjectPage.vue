<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { fetchWithAuth } from '@/utils/fetchWithAuth'
import { useAuthStore } from '@/stores/auth'
import Report from '@/views/Report.vue'
const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const projectId = route.params.projectId as string

interface ProjectData {
  name: string
  description: string
  createdAt: string
  updatedAt: string
  myRole: string
}

interface Member {
  id: string
  firstname: string
  lastname: string
  projectRole: string
}

interface Vulnerability {
  id: string
  title: string
  severity: string
  status: string
}

const project = ref<ProjectData | null>(null)
const members = ref<Member[]>([])
const vulnerabilities = ref<Vulnerability[]>([])
const loadingVulns = ref(true)
const showReportForm = ref(false)
const newMemberUserId = ref('')
const newMemberRole = ref('')
const memberActionLoading = ref(false)
const role = ref('')

const memberRoleItems = [
  { title: 'Tester', value: 'TESTER' },
  { title: 'Programmer', value: 'PROGRAMMER' },
  { title: 'Lead', value: 'LEAD' },
]

const loadProject = async () => {
  try {
    const res = await fetchWithAuth(
      `http://localhost:8080/api/v1/userprojects/${projectId}`
    )
    if (!res.ok) {
      authStore.logout()
      return
    }
    const data = await res.json()
    project.value = data.data
    role.value = data.data.myRole
  } catch (err) {
    authStore.logout()
  }
}

const loadMembers = async () => {
  try {
    const res = await fetchWithAuth(
      `http://localhost:8080/api/v1/userprojects/members/${projectId}`
    )
    const data = await res.json()
    members.value = data.data || []
  } catch (err) {
    authStore.logout()
  }
}

const loadVulnerabilities = async () => {
  try {
    const res = await fetchWithAuth(
      `http://localhost:8080/api/v1/vulnerabilities/all/${projectId}`
    )
    const data = await res.json()
    vulnerabilities.value = data.data || []
  } catch (err) {
    authStore.logout()
  } finally {
    loadingVulns.value = false
  }
}

onMounted(() => {
  loadProject()
  loadMembers()
  loadVulnerabilities()
  localStorage.setItem('lastProjectId', projectId)
})

const handleSelfRemove = async () => {
  try {
    const res = await fetchWithAuth(
      `http://localhost:8080/api/v1/userprojects/self-remove/validate/${projectId}`,
      { method: 'POST' }
    )
    const data = await res.json()

    if (data.data?.requiresConfirmation) {
      alert(data.data.message)
      return
    }

    await fetchWithAuth(
      `http://localhost:8080/api/v1/userprojects/self-remove/${projectId}`,
      { method: 'DELETE' }
    )
    router.push('/projects')
  } catch (err) {
    authStore.logout()
  }
}

const handleDeleteProject = async () => {
  const confirmed = window.confirm(
    'Are you sure? This will permanently delete the entire project.'
  )
  if (!confirmed) return

  try {
    await fetchWithAuth(
      `http://localhost:8080/api/v1/projects/delete/${projectId}`,
      { method: 'DELETE' }
    )
    router.push('/projects')
  } catch (err) {
    authStore.logout()
  }
}

const handleDeleteVulnerability = async (vulnerabilityId: string) => {
  const confirmed = window.confirm('Delete this vulnerability?')
  if (!confirmed) return

  try {
    const res = await fetchWithAuth(
      `http://localhost:8080/api/v1/vulnerabilities/delete/${projectId}/${vulnerabilityId}`,
      { method: 'DELETE' }
    )
    if (!res.ok) {
      const data = await res.json()
      alert(data.message || 'Failed to delete vulnerability.')
      return
    }
    loadVulnerabilities()
  } catch (err: unknown) {
    if (err instanceof Error) {
      console.error('Delete vulnerability failed:', err.message)
    }
    alert('Network or authentication error while deleting.')
    authStore.logout()
  }
}

const handleStatusChange = async (vulnerabilityId: string, newStatus: string) => {
  try {
    const res = await fetchWithAuth(
      'http://localhost:8080/api/v1/vulnerabilities/transition',
      {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          projectId,
          vulnerabilityId,
          status: newStatus
        })
      }
    )
    if (!res.ok) {
      const data = await res.json()
      alert(data.message || 'Failed to update vulnerability status.')
      return
    }
    loadVulnerabilities()
  } catch (err: unknown) {
    if (err instanceof Error) {
      console.error('Status change failed:', err.message)
    }
    alert('Network or authentication error while updating status.')
    authStore.logout()
  }
}

const handleAddMember = async () => {
  if (!newMemberUserId.value) {
    alert('Enter a user ID.')
    return
  }

  try {
    memberActionLoading.value = true

    const res = await fetchWithAuth(
      'http://localhost:8080/api/v1/userprojects/members/add',
      {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          projectId,
          userId: newMemberUserId.value,
          role: newMemberRole.value
        })
      }
    )

    const data = await res.json()

    if (data.data?.requiresConfirmation) {
      const confirmed = window.confirm(data.data.message)
      if (!confirmed) return

      await fetchWithAuth(
        'http://localhost:8080/api/v1/userprojects/members/add',
        {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            projectId,
            userId: newMemberUserId.value,
            role: newMemberRole.value
          })
        }
      )
    }

    newMemberUserId.value = ''
    newMemberRole.value = 'TESTER'
    loadMembers()
  } catch (err) {
    alert('Failed to add member.')
  } finally {
    memberActionLoading.value = false
  }
}

const handleRemoveMember = async (userId: string) => {
  try {
    const validateRes = await fetchWithAuth(
      'http://localhost:8080/api/v1/userprojects/members/remove/validate',
      {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ projectId, userId })
      }
    )

    const validateData = await validateRes.json()

    if (validateData.data?.requiresConfirmation) {
      const confirmed = window.confirm(validateData.data.message)
      if (!confirmed) return
    }

    await fetchWithAuth(
      'http://localhost:8080/api/v1/userprojects/members/remove',
      {
        method: 'DELETE',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ projectId, userId })
      }
    )

    loadMembers()
  } catch (err) {
    alert('Failed to remove member.')
  }
}

const handleOpenAssignments = () => {
  window.alert('Coming soon!')
}

const handleChangeMemberRole = async (userId: string, currentRole: string) => {
  let nextRole: string

  if (currentRole === 'TESTER') nextRole = 'PROGRAMMER'
  else if (currentRole === 'PROGRAMMER') nextRole = 'LEAD'
  else if (currentRole === 'LEAD') nextRole = 'PROGRAMMER'
  else nextRole = 'PROGRAMMER'

  const confirmed = window.confirm(
    `Change user role from ${currentRole} → ${nextRole}?`
  )
  if (!confirmed) return

  try {
    const res = await fetchWithAuth(
      'http://localhost:8080/api/v1/userprojects/change',
      {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          projectId,
          userId,
          newRole: nextRole
        })
      }
    )

    if (!res.ok) {
      const data = await res.json()
      alert(data.message || 'Failed to update role.')
      return
    }

    loadMembers()
  } catch (err) {
    alert('Failed to update role.')
  }
}
</script>

<template>
  <p v-if="!project">Loading...</p>
  <template v-else>
    <div class="project-container-wide">
      <!-- HEADER ROW -->
      <div class="project-header-wide">
        <div class="project-title-group">
          <h1>{{ project.name }}</h1>
          <p class="project-description">
            {{ project.description || 'No description provided.' }}
          </p>
          <div class="timestamps">
            <span>Created: {{ new Date(project.createdAt).toLocaleDateString() }}</span>
            <span>
              Last Updated:
              {{ project.updatedAt ? new Date(project.updatedAt).toLocaleDateString() : '—' }}
            </span>
          </div>
        </div>

        <div class="project-meta-actions">
          <div class="role-badge">My Role: {{ role }}</div>

          <div class="project-actions">
            <template v-if="role === 'LEAD'">
              <v-btn
                color="success"
                @click="router.push(`/project/update/${projectId}`)"
              >
                Update Project
              </v-btn>
              <v-btn color="error" @click="handleDeleteProject">
                Delete Project
              </v-btn>
            </template>

            <v-btn color="leave" @click="handleSelfRemove">
              Leave Project
            </v-btn>
          </div>
        </div>
      </div>

      <!-- MAIN GRID -->
      <div class="project-grid-2col">
        <!-- LEFT COLUMN (VULNERABILITIES) -->
        <div class="left-column">
          <div class="section-card tall-card">
            <h2>Vulnerabilities</h2>

            <v-btn
              color="info"
              class="mb-4"
              @click="showReportForm = !showReportForm"
            >
              + Report Vulnerability
            </v-btn>

            <div v-if="showReportForm" class="report-section">
              <Report
                :project-id="projectId"
                :on-success="() => {
                  loadVulnerabilities()
                  showReportForm = false
                }"
              />
            </div>

            <p v-if="loadingVulns" style="margin-top: 10px">Loading vulnerabilities...</p>
            <p v-else-if="vulnerabilities.length === 0" style="margin-top: 10px; color: rgb(var(--v-theme-secondary))">
              No vulnerabilities reported yet.
            </p>
            <ul v-else class="vuln-list">
              <li v-for="v in vulnerabilities" :key="v.id" class="vuln-card">
                <div class="vuln-main">
                  <strong>{{ v.title }}</strong>
                  <span :class="['severity', v.severity.toLowerCase()]">
                    {{ v.severity }}
                  </span>
                </div>

                <div class="vuln-sub">
                  <span :class="['status', v.status.toLowerCase()]">
                    {{ v.status }}
                  </span>
                </div>

                <div class="vuln-actions">
                  <v-btn
                    size="small"
                    variant="tonal"
                    color="grey"
                    @click="handleStatusChange(v.id, 'PATCHED')"
                  >
                    Patch
                  </v-btn>

                  <v-btn
                    size="small"
                    variant="tonal"
                    color="grey"
                    @click="handleStatusChange(v.id, 'VERIFIED')"
                  >
                    Verify
                  </v-btn>

                  <v-btn
                    v-if="role === 'LEAD'"
                    size="small"
                    variant="tonal"
                    color="grey"
                    @click="handleDeleteVulnerability(v.id)"
                  >
                    Delete
                  </v-btn>
                </div>
              </li>
            </ul>
          </div>
        </div>

        <!-- RIGHT COLUMN (STACKED) -->
        <div class="right-column">
          <!-- MEMBERS -->
          <div class="section-card">
            <h2>Project Members</h2>

            <ul class="member-list">
              <li v-for="m in members" :key="m.id" class="member-card">
                <div class="member-name">
                  {{ m.firstname }} {{ m.lastname }}
                </div>

                <div v-if="role === 'LEAD'" class="member-actions">
                  <v-btn
                    size="small"
                    variant="tonal"
                    color="blue"
                    @click="handleChangeMemberRole(m.id, m.projectRole)"
                  >
                    Change Role
                  </v-btn>

                  <v-btn
                    size="small"
                    variant="tonal"
                    color="red"
                    @click="handleRemoveMember(m.id)"
                  >
                    Remove
                  </v-btn>
                </div>

                <span class="member-role fixed-role">{{ m.projectRole }}</span>
              </li>
            </ul>

            <!-- ADD MEMBER UI (LEAD ONLY) -->
            <template v-if="role === 'LEAD'">
              <h3 style="margin-top: 14px">Add Member</h3>

              <div class="add-member-row">
                <v-text-field
                  v-model="newMemberUserId"
                  label="User ID"
                  variant="outlined"
                  density="compact"
                  hide-details
                  class="member-input"
                />

                <v-select
                  v-model="newMemberRole"
                  :items="memberRoleItems"
                  label="Role"
                  variant="outlined"
                  density="compact"
                  hide-details
                  class="member-input"
                />
              </div>

              <v-btn
                color="info"
                class="mt-3 mb-4"
                @click="handleAddMember"
                :disabled="memberActionLoading"
              >
                {{ memberActionLoading ? 'Adding...' : 'Add Member' }}
              </v-btn>
            </template>
          </div>

          <!-- ASSIGNMENTS -->
          <div class="section-card">
            <h2>Assignments</h2>
            <p>View, self-assign, and manage vulnerability roles.</p>
            <v-btn
              color="info"
              class="mt-2"
              @click="handleOpenAssignments"
            >
              Open Assignments
            </v-btn>
          </div>
        </div>
      </div>
    </div>
  </template>
</template>

<style scoped>
/* PAGE WRAPPER */
.project-container-wide {
  padding: 2rem;
  color: rgb(var(--v-theme-on-surface));
}

/* HEADER */
.project-header-wide {
  display: grid;
  grid-template-columns: 1fr auto;
  grid-template-rows: auto auto auto;
  column-gap: 32px;
  row-gap: 10px;
  margin-bottom: 25px;
}

.project-title-group {
  grid-column: 1 / 2;
  grid-row: 1 / 4;
  max-width: 65%;
}

.project-description {
  margin-top: 6px;
  font-size: 1rem;
  color: rgb(var(--v-theme-on-surface-variant));
}

.timestamps {
  margin-top: 6px;
  display: flex;
  gap: 15px;
  font-size: 0.9rem;
  color: rgb(var(--v-theme-secondary));
}

/* RIGHT COLUMN */
.project-meta-actions {
  grid-column: 2 / 3;
  grid-row: 1 / 4;
  display: grid;
  grid-template-rows: auto auto auto;
  row-gap: 10px;
  justify-items: end;
}

.role-badge {
  grid-row: 6;
  background: rgb(var(--v-theme-surface));
  padding: 6px 14px;
  border: 1px solid rgb(var(--v-theme-outline));
  font-weight: 700;
  font-size: 0.9rem;
  border-radius: 999px;
}

.project-actions {
  grid-row: 8;
  display: flex;
  gap: 10px;
}

/* CARDS */
.section-card {
  background: rgb(var(--v-theme-surface));
  border: 1px solid rgb(var(--v-theme-outline));
  padding: 20px;
  border-radius: 12px;
}

.section-card h2 {
  margin-top: 10px;
  margin-bottom: 10px;
}

/* TWO-COLUMN GRID */
.project-grid-2col {
  display: grid;
  grid-template-columns: 1fr 1.2fr;
  gap: 24px;
  margin-top: 25px;
}

.left-column {
  display: flex;
}

.tall-card {
  width: 100%;
  min-height: 260px;
}

.right-column {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* MEMBERS */
.member-list {
  list-style: none;
  padding: 0;
}

.member-card {
  display: grid;
  grid-template-columns: 1fr auto auto;
  align-items: center;
  background: rgb(var(--v-theme-surface-light));
  padding: 10px 14px;
  border-radius: 8px;
  margin-bottom: 10px;
  border: 1px solid rgb(var(--v-theme-outline));
  min-height: 48px;
  column-gap: 12px;
}

.member-name {
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  font-size: clamp(0.8rem, 1vw, 1rem);
}

.member-actions {
  display: flex;
  gap: 8px;
}

.fixed-role {
  justify-self: end;
  color: rgb(var(--v-theme-secondary));
  font-size: 0.95rem;
  font-weight: bold;
  white-space: nowrap;
}

.add-member-row {
  display: flex;
  gap: 10px;
  margin-top: 10px;
}

.member-input {
  max-width: 200px;
}

/* VULNERABILITIES */
.vuln-list {
  list-style: none;
  padding: 0;
  margin-top: 12px;
}

.vuln-card {
  background: rgb(var(--v-theme-surface-variant));
  border: 1px solid rgb(var(--v-theme-outline));
  border-radius: 10px;
  padding: 12px;
  margin-bottom: 10px;
}

.vuln-main {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.vuln-sub {
  margin-top: 8px;
  font-size: 0.85rem;
  color: rgb(var(--v-theme-secondary));
}

.severity {
  padding: 3px 10px;
  border-radius: 999px;
  font-size: 0.75rem;
  font-weight: bold;
}

.severity.low { background: rgb(var(--v-theme-severity-low)); color: rgb(var(--v-theme-on-severity-low)); }
.severity.medium { background: rgb(var(--v-theme-severity-medium)); color: rgb(var(--v-theme-on-severity-medium)); }
.severity.high { background: rgb(var(--v-theme-severity-high)); color: rgb(var(--v-theme-on-severity-high)); }
.severity.critical { background: rgb(var(--v-theme-severity-critical)); color: rgb(var(--v-theme-on-severity-critical)); }

.status {
  font-weight: bold;
  text-transform: uppercase;
}

.status.reported { color: rgb(var(--v-theme-info)); }
.status.in_progress { color: rgb(var(--v-theme-status-in-progress)); }
.status.patched { color: rgb(var(--v-theme-status-patched)); }
.status.verified { color: rgb(var(--v-theme-status-verified)); }
.status.under_review { color: rgb(var(--v-theme-warning)); }

.vuln-actions {
  margin-top: 8px;
  display: flex;
  gap: 8px;
}

.report-section form {
  margin: 6px auto 40px auto !important;
  padding: 5px !important;
}

.report-section input,
.report-section textarea {
  margin: 6px 0 !important;
}

</style>
