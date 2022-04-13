package me.ftmc.login

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import me.ftmc.LogHolder
import me.ftmc.RecordBackend
import me.ftmc.jsonProcessor
import me.ftmc.message.LoginStateChangeMessageData
import me.ftmc.message.Message
import me.ftmc.message.MessageType

var cookieUsable = false

class LoginStateHolder(recordBackend: RecordBackend) {
  private val coroutineScope = CoroutineScope(Job())
  private val messageSendChange = recordBackend.messageReceiveChannel
  val messageChannel = MutableSharedFlow<Message>()
  private val loginProcessor = LoginProcessor(this)
  private val loginStateChecker = LoginStateChecker(this)
  private val logger = LogHolder()
  private var loginState = 0

  fun start() {
    logger.debug("[login state holder] 初始化开始")
    coroutineScope.launch {
      logger.debug("[login state holder] 消息上传队列监听开始")
      messageChannel.collect { message ->
        if (message.type == MessageType.LOGIN_STATE_CHANGE) {
          val messageData = jsonProcessor.decodeFromString<LoginStateChangeMessageData>(message.data)
          if (messageData.newValue > 0) {
            loginProcessor.start()
          } else if (messageData.newValue == 0) {
            loginStateChecker.start()
          }
        }
        messageSendChange.emit(message)
      }
    }
    if (!cookieUsable) {
      loginProcessor.start()
    } else {
      loginStateChecker.start()
    }
  }

  fun stop() {
    loginProcessor.stop()
    loginStateChecker.stop()
    coroutineScope.cancel()
    logger.debug("[login state holder] 已停止")
  }

}