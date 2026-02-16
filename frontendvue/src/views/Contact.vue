<script setup>
import { reactive, ref } from "vue"

const form = reactive({
    name: "",
    email: "",
    message: ""
})

const submitted = ref(false) // null not needed cuz it can only be true/false

function handleSubmit() {
    console.log("📩 Contact form submitted:", form)
    submitted.value = true
    form.name = ""
    form.email = ""
    form.message = ""
}
</script>

<template>
    <v-container class="contact-page">
        <h1>Contact Us</h1>
        <p class="intro">
            Have questions, feedback, or need support with Clamped?
            We’d love to hear from you.
        </p>

        <v-alert v-if="submitted" type="success" variant="tonal" class="mb-4">
            ✅ Your message has been sent. Thank you!
        </v-alert>

        <v-form @submit.prevent="handleSubmit">
            <v-text-field label="Name" v-model="form.name" required />

            <v-text-field label="Email" type="email" v-model="form.email" required />

            <v-textarea label="Message" rows="5" v-model="form.message" required />

            <v-btn type="submit" color="primary">
                Send Message
            </v-btn>
        </v-form>
    </v-container>
</template>

<style scoped>
.contact-page {
    padding: 2rem;
    max-width: 700px;
    margin: 0 auto;
    font-family: Arial, sans-serif;
    color: rgb(var(--v-theme-on-surface));
    line-height: 1.6;
}

.contact-page h1 {
    font-size: 2rem;
    margin-bottom: 1rem;
    color: rgb(var(--v-theme-info));
}

.intro {
    margin-bottom: 1.5rem;
    color: rgb(var(--v-theme-on-surface-variant));
}
</style>