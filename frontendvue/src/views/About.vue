<script setup lang="ts">
</script>

<template>
  <v-container style="max-width: 800px;" class="pa-8">
    <h1 class="text-info" style="margin-bottom: 1rem;">About Clamped!</h1>
    <p style="margin-bottom: 2rem">
      Clamped! is a secure vulnerability tracking system built to help small teams
      and organizations manage, report, and resolve software vulnerabilities.
    </p>

    <v-card variant="elevated" elevation="2" class="about-card">
      <h2 style="margin-bottom: 0.6rem;">Mission</h2>
      <p class="text-body-1">
        Our goal is to make vulnerability tracking accessible and reliable,
        combining modern security with a user-friendly dashboard.
        Clamped! is designed with defense, compliance, and collaboration in mind.
      </p>
    </v-card>

    <v-card variant="elevated" elevation="2" class="about-card">
      <h2 style="margin-bottom: 0.6rem;">Features</h2>
      <ul class="features-list">
        <li>Report vulnerabilities and assign them to team members.</li>
        <li>Track status, severity, and deadlines for every issue.</li>
        <li>Role-based workflows for reporters, assignees, and verifiers.</li>
        <li>Filtered refresh token rotation with JSESSIONID cookie support for continuous access.</li>
        <li>Session-backed authentication with JWT for secure access.</li>
      </ul>
    </v-card>

    <v-card variant="elevated" elevation="2" class="about-card">
      <h2 style="margin-bottom: 0.6rem;">Who We Serve</h2>
      <p class="text-body-1" style="line-height: 1.6;">
        Clamped! is tailored for smaller teams, defense projects, and
        organizations who need a lightweight but powerful way to stay on top
        of security risks.
      </p>
    </v-card>
  </v-container>
</template>

<style scoped>
.about-card {
  margin-bottom: 2rem;
  padding: 1rem 1.5rem;
}

.features-list {
  padding-left: 1.2rem;
  list-style: disc;
  line-height: 1.6;
}

.features-list li {
  margin-bottom: 0.5rem;
}
</style>