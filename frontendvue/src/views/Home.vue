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
  <v-container class="text-center mt-12">
    <!-- Header -->
    <v-row justify="center">
      <v-col cols="12">
        <h1 class="text-h4 font-weight-bold mb-4">
          Welcome to Clamped!
        </h1>
      </v-col>
    </v-row>

    <!-- Description -->
    <v-row justify="center">
      <v-col cols="12" md="8">
        <p class="text-body-1 mb-8">
          Your one-stop solution for vulnerability management.
          Built with Vue.js and Vuetify.
        </p>
      </v-col>
    </v-row>

    <!-- Action Buttons -->
    <v-row justify="center" align="center" class="ga-6">
      <v-col cols="auto">
        <v-btn
          color="primary"
          size="large"
          @click="goToRegister"
        >
          Go To Register
        </v-btn>
      </v-col>

      <v-col cols="auto">
        <v-btn
          color="primary"
          size="large"
          variant="outlined"
          @click="goToLogin"
        >
          Go To Login
        </v-btn>
      </v-col>
    </v-row>
  </v-container>
</template>

