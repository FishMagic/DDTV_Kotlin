pluginManagement {
  repositories {
    google()
    gradlePluginPortal()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
  }

}

rootProject.name = "DDTV_Kotlin"

include(":cliCommon")
include(":cliLocal")
include(":cliRemote")
include(":guiCommon")
include(":guiLocal")
include(":guiRemote")
include(":wsServer")
include(":middleLayer")
include(":recordBackend")
include(":wsClient")