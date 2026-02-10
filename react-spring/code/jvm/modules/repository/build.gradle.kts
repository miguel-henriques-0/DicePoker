plugins {
    kotlin("jvm")
}

group = "pt.isel.daw"
version = "1.0-SNAPSHOT"

dependencies {
    // Module dependencies
    api(project(":domain"))

    // To use Kotlin specific date and time functions
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.1")

    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}
