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
  <v-container class="pa-8">
    <h1 class="text-info mb-6">Settings</h1>

    <!-- Notifications -->
    <v-card variant="elevated" elevation="2" class="mb-6">
      <v-card-title>Notifications</v-card-title>
      <v-card-text>
        <v-form @submit.prevent="handleNotificationsSave" class="d-flex flex-column ga-2">
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
          <div class="d-flex ga-2">
            <v-btn
              v-if="notificationsEnabled && notificationEmail.trim()"
              color="secondary"
              @click="sendTestEmail"
            >
              Send test email
            </v-btn>
            <v-btn type="submit" color="info">Save</v-btn>
          </div>
        </v-form>
      </v-card-text>
    </v-card>

    <!-- Display -->
    <v-card variant="elevated" elevation="2" class="mb-6">
      <v-card-title>Display</v-card-title>
      <v-card-text>
        <v-form @submit.prevent="handleDisplaySave" class="d-flex flex-column ga-2">
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
          <div><v-btn type="submit" color="info">Save</v-btn></div>
        </v-form>
      </v-card-text>
    </v-card>

    <!-- Preferences -->
    <v-card variant="elevated" elevation="2" class="mb-6">
      <v-card-title>Preferences</v-card-title>
      <v-card-text>
        <v-form @submit.prevent="handlePreferencesSave" class="d-flex flex-column ga-2">
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
          <div><v-btn type="submit" color="info">Save</v-btn></div>
        </v-form>
      </v-card-text>
    </v-card>

    <!-- Logout -->
    <v-card variant="elevated" elevation="2" class="mb-6">
      <v-card-title>Logout</v-card-title>
      <v-card-text>
        <v-btn color="error" @click="handleLogoutAllSessions">
          Logout of all sessions
        </v-btn>
      </v-card-text>
    </v-card>
  </v-container>
</template>
