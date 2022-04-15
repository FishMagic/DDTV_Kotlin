package me.ftmc

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class ConfigClass(val cookies: MutableList<String> = mutableListOf())

class ConfigHolder {

  companion object {
    var configClass: ConfigClass? = null
  }

  private val configFile = File("./config.json")
  private val jsonSaver = Json {
    prettyPrint = true
  }
  private val logger = LogHolder()

  fun saveConfig() {
    if (!configFile.exists()) {
      logger.debug("[config holder] 配置文件不存在")
      configFile.createNewFile()
    }
    val configString = jsonSaver.encodeToString(configClass)
    val configFOS = FileOutputStream(configFile)
    configFOS.write(configString.encodeToByteArray())
    configFOS.flush()
    configFOS.close()
  }

  fun loadConfig(): ConfigClass {
    if (configClass == null) {
      if (!configFile.exists()) {
        logger.debug("[config holder] 配置文件不存在")
        configFile.createNewFile()
      }
      configClass = try {
        val configFIS = FileInputStream(configFile)
        val configString = configFIS.readBytes().decodeToString()
        jsonSaver.decodeFromString(configString)
      } catch (_: Exception) {
        logger.warn("[config holder] 配置文件读取失败")
        ConfigClass()
      }
    }
    return configClass!!
  }
}