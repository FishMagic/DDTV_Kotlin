plugins {
  kotlin("multiplatform")
  kotlin("plugin.serialization") version "1.6.20"
}

val ktor_version = "2.0.0"

kotlin {
  jvm {
    compilations.all {
      kotlinOptions.jvmTarget = "15"
    }
  }
  sourceSets {
    val jvmMain by getting {
      dependencies {
        implementation(project(":middleLayer"))
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.6.1")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
        implementation("io.ktor:ktor-server-core:$ktor_version")
        implementation("io.ktor:ktor-server-cio:$ktor_version")
        implementation("io.ktor:ktor-network-tls-certificates:$ktor_version")
        implementation("io.ktor:ktor-server-cors:$ktor_version")
        implementation("io.ktor:ktor-server-compression:$ktor_version")
        implementation("io.ktor:ktor-server-content-negotiation:$ktor_version")
        implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")
        implementation("io.ktor:ktor-server-websockets:$ktor_version")
      }
    }
    val jvmTest by getting
  }
}