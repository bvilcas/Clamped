<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { applySettings, loadSettings, saveSettings } from '@/utils/settingsStorage'

const authStore = useAuthStore()

const notificationsEnabled = ref(false)
const notificationEmail = ref('')
const displayMode = ref('system')
const textSize = ref('medium')
const defaultPage = ref('projects')
const autoOpenLastProject = ref(true)

onMounted(() => {
  const settings = loadSettings()
  notificationsEnabled.value = settings.notificationsEnabled
  notificationEmail.value = settings.notificationEmail
  displayMode.value = settings.displayMode
  textSize.value = settings.textSize
  defaultPage.value = settings.defaultPage
  autoOpenLastProject.value = settings.autoOpenLastProject
  applySettings(settings)
})

const buildSettings = () => ({
  notificationsEnabled: notificationsEnabled.value,
  notificationEmail: notificationEmail.value,
  displayMode: displayMode.value,
  textSize: textSize.value,
  defaultPage: defaultPage.value,
  autoOpenLastProject: autoOpenLastProject.value,
})

const handleNotificationsSave = () => {
  if (notificationsEnabled.value && !notificationEmail.value.trim()) {
    alert('Please add an email address for notifications.')
    return
  }
  const next = buildSettings()
  saveSettings(next)
  alert('Notification settings saved.')
}

const handleDisplaySave = () => {
  const next = buildSettings()
  saveSettings(next)
  applySettings(next)
  alert('Display settings saved.')
}

const handlePreferencesSave = () => {
  const next = buildSettings()
  saveSettings(next)
  alert('Preferences saved.')
}

const handleLogoutAllSessions = async () => {
  const confirmed = window.confirm('Log out of all sessions?')
  if (confirmed) {
    await authStore.logoutAllSessions()
  }
}

const sendTestEmail = () => {
  window.location.href = `mailto:${notificationEmail.value}?subject=Clamped%20Alerts`
}

const displayModeItems = [
  { title: 'System', value: 'system' },
  { title: 'Light', value: 'light' },
  { title: 'Dark', value: 'dark' },
]

const textSizeItems = [
  { title: 'Small text', value: 'small' },
  { title: 'Medium text', value: 'medium' },
  { title: 'Large text', value: 'large' },
]

const defaultPageItems = [
  { title: 'Default to Projects', value: 'projects' },
  { title: 'Default to Reports', value: 'reports' },
  { title: 'Default to Dashboard', value: 'dashboard' },
]
</script>

<template>
  <div class="profile-page">
    <h1 class="profile-title">Settings</h1>

    <div class="profile-section">
      <h2>Notifications</h2>
      <v-form @submit.prevent="handleNotificationsSave" class="profile-form">
        <v-checkbox
          v-model="notificationsEnabled"
          label="Enable alerts"
          density="compact"
          hide-details
        />
        <v-text-field
          v-model="notificationEmail"
          label="Alert email address"
          type="email"
          variant="outlined"
          density="comfortable"
          :disabled="!notificationsEnabled"
        />
        <v-btn
          v-if="notificationsEnabled && notificationEmail.trim()"
          color="secondary"
          @click="sendTestEmail"
        >
          Send test email
        </v-btn>
        <v-btn type="submit" color="info">Save</v-btn>
      </v-form>
    </div>

    <div class="profile-section">
      <h2>Display</h2>
      <v-form @submit.prevent="handleDisplaySave" class="profile-form">
        <v-select
          v-model="displayMode"
          :items="displayModeItems"
          label="Theme"
          variant="outlined"
          density="comfortable"
        />
        <v-select
          v-model="textSize"
          :items="textSizeItems"
          label="Text Size"
          variant="outlined"
          density="comfortable"
        />
        <v-btn type="submit" color="secondary">Save</v-btn>
      </v-form>
    </div>

    <div class="profile-section">
      <h2>Preferences</h2>
      <v-form @submit.prevent="handlePreferencesSave" class="profile-form">
        <v-select
          v-model="defaultPage"
          :items="defaultPageItems"
          label="Default Page"
          variant="outlined"
          density="comfortable"
        />
        <v-checkbox
          v-model="autoOpenLastProject"
          label="Auto-open last project"
          density="compact"
          hide-details
        />
        <v-btn type="submit" color="secondary">Save</v-btn>
      </v-form>
    </div>

    <div class="profile-section">
      <h2>Logout</h2>
      <v-btn color="error" @click="handleLogoutAllSessions">
        Logout of all sessions
      </v-btn>
    </div>
  </div>
</template>

<style scoped>
.profile-page {
  padding: 2rem;
  max-width: 700px;
  margin: 0 auto;
  font-family: Arial, sans-serif;
  color: rgb(var(--v-theme-on-surface));
}

.profile-title {
  font-size: 2rem;
  margin-bottom: 1rem;
}

.profile-section {
  margin-bottom: 2rem;
}

.profile-section h2 {
  font-size: 1.2rem;
  margin-bottom: 0.5rem;
}

.profile-form {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}
</style>
