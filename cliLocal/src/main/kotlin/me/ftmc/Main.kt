package me.ftmc

import kotlinx.coroutines.runBlocking

fun main() {
  val logger = LogHolder("cliLocal")
  runBlocking {
    try {
      val middleLayer = MiddleLayer()
      val frontend = CliCommon(middleLayer)
      val backend = RecordBackend(middleLayer)
      Runtime.getRuntime().addShutdownHook(object : Thread() {
        override fun run() {
          runBlocking {
            try {
              frontend.stop()
              backend.stop()
            } catch (e: Exception) {
              logger.errorCatch(e)
            }
          }
        }
      })
      backend.start()
      frontend.start()
    } catch (e: Exception) {
      logger.errorCatch(e)
    }
  }
}