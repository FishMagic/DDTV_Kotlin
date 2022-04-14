package me.ftmc

import io.ktor.http.parseServerSetCookieHeader
import io.ktor.http.renderCookieHeader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import me.ftmc.action.ActionType
import me.ftmc.login.LoginStateHolder
import me.ftmc.login.cookieUsable
import me.ftmc.login.globalLoginState
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
  private val configClass = configHolder.loadConfig()

  private val actionCollection: suspend CoroutineScope.() -> Unit = {
    logger.debug("[record backend] 动作下发队列监听开始")
    actionReceiveChannel.collect { action ->
      when (action.type) {
        ActionType.HELLO -> messageSendChannel.emit(
          Message(
            MessageType.HELLO,
            "Hello, ${action.data}"
          )
        )
        ActionType.LOGOUT -> {
          cookieUsable = false
          cookiesStorage.clearCookie()
          configSave()
          runBlocking { loginStateHolder.logout() }
        }
        else -> {}
      }
    }
  }
  private val messageCollection: suspend CoroutineScope.() -> Unit = {
    logger.debug("[record backend] 消息上传队列监听开始")
    messageReceiveChannel.collect { message ->
      when (message.type) {
        MessageType.LOGIN_SUCCESS -> {
          configSave()
        }
        MessageType.LOGIN_FAILURE -> {
          cookiesStorage.clearCookie()
          configSave()
        }
        else -> {}
      }
      messageSendChannel.emit(message)
    }
  }

  private var actionCollectionJob: Job? = null
  private var messageCollectionJob: Job? = null

  override suspend fun start() {
    logger.info("[record backend] 开始初始化")
    configLoad()
    runBlocking {
      actionCollectionJob = coroutineScope.launch(block = actionCollection)
      messageCollectionJob = coroutineScope.launch(block = messageCollection)
    }
    roomHolder.start()
    loginStateHolder.start()
    logger.info("[record backend] 初始化完成")
  }

  override suspend fun stop() {
    runBlocking {
      roomHolder.stop()
      loginStateHolder.stop()
    }
    coroutineScope.cancel()
    logger.info("[record backend] 已停止")
  }

  private fun configSave() {
    logger.debug("[record backend] 开始保存配置文件")
    configClass.cookies.clear()
    cookiesStorage.getCookie().forEach {
      configClass.cookies.add(renderCookieHeader(it))
    }
    configHolder.saveConfig(configClass)
    logger.debug("[record backend] 配置文件保存完成")
  }

  private fun configLoad() {
    logger.debug("[record backend] 开始加载配置文件")
    configClass.cookies.forEach {
      cookiesStorage.addCookie(parseServerSetCookieHeader(it))
      cookieUsable = true
      globalLoginState = 0
    }
    logger.debug("[record backend] 配置文件加载完成")
  }
}