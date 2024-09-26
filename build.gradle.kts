import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType
import org.jetbrains.intellij.platform.gradle.models.ProductRelease

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.0"
    kotlin("plugin.serialization") version "1.9.10"
    id("org.jetbrains.intellij.platform") version "2.0.1"
}

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        create("2024.2")
        instrumentationTools()
        pluginVerifier()
    }
    implementation(libs.bundles.ktor) {
        exclude(group = "org.jetbrains.kotlin")
        exclude(group = "org.jetbrains.kotlinx")
        exclude(group = "org.slf4j")
    }
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
}

val paramSinceBuild = "223"

intellijPlatform {
    version = "1.1.7"
    group = "co.tula.mermaidchart"
    pluginConfiguration {
        ideaVersion {
            sinceBuild = paramSinceBuild
            untilBuild = provider { null }
        }
    }

    pluginVerification {
        ides {
            select {
                types = listOf(IntelliJPlatformType.IntellijIdeaCommunity)
                channels = listOf(ProductRelease.Channel.RELEASE)
                sinceBuild = paramSinceBuild
                untilBuild = provider { null }
            }
        }
    }

    signing {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishing {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }
}
