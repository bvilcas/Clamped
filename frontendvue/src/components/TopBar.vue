<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useSidebar } from '@/composables/useSidebar'
import type { UserDTO } from '@/dto/user-dto'

defineProps<{
  user: UserDTO | null
}>()

const router = useRouter()
const authStore = useAuthStore()
const menuOpen = ref(false)
const { sidebarVisible, toggleSidebar } = useSidebar()
</script>

<template>
  <header class="topbar">
    <!-- LEFT -->
    <div class="d-flex align-center ga-3">
      <v-btn
        v-show="!sidebarVisible"
        icon="mdi-menu"
        variant="text"
        size="small"
        class="sidebar-toggle"
        @click="toggleSidebar"
      />
      <v-text-field
        variant="outlined"
        density="compact"
        hide-details
        placeholder="Search"
        class="topbar-search"
      />
    </div>

    <!-- RIGHT -->
    <div class="d-flex align-center ga-4">
      <v-btn icon="mdi-bell-outline" variant="text" size="small" />

      <template v-if="user">
        <div class="d-flex align-center ga-2">
          <span class="avatar">
            {{ user.firstname[0] }}{{ user.lastname[0] }}
          </span>
          <span class="name">
            {{ user.firstname }} {{ user.lastname }}
          </span>
        </div>

        <v-menu v-model="menuOpen" location="bottom end">
          <template #activator="{ props }">
            <v-btn icon="mdi-cog-outline" variant="text" size="small" v-bind="props" />
          </template>

          <v-list>
            <v-list-item prepend-icon="mdi-account-outline" @click="menuOpen = false; router.push('/profile')">
              Profile
            </v-list-item>
            <v-list-item prepend-icon="mdi-cog-outline" @click="menuOpen = false; router.push('/settings')">
              Settings
            </v-list-item>
            <v-list-item prepend-icon="mdi-logout" @click="menuOpen = false; authStore.logout()">
              Logout
            </v-list-item>
          </v-list>
        </v-menu>
      </template>
    </div>
  </header>
</template>

<style scoped>
.topbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 1.5rem;
  border-bottom: 1px solid rgb(var(--v-theme-outline));
  background: rgb(var(--v-theme-surface));
  position: sticky;
  top: 0;
  z-index: 100;
}

.sidebar-toggle {
  color: rgb(var(--v-theme-on-surface)) !important;
}

.topbar-search {
  width: 600px;
}

.avatar {
  background: rgb(var(--v-theme-avatar));
  color: white;
  font-size: 0.8rem;
  font-weight: bold;
  padding: 0.4rem 0.6rem;
  border-radius: 50%;
}

.name {
  font-size: 0.9rem;
}

.topbar :deep(.v-btn--variant-text > .v-btn__overlay) { opacity: 0 !important; }
.topbar :deep(.v-ripple__container) { color: rgb(var(--v-theme-nav-action)); }
.topbar :deep(.v-ripple__animation) { opacity: 0.18 !important; }
.topbar :deep(.v-btn--variant-text:hover) { background-color: rgb(var(--v-theme-surface-light)) !important; }
.topbar :deep(.v-btn--variant-text:active) { background-color: rgb(var(--v-theme-surface-light)) !important; filter: brightness(0.95); }
</style>
