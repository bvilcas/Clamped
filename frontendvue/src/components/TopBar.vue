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
    <div class="topbar-left">
      <v-btn
        v-show="!sidebarVisible"
        variant="text"
        size="small"
        class="sidebar-toggle"
        @click="toggleSidebar"
      >
        ☰
      </v-btn>
      <v-text-field
        variant="outlined"
        density="compact"
        hide-details
        placeholder="Search"
        class="topbar-search"
      />
    </div>

    <!-- RIGHT -->
    <div class="topbar-right">
      <v-btn icon variant="text" size="small">
        🔔
      </v-btn>

      <template v-if="user">
        <div class="profile">
          <span class="avatar">
            {{ user.firstname[0] }}{{ user.lastname[0] }}
          </span>
          <span class="name">
            {{ user.firstname }} {{ user.lastname }}
          </span>
        </div>

        <v-menu v-model="menuOpen" location="bottom end">
          <template #activator="{ props }">
            <v-btn icon variant="text" size="small" v-bind="props">
              ⚙️
            </v-btn>
          </template>

          <v-list>
            <v-list-item @click="menuOpen = false; router.push('/profile')">
              Profile
            </v-list-item>
            <v-list-item @click="menuOpen = false; router.push('/settings')">
              Settings
            </v-list-item>
            <v-list-item @click="menuOpen = false; authStore.logout()">
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

.topbar-left {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.sidebar-toggle {
  min-width: unset !important;
  width: 36px !important;
  height: 36px !important;
  font-size: 1.2rem !important;
  text-transform: none !important;
  letter-spacing: normal !important;
  color: rgb(var(--v-theme-on-surface)) !important;
}

.topbar-search {
  width: 600px;
}

.topbar-right {
  display: flex;
  align-items: center;
  gap: 14px;
}

.profile {
  display: flex;
  align-items: center;
  gap: 0.4rem;
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
