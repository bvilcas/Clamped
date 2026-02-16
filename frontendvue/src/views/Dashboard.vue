<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { fetchWithAuth } from '@/utils/fetchWithAuth'
import type { UserDTO } from '@/dto/user-dto'
import type { ProjectDTO } from '@/dto/project-dto'
import type { VulnerabilityDTO } from '@/dto/vulnerability-dto'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
// router
const router = useRouter()

// Get the store instance (you can now access the global state)
const authStore = useAuthStore()

// State variables
// refs are generic types, so we provide the type in <>
const user = ref<UserDTO | null>(null)
const reported = ref<VulnerabilityDTO[]>([])
const assigned = ref<VulnerabilityDTO[]>([])
const verified = ref<VulnerabilityDTO[]>([])

const projects = ref<ProjectDTO[]>([])


  onMounted(() => {
    // Data fetching =========

    // No need to type annotate since this is technically same concept as "const x = 5"
    // We use arrow functions to order each call using ".then()" later
    const fetchProfile = async () => {
      try {
        console.log("Sending token:", localStorage.getItem("accessToken"))

        const res = await fetchWithAuth("http://localhost:8080/api/v1/users/profile")
        if (res.ok) {
          const data = await res.json()
          console.log("🔎 /profile response:", data)
          user.value = data.data
        }
      } catch (err: unknown) {
        if (err instanceof Error) {
          console.error("Profile fetch failed:", err.message);
        } else {
          console.error("Profile fetch failed with unknown error");
        }
        authStore.logout(); // force logout if token refresh failed
      }
    };


    const fetchUserVulns = async () => {
      const base = "http://localhost:8080/api/v1/uservulns"; // it knows its a string

      try {
        // shortcut to create 3 constants at once


        // await always waits for a promise
        // promise is a placeholder for future value
        // Creates an array of promises/constants
        // Promise.all is a checkpoint that waits for all promises to resolve
        const [reportedRes, assignedRes, verifiedRes] = await Promise.all([
          fetchWithAuth(`${base}/reported`),
          fetchWithAuth(`${base}/assigned`),
          fetchWithAuth(`${base}/verified`),
        ]);

        if (reportedRes.ok) {
          reported.value = (await reportedRes.json()).data || [];
        }
        if (assignedRes.ok) {
          assigned.value = (await assignedRes.json()).data || [];
        }
        if (verifiedRes.ok) {
          verified.value = (await verifiedRes.json()).data || [];
        }

      } catch (err: unknown) {
        if (err instanceof Error) {
          console.error(" Vulnerabilities fetch failed:", err.message);
        } else {
          console.error(" Vulnerabilities fetch failed with unknown error");
        }
        authStore.logout(); // force logout if refresh failed mid-fetch
      }
    };

    const fetchProjects = async () => {
      const res = await fetchWithAuth("http://localhost:8080/api/v1/userprojects/all");
      const data = await res.json();
      projects.value = data.data || [];
    };

    // we defined functions above now we call them in order
    fetchProfile().then(() => {
      fetchUserVulns();
      fetchProjects();
    });
  });

  // Watch for authentication changes and redirect if logged out
  // Store result of first getter (watching the ref)
  watch(
    () => authStore.isAuthenticated, // Watch this
    (isAuth) => { // Receive the value
      // Logic
      if (isAuth === false) {
        router.replace('/login') // Prevents back button redirect
      }
    }
  )
</script>

<template>
  <section class="content">
    <div class="title-row">
      <h1 class="title">Where You Left Off</h1>
      <div class="stats-bar">
        <span>Open Vulns: <strong>{{ reported.length }}</strong></span>
        <span>Patched: <strong>{{ verified.length }}</strong></span>
        <span>Overdue: <strong>2</strong></span>
      </div>
    </div>

    <div class="cards">
          <div class="card">
            <h2>Recent Reports</h2>
            <p v-if="reported.length === 0" class="empty">
              No reports submitted.
            </p>
            <ul v-else class="list">
              <li v-for="v in reported" :key="v.id">
                {{ v.title }} - {{ v.status }}
              </li>
            </ul>
          </div>

          <div class="card">
            <h2>Assigned to You</h2>
            <p v-if="assigned.length === 0" class="empty">
              No tasks assigned.
            </p>
            <ul v-else class="list">
              <li v-for="v in assigned" :key="v.id">
                {{ v.title }} - {{ v.status }}
              </li>
            </ul>
          </div>

          <div class="card">
            <h2>Verified by You</h2>
            <p v-if="verified.length === 0" class="empty">
              No verifications yet.
            </p>
            <ul v-else class="list">
              <li v-for="v in verified" :key="v.id">
                {{ v.title }} - {{ v.status }}
              </li>
            </ul>
          </div>
        </div>

        <!-- ADMIN -->
        <div v-if="user?.role === 'ADMIN'" class="admin-card">
          <h2>👑 Admin Panel</h2>
          <div class="actions">
            <v-btn
              color="info"
              @click="router.push('/admin/users')"
            >
              Manage Users
            </v-btn>
            <v-btn
              color="secondary"
              @click="router.push('/admin/assign')"
            >
              Assign Roles
            </v-btn>
          </div>
        </div>

        <div class="create">
          <v-btn
            color="info"
            @click="router.push('/report')"
          >
            + Report Vulnerability
          </v-btn>
        </div>

        <!-- SECOND ROW -->
        <div class="cards secondary-cards">
          <div class="card">
            <h2>Recently Updated Projects</h2>
            <p v-if="projects.length === 0" class="empty">
              No recent project activity.
            </p>
            <ul v-else class="list">
              <li v-for="p in projects.slice(0, 5)" :key="p.id">
                {{ p.name }}
              </li>
            </ul>
          </div>

          <div class="card">
            <h2>Your Activity Breakdown</h2>
            <ul class="list">
              <li>Reported: {{ reported.length }}</li>
              <li>Assigned: {{ assigned.length }}</li>
              <li>Verified: {{ verified.length }}</li>
            </ul>
          </div>

          <div class="card">
            <h2>My Projects</h2>
            <p v-if="projects.length === 0" class="empty">
              No projects yet.
            </p>
            <ul v-else class="list">
              <li
                v-for="p in projects"
                :key="p.id"
                style="cursor: pointer"
                @click="router.push(`/project/${p.id}`)"
              >
                {{ p.name }}
              </li>
            </ul>
          </div>
        </div>

        <div class="create">
          <v-btn
            color="info"
            @click="router.push('/projects/create')"
          >
            + Create Project
          </v-btn>
        </div>
  </section>
</template>


<style scoped>
.content {
  padding: 2rem;
  overflow-y: auto;
}

.title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1.5rem;
}

.title {
  font-size: 1.7rem;
  font-weight: 700;
  margin: 0;
  color: rgb(var(--v-theme-on-surface));
}

.stats-bar {
  display: flex;
  gap: 1rem;
  font-size: 0.95rem;
}

.cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1.25rem;
}

.secondary-cards {
  margin-top: 2rem;
}

.card {
  background: rgb(var(--v-theme-surface));
  border: 1px solid rgb(var(--v-theme-outline));
  border-radius: 12px;
  padding: 1rem;
}

.card h2 {
  font-size: 1rem;
  margin-bottom: 1rem;
}

.list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.list li {
  border-top: 1px solid rgb(var(--v-theme-outline));
  padding: 0.6rem 0;
}

.list li:first-child {
  border-top: none;
}

.empty {
  color: rgb(var(--v-theme-secondary));
  font-size: 0.9rem;
}

.create {
  margin-top: 2rem;
  text-align: right;
}

.admin-card {
  margin-top: 2rem;
  background: rgb(var(--v-theme-surface));
  border: 1px solid rgb(var(--v-theme-outline));
  border-radius: 12px;
  padding: 1rem;
}

.actions {
  display: flex;
  gap: 0.75rem;
  margin-top: 1rem;
}

@media (max-width: 1024px) {
  .cards {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .cards {
    grid-template-columns: 1fr;
  }
}
</style>