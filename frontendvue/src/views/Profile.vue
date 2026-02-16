<script setup lang="ts">
import { onMounted, ref, reactive } from 'vue'
import { fetchWithAuth } from '@/utils/fetchWithAuth'
import { useAuthStore } from '@/stores/auth'
const authStore = useAuthStore()

const user = ref<{ firstname: string; lastname: string; username: string; role: string } | null>(null)
const updateForm = reactive({ firstName: '', lastName: '' })
const email = ref('')
const passwordForm = reactive({ oldPassword: '', newPassword: '' })

onMounted(async () => {
  try {
    const res = await fetchWithAuth('http://localhost:8080/api/v1/users/profile')
    if (res.ok) {
      const data = await res.json()
      user.value = data.data
    } else {
      authStore.logout()
    }
  } catch (err) {
    console.error('Failed to load profile:', err)
    authStore.logout()
  }
})

const handleUpdate = async () => {
  try {
    const res = await fetchWithAuth('http://localhost:8080/api/v1/users/update', {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(updateForm),
    })
    if (res.ok) {
      alert('Name updated!')
      window.location.reload()
    }
  } catch (err) {
    console.error('Update failed:', err)
  }
}

const handleEmailChange = async () => {
  try {
    const res = await fetchWithAuth(`http://localhost:8080/api/v1/users/update/${email.value}`, {
      method: 'PUT',
    })
    if (res.status === 204) {
      alert('Email changed. Please log in again.')
      authStore.logoutAllSessions()
    }
  } catch (err) {
    console.error('Email change failed:', err)
  }
}

const handlePasswordChange = async () => {
  try {
    const res = await fetchWithAuth('http://localhost:8080/api/v1/users/change-password', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(passwordForm),
    })
    if (res.status === 204) {
      alert('Password changed. Please log in again.')
      authStore.logoutAllSessions()
    }
  } catch (err) {
    console.error('Password change failed:', err)
  }
}
</script>

<template>
  <div v-if="!user">
    <p>Loading profile...</p>
  </div>
  <div v-else class="profile-page">
    <h1 class="profile-title">My Profile</h1>

    <div class="profile-card">
      <p><strong>Name:</strong> {{ user.firstname }} {{ user.lastname }}</p>
      <p><strong>Email:</strong> {{ user.username }}</p>
      <p><strong>Role:</strong> {{ user.role }}</p>
    </div>

    <div class="profile-section">
      <h2>Update Name</h2>
      <v-form @submit.prevent="handleUpdate" class="profile-form">
        <v-text-field
          v-model="updateForm.firstName"
          label="First Name"
          variant="outlined"
          density="comfortable"
        />
        <v-text-field
          v-model="updateForm.lastName"
          label="Last Name"
          variant="outlined"
          density="comfortable"
        />
        <v-btn type="submit" color="info">Update</v-btn>
      </v-form>
    </div>

    <div class="profile-section">
      <h2>Change Email</h2>
      <v-form @submit.prevent="handleEmailChange" class="profile-form">
        <v-text-field
          v-model="email"
          label="New Email"
          type="email"
          variant="outlined"
          density="comfortable"
        />
        <v-btn type="submit" color="secondary">Change Email</v-btn>
      </v-form>
    </div>

    <div class="profile-section">
      <h2>Change Password</h2>
      <v-form @submit.prevent="handlePasswordChange" class="profile-form">
        <v-text-field
          v-model="passwordForm.oldPassword"
          label="Old Password"
          type="password"
          variant="outlined"
          density="comfortable"
        />
        <v-text-field
          v-model="passwordForm.newPassword"
          label="New Password"
          type="password"
          variant="outlined"
          density="comfortable"
        />
        <v-btn type="submit" color="error">Change Password</v-btn>
      </v-form>
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

.profile-card {
  background: rgb(var(--v-theme-surface));
  border: 1px solid rgb(var(--v-theme-outline));
  border-radius: 8px;
  padding: 1rem 1.5rem;
  margin-bottom: 2rem;
  box-shadow: 0 2px 6px rgba(0,0,0,0.05);
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
