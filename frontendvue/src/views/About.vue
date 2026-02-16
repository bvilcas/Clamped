<script setup lang="ts">
</script>

<template>
  <div class="about-page">
    <h1>About Clamped!</h1>
    <p class="tagline">
      Clamped! is a secure vulnerability tracking system built to help small teams
      and organizations manage, report, and resolve software vulnerabilities.
    </p>

    <div class="about-section">
      <h2>Mission</h2>
      <p>
        Our goal is to make vulnerability tracking accessible and reliable,
        combining modern security with a user-friendly dashboard.
        Clamped! is designed with defense, compliance, and collaboration in mind.
      </p>
    </div>

    <div class="about-section">
      <h2>Features</h2>
      <ul>
        <li>Report vulnerabilities and assign them to team members.</li>
        <li>Track status, severity, and deadlines for every issue.</li>
        <li>Role-based workflows for reporters, assignees, and verifiers.</li>
        <li>Filtered refresh token rotation with JSESSIONID cookie support for continous access.</li>
        <li>Session-backed authentication with JWT for secure access.</li>
      </ul>
    </div>

    <div class="about-section">
      <h2>Who We Serve</h2>
      <p>
        Clamped! is tailored for smaller teams, defense projects, and
        organizations who need a lightweight but powerful way to stay on top
        of security risks.
      </p>
    </div>
  </div>
</template>

<style scoped>
.about-page {
  padding: 2rem;
  max-width: 800px;
  margin: 0 auto;
  font-family: Arial, sans-serif;
  color: rgb(var(--v-theme-on-surface));
  line-height: 1.6;
}

.about-page h1 {
  font-size: 2rem;
  margin-bottom: 1rem;
  color: rgb(var(--v-theme-info));
}

.tagline {
  font-size: 1.1rem;
  margin-bottom: 2rem;
  color: rgb(var(--v-theme-on-surface-variant));
}

.about-section {
  margin-bottom: 2rem;
  background: rgb(var(--v-theme-surface));
  padding: 1rem 1.5rem;
  border: 1px solid rgb(var(--v-theme-outline));
  border-radius: 10px;
  box-shadow: 0 2px 6px rgba(0,0,0,0.05);
}

.about-section h2 {
  margin-bottom: 0.6rem;
  color: rgb(var(--v-theme-on-surface));
}

.about-section ul {
  padding-left: 1.2rem;
  list-style: disc;
}

.about-section ul li {
  margin-bottom: 0.5rem;
}
</style>
