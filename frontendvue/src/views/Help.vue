<script setup lang="ts">
</script>

<template>
  <div class="help-page">
    <h1>Help & Documentation</h1>
    <p class="intro">
      Welcome to Clamped! Here you'll find guidance on how to use the system,
      report vulnerabilities, and manage your security workflow.
    </p>

    <div class="help-section">
      <h2>Getting Started</h2>
      <p>
        After logging in, use the sidebar to navigate to your Profile, Vulnerabilities,
        and other sections. Your session is secured using JSESSIONID cookies and
        refresh token rotation for continuous access.
      </p>
    </div>

    <div class="help-section">
      <h2>Reporting Vulnerabilities</h2>
      <p>
        Go to the <strong>Report</strong> page to submit a new vulnerability. Fill in details like:
      </p>
      <ul>
        <li>Title and description</li>
        <li>Severity level (Low, Medium, High, Critical)</li>
        <li>CVE or CWE ID if available</li>
        <li>Repository and commit hash</li>
        <li>Due date for resolution</li>
      </ul>
    </div>

    <div class="help-section">
      <h2>Managing Assignments</h2>
      <p>
        Vulnerabilities are categorized into:
      </p>
      <ul>
        <li><strong>Reported</strong> -- Issues you've submitted.</li>
        <li><strong>Assigned</strong> -- Issues you are responsible for resolving.</li>
        <li><strong>Verified</strong> -- Issues you have confirmed as fixed.</li>
      </ul>
      <p>
        You can also self-assign to open vulnerabilities if your team allows it.
      </p>
    </div>

    <div class="help-section">
      <h2>Security & Sessions</h2>
      <p>
        Clamped! uses filtered refresh token rotation and JSESSIONID cookies for safe,
        unlimited usage. If you change your email or password, all sessions will be
        invalidated, and you'll be required to log in again.
      </p>
    </div>

    <div class="help-section">
      <h2>FAQ</h2>
      <ul>
        <li><strong>Why was I logged out after updating my email?</strong><br />
        For security, changing your email or password revokes all active sessions.</li>
        <li><strong>What happens if my session expires?</strong><br />
        Your refresh token rotation + JSESSIONID cookie will issue a new access token unless all sessions were revoked.</li>
        <li><strong>How do I reset my password?</strong><br />
        Go to Profile then Change Password. This will log you out everywhere for security.</li>
      </ul>
    </div>
  </div>
</template>

<style scoped>
.help-page {
  padding: 2rem;
  max-width: 900px;
  margin: 0 auto;
  font-family: Arial, sans-serif;
  color: rgb(var(--v-theme-on-surface));
  line-height: 1.6;
}

.help-page h1 {
  font-size: 2rem;
  margin-bottom: 1rem;
  color: rgb(var(--v-theme-info));
}

.intro {
  margin-bottom: 2rem;
  color: rgb(var(--v-theme-on-surface-variant));
  font-size: 1.1rem;
}

.help-section {
  margin-bottom: 2rem;
  background: rgb(var(--v-theme-surface));
  padding: 1.5rem;
  border: 1px solid rgb(var(--v-theme-outline));
  border-radius: 10px;
  box-shadow: 0 2px 6px rgba(0,0,0,0.05);
}

.help-section h2 {
  margin-bottom: 0.6rem;
  font-size: 1.3rem;
  color: rgb(var(--v-theme-on-surface));
}

.help-section ul {
  padding-left: 1.2rem;
  list-style: disc;
}

.help-section ul li {
  margin-bottom: 0.5rem;
}
</style>
