import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("jvm")
  kotlin("plugin.serialization") version "1.6.20"
  application
}

kotlin {
  sourceSets {
    val main by getting {
      dependencies {
        implementation(project(":cliCommon"))
        implementation(project(":middleLayer"))
        implementation(project(":recordBackend"))
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.6.1")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
      }
    }
    val test by getting
  }
}

tasks.withType<KotlinCompile> {
  kotlinOptions.jvmTarget = "15"
}

application {
  mainClass.set("me.ftmc.MainKt")
}