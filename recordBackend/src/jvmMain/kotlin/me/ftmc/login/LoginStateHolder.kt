package me.ftmc.login

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import me.ftmc.LogHolder
import me.ftmc.RecordBackend
import me.ftmc.jsonProcessor
import me.ftmc.message.LoginStateChangeMessageData
import me.ftmc.message.Message
import me.ftmc.message.MessageType

var cookieUsable = false

interface LoginClass {
  fun start()
  fun stop()
}

class LoginStateHolder(recordBackend: RecordBackend) {
  private val coroutineScope = CoroutineScope(Job())
  private val messageSendChange = recordBackend.messageReceiveChannel
  val messageChannel = MutableSharedFlow<Message>()
  private var loginClass: LoginClass? = null
  private val logger = LogHolder()

  private val messageCollection: suspend CoroutineScope.() -> Unit = {
    logger.debug("[login state holder] 开始监听消息上传队列")
    messageChannel.collect { message ->
      if (message.type == MessageType.LOGIN_STATE_CHANGE) {
        val messageData = jsonProcessor.decodeFromString<LoginStateChangeMessageData>(message.data)
        if (messageData.newValue > 0) {
          cookieUsable = false
          loginClass?.stop()
          loginClass = null
        } else if (messageData.newValue == 0) {
          cookieUsable = true
          loginClass?.stop()
          loginClass = LoginStateChecker(this@LoginStateHolder)
        }
      }
      messageSendChange.emit(message)
    }
  }

  private var messageCollectionJob: Job? = null

  fun start() {
    logger.debug("[login state holder] 开始初始化")
    loginClass = if (cookieUsable) LoginStateChecker(this) else LoginProcessor(this)
    loginClass?.start()
    runBlocking { messageCollectionJob = coroutineScope.launch(block = messageCollection) }
    logger.debug("[login state holder] 初始化完成")
  }

  fun stop() {
    runBlocking {
      loginClass?.stop()
    }
    coroutineScope.cancel()
    logger.debug("[login state holder] 已停止")
  }

  suspend fun logout() {
    if (loginClass !is LoginProcessor) {
      runBlocking { loginClass?.stop() }
      loginClass = LoginProcessor(this)
      loginClass?.start()
      messageSendChange.emit(
        Message(
          MessageType.LOGIN_STATE_CHANGE,
          jsonProcessor.encodeToString(LoginStateChangeMessageData(3, "退出登录"))
        )
      )
    }
  }

}