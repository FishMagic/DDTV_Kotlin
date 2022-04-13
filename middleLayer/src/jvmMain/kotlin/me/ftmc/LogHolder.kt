package me.ftmc

import java.io.File
import java.io.FileWriter
import java.time.Instant
import kotlinx.serialization.Serializable

class LogObject(private val level: String, private val time: Long, val message: String) {
  override fun toString(): String {
    return "[${this.level}] ${formatTimeToISO(this.time)}: ${this.message}"
  }
}

fun createLogFile(platform: String, time: String): File {
  val logDir = File("log/")
  if (logDir.isFile) {
    logDir.delete()
  }
  if (!logDir.exists()) {
    logDir.mkdir()
  }
  val logFile = File(logDir, "DDTV-$platform-$time.log")
  if (!logFile.exists()) {
    logFile.createNewFile()
  }
  return logFile
}

@Serializable
enum class LocalLogLevel(val levelInt: Int) {
  NONE(0),
  INFO(1),
  WARNING(2),
  DEBUG(3),
  ALL(4)
}

class LogHolder(platform: String, debugPrint: Boolean = false) {
  companion object {
    val loggerBuket = mutableListOf<LogObject>()
    var maxSize = 100
    var logFile: File? = null
    var logLevel = LocalLogLevel.ALL
    var debugPrint = false
  }

  init {
    logFile = logFile ?: createLogFile(platform, formatTimeToFileName(Instant.now().epochSecond))
    LogHolder.debugPrint = debugPrint
  }

  constructor() : this("", debugPrint) {
    require(logFile != null)
  }

  @Synchronized
  fun info(message: String) {
    if (logLevel >= LocalLogLevel.INFO) {
      synchronized(loggerBuket) {
        if (maxSize > 0 && loggerBuket.size >= maxSize) {
          loggerBuket.removeAt(0)
        }
        val logObject = LogObject("INFO", Instant.now().epochSecond, message)
        loggerBuket.add(logObject)
        println(logObject)
        val writer = FileWriter(logFile!!, true)
        writer.write(logObject.toString() + "\n")
        writer.flush()
        writer.close()
      }
    }
  }

  @Synchronized
  fun warn(message: String) {
    if (logLevel >= LocalLogLevel.WARNING) {
      synchronized(loggerBuket) {
        if (maxSize > 0 && loggerBuket.size >= maxSize) {
          loggerBuket.removeAt(0)
        }
        val logObject = LogObject("WARN", Instant.now().epochSecond, message)
        loggerBuket.add(logObject)
        println()
        val writer = FileWriter(logFile!!, true)
        writer.write(logObject.toString() + "\n")
        writer.flush()
        writer.close()
      }
    }
  }

  @Synchronized
  fun debug(message: String) {
    if (logLevel >= LocalLogLevel.DEBUG) {
      synchronized(loggerBuket) {
        if (maxSize > 0 && loggerBuket.size >= maxSize) {
          loggerBuket.removeAt(0)
        }
        val logObject = LogObject("DEBUG", Instant.now().epochSecond, message)
        loggerBuket.add(logObject)
        if (debugPrint) {
          println(logObject)
        }
        val writer = FileWriter(logFile!!, true)
        writer.write(logObject.toString() + "\n")
        writer.flush()
        writer.close()
      }
    }
  }

  @Synchronized
  fun errorCatch(e: Throwable) {
    loggerBuket.add(LogObject("ERROR", Instant.now().epochSecond, e.message.toString()))
    e.printStackTrace()
    val writer = FileWriter(logFile!!, true)
    writer.write(e.stackTrace.toString() + "\n")
    writer.flush()
    writer.close()
  }
}