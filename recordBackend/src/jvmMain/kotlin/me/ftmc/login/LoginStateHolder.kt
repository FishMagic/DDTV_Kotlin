package me.ftmc.login

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import me.ftmc.LogHolder
import me.ftmc.RecordBackend
import me.ftmc.message.Message
import me.ftmc.message.MessageType

/**
 * 0 -> 未登录
 * 1 -> 已登录
 * 2 -> 登录失效
 * 3 -> 登录中
 */
var globalLoginState = 0

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
      when (message.type) {
        MessageType.LOGIN_FAILURE -> {
          loginClass?.stop()
          loginClass = null
          globalLoginState = 2
        }
        MessageType.LOGIN_SUCCESS -> {
          loginClass?.stop()
          loginClass = LoginStateChecker(this@LoginStateHolder)
          globalLoginState = 1
        }
        else -> {}
      }
      messageSendChange.emit(message)
    }
  }

  private var messageCollectionJob: Job? = null

  fun start() {
    logger.debug("[login state holder] 开始初始化")
    loginClass = if (globalLoginState == 1) LoginStateChecker(this) else LoginProcessor(this)
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

  fun logout() {
    if (loginClass !is LoginProcessor) {
      runBlocking { loginClass?.stop() }
      loginClass = LoginProcessor(this)
      loginClass?.start()
      globalLoginState = 3
      runBlocking { messageSendChange.emit(Message(MessageType.LOGOUT)) }
    }
  }

}