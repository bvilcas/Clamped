<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const form = reactive({
  email: '',
  password: ''
})

const error = ref('')

const handleSubmit = async () => {
  try {
    const res = await fetch('http://localhost:8080/api/v1/auth/login', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      credentials: 'include',
      body: JSON.stringify(form)
    })

    if (!res.ok) throw new Error('Login failed')

    const data = await res.json()
    authStore.login(data.accessToken)

    router.replace('/dashboard')
  } catch (err) {
    console.error(err)
    error.value = 'Could not login. Try again.'
  }
}
</script>

<template>
  <div class="login-form">
    <h2>Login</h2>
    <v-form @submit.prevent="handleSubmit">
      <v-text-field
        v-model="form.email"
        label="Email"
        type="email"
        variant="outlined"
        density="comfortable"
        required
      />
      <v-text-field
        v-model="form.password"
        label="Password"
        type="password"
        variant="outlined"
        density="comfortable"
        required
      />
      <v-btn
        type="submit"
        color="primary"
        block
        size="large"
        class="mt-2"
      >
        Login
      </v-btn>
      <v-alert v-if="error" type="error" density="compact" class="mt-3">
        {{ error }}
      </v-alert>
    </v-form>
  </div>
</template>

<style scoped>
.login-form {
  max-width: 400px;
  margin: 100px auto;
  padding: 30px;
  background-color: rgb(var(--v-theme-surface-variant));
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  font-family: Arial, sans-serif;
}

.login-form h2 {
  text-align: center;
  margin-top: 0;
  margin-bottom: 20px;
  color: rgb(var(--v-theme-on-surface));
}
</style>
