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
    <v-container class="pa-8">
        <h1 class="text-info mb-4">Contact Us</h1>
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

            <v-btn type="submit" color="info">
                Send Message
            </v-btn>
        </v-form>
    </v-container>
</template>

<style scoped>

.intro {
    margin-bottom: 1.5rem;
}

</style>