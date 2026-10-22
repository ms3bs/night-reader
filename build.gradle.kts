plugins {
    kotlin("jvm") version "2.1.0" apply false
    kotlin("plugin.serialization") version "2.1.0" apply false
    id("io.ktor.plugin") version "3.3.3" apply false
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.plugin.serialization")

    repositories { mavenCentral() }

    dependencies {
        implementation("io.ktor:ktor-server-core:3.3.3")
        implementation("io.ktor:ktor-server-netty:3.3.3")
        implementation("io.ktor:ktor-server-content-negotiation:3.3.3")
        implementation("io.ktor:ktor-serialization-kotlinx-json:3.3.3")
        implementation("io.ktor:ktor-server-auth:3.3.3")
        implementation("io.ktor:ktor-server-auth-jwt:3.3.3")
        implementation("io.ktor:ktor-server-call-logging:3.3.3")
        implementation("ch.qos.logback:logback-classic:1.5.12")
    }
}