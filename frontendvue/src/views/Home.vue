<script setup lang="ts">
import { onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';

const router = useRouter();
const authStore = useAuthStore();

const goToRegister = () => {
    router.push('/register');
};

const goToLogin = () => {
    router.push('/login');
};

onMounted(async () => {
    await authStore.initAuth(); // make sure state is set first
});

// React useEffect equivalent
watch(
  () => authStore.isAuthenticated,
  (isAuthenticated) => {
    // pinia auto unwraps refs, so we can use it directly
    if (isAuthenticated === true) {
      router.replace('/dashboard')
    }
  },
  { immediate: true }
)
</script>

<template>
  <div class="home-card">
    <h2>Clamped!</h2>
    <p class="home-subtitle">Your one-stop solution for vulnerability management. Built with Vue.js and Vuetify.</p>
    <div class="home-actions">
      <v-btn color="primary" size="large" @click="goToRegister">Go To Register</v-btn>
      <v-btn color="primary" size="large" variant="outlined" @click="goToLogin">Go To Login</v-btn>
    </div>
  </div>
</template>

<style scoped>
.home-card {
  max-width: 480px;
  margin: 100px auto;
  padding: 30px;
  background-color: rgb(var(--v-theme-surface-variant));
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  text-align: center;
}

.home-card h2 {
  margin-top: 0;
  margin-bottom: 16px;
  color: rgb(var(--v-theme-on-surface));
  font-size: 1.8rem;
}

.home-subtitle {
  margin-bottom: 28px;
  color: rgb(var(--v-theme-on-surface));
}

.home-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
}
</style>

