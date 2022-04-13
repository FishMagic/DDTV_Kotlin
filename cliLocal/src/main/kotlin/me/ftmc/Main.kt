package me.ftmc

import kotlinx.coroutines.runBlocking

fun main() {
  LogHolder("cliLocal")
  runBlocking {
    val middleLayer = MiddleLayer()
    val frontend = CliCommon(middleLayer)
    val backend = RecordBackend(middleLayer)
    Runtime.getRuntime().addShutdownHook(object : Thread() {
      override fun run() {
        runBlocking {
          frontend.stop()
          backend.stop()
        }
      }
    })
    backend.start()
    frontend.start()
  }
}