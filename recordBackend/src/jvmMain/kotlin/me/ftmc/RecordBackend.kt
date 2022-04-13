package me.ftmc

import io.ktor.http.parseServerSetCookieHeader
import io.ktor.http.renderCookieHeader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import me.ftmc.action.ActionType
import me.ftmc.config.ConfigClass
import me.ftmc.config.ConfigHolder
import me.ftmc.login.LoginStateHolder
import me.ftmc.login.cookieUsable
import me.ftmc.message.LoginStateChangeMessageData
import me.ftmc.message.Message
import me.ftmc.message.MessageType
import me.ftmc.room.RoomHolder

class RecordBackend(middleLayer: MiddleLayer) : Backend {
  override val coroutineScope = CoroutineScope(Job())
  override val messageSendChannel = middleLayer.messageChannel
  override val actionReceiveChannel = middleLayer.actionChannel
  val messageReceiveChannel = MutableSharedFlow<Message>()
  private val configHolder = ConfigHolder()
  private val roomHolder = RoomHolder(this)
  private val loginStateHolder = LoginStateHolder(this)
  private val logger = LogHolder()

  override suspend fun start() {
    logger.info("[record backend] 开始初始化")
    configLoad()
    roomHolder.start()
    coroutineScope.launch {
      logger.info("[record backend] 动作下发队列监听开始")
      actionReceiveChannel.collect { action ->
        when (action.type) {
          ActionType.HELLO -> messageSendChannel.emit(
            Message(
              MessageType.HELLO,
              "Hello, ${action.data}"
            )
          )
          else -> {}
        }
      }
    }
    coroutineScope.launch {
      logger.info("[record backend] 消息上传队列监听开始")
      messageReceiveChannel.collect { message ->
        when (message.type) {
          MessageType.LOGIN_STATE_CHANGE -> {
            val messageData = jsonProcessor.decodeFromString<LoginStateChangeMessageData>(message.data)
            if (messageData.newValue == 0) {
              configSave()
            }
          }
        }
        messageSendChannel.emit(message)
      }
    }
    loginStateHolder.start()
    logger.info("[record backend] 初始化完成")
  }

  override suspend fun stop() {
    roomHolder.stop()
    loginStateHolder.stop()
    coroutineScope.cancel()
    logger.info("[record backend] 已停止")
  }

  private fun configSave() {
    logger.info("[record backend] 开始保存配置文件")
    val configClass = ConfigClass()
    cookiesStorage.getCookie().forEach {
      configClass.cookies.add(renderCookieHeader(it))
    }
    configHolder.saveConfig(configClass)
    logger.info("[record backend] 配置文件保存完成")
  }

  private fun configLoad() {
    logger.info("[record backend] 开始加载配置文件")
    val configClass = configHolder.loadConfig()
    configClass.cookies.forEach {
      cookiesStorage.addCookie(parseServerSetCookieHeader(it))
      cookieUsable = true
    }
    logger.info("[record backend] 配置文件加载完成")
  }
}