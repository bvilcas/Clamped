<script setup lang="ts">
import { computed } from "vue"
import { useRoute, useRouter } from "vue-router"
import { useAuthStore } from "@/stores/auth"
import { useSidebar } from "@/composables/useSidebar"

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const { toggleSidebar } = useSidebar()

const isProjectsActive = computed(() => route.path.startsWith("/projects") || route.path.startsWith("/project/"))
const isVulnsActive = computed(() => route.path.startsWith("/vulns"))
const isHelpActive = computed(() => route.path.startsWith("/help"))
const isContactActive = computed(() => route.path.startsWith("/contact"))
const isAboutActive = computed(() => route.path.startsWith("/about"))
</script>

<template>
  <aside class="sidebar">
    <div class="sidebar-header">
      <div class="logo" @click="router.push('/dashboard')" style="cursor: pointer;">
        <img src="/logo.png" alt="Clamped Logo" />
      </div>
      <v-btn variant="text" size="small" class="collapse-btn" @click="toggleSidebar">
        «
      </v-btn>
    </div>

    <nav class="nav">
      <v-btn variant="text" block class="nav-item" :class="{ 'nav-item-active': isProjectsActive }" @click="router.push('/projects')">
        📁 Projects
      </v-btn>

      <v-btn variant="text" block class="nav-item" :class="{ 'nav-item-active': isVulnsActive }" @click="router.push('/vulns')">
        🛡️ My Vulns
      </v-btn>

      <v-btn variant="text" block class="nav-item">
        💬 Messages
      </v-btn>

      <v-btn variant="text" block class="nav-item">
        👥 Team
      </v-btn>
    </nav>

    <div class="sidebar-footer">
      <v-btn variant="text" block class="nav-item" :class="{ 'nav-item-active': isHelpActive }" @click="router.push('/help')">
        ❓ Help/Docs
      </v-btn>

      <v-btn variant="text" block class="nav-item" :class="{ 'nav-item-active': isContactActive }" @click="router.push('/contact')">
        📧 Contact Us
      </v-btn>

      <v-btn variant="text" block class="nav-item" :class="{ 'nav-item-active': isAboutActive }" @click="router.push('/about')">
        ℹ️ About Clamped!
      </v-btn>

      <div class="sidebar-logout">
        <v-btn color="error" block @click="authStore.logout()">
          Logout
        </v-btn>
      </div>
    </div>
  </aside>
</template>

<style scoped>
.sidebar {
  width: 220px;
  background: rgb(var(--v-theme-surface-variant));
  border-right: 1px solid rgb(var(--v-theme-outline));
  display: flex;
  flex-direction: column;
  padding: 1rem;
}

.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 0.5rem;
  margin-left: 17px;
  line-height: 0;
}

.collapse-btn {
  min-width: unset !important;
  width: 28px !important;
  height: 28px !important;
  font-size: 1rem !important;
  color: rgb(var(--v-theme-secondary)) !important;
  text-transform: none !important;
  letter-spacing: normal !important;
  margin-right: -10px;
}
.collapse-btn :deep(.v-btn__overlay) { opacity: 0 !important; }
.collapse-btn:hover { background-color: rgb(var(--v-theme-outline)) !important; }

.logo {
  width: 100px;
  height: 50px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: flex-start;
  margin: 5px 0;
}

.logo img {
  height: 50px;
  width: 175px;
  display: block;
}

.nav {
  flex: 1;
}

.nav-item {
  justify-content: flex-start !important;
  text-transform: none !important;
  letter-spacing: normal !important;
  font-size: 0.95rem !important;
  color: rgb(var(--v-theme-on-surface)) !important;
  margin-bottom: 0.3rem;
  border-radius: 8px !important;
}

.nav-item :deep(.v-btn__overlay) { opacity: 0 !important; }
.nav-item :deep(.v-ripple__container) { color: rgb(var(--v-theme-nav-action)); }
.nav-item :deep(.v-ripple__animation) { opacity: 0.10 !important; }
.nav-item:hover { background-color: rgb(var(--v-theme-outline)) !important; }
.nav-item.nav-item-active { background-color: rgba(var(--v-theme-nav-action), 0.18) !important; }

.sidebar-footer {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  margin-top: auto;
  margin-bottom: 1rem;
}

.sidebar-logout {
  margin-top: 2px;
}

@media (max-width: 768px) {
  .sidebar {
    width: 100%;
    flex-direction: row;
    justify-content: space-between;
    border-right: none;
    border-bottom: 1px solid rgb(var(--v-theme-outline));
  }
}
</style>
