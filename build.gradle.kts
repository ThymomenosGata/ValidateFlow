plugins {
    kotlin("jvm") version "2.2.20"
    `maven-publish`
}

group = "org.wordy.validate.flow"
version = "0.0.1"

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.github.ThymomenosGata"
            artifactId = "validateflow"
            version = "0.0.1"

            from(components["java"])
        }
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    testImplementation("app.cash.turbine:turbine:1.2.1")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}