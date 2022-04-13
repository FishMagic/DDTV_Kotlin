package me.ftmc

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import me.ftmc.action.ActionType
import me.ftmc.login.LoginStateHolder
import me.ftmc.message.Message
import me.ftmc.message.MessageType
import me.ftmc.room.RoomHolder

class RecordBackend(middleLayer: MiddleLayer) : Backend {
  override val coroutineScope = CoroutineScope(Job())
  override val messageSendChannel = middleLayer.messageChannel
  override val actionReceiveChannel = middleLayer.actionChannel
  val messageReceiveChannel = MutableSharedFlow<Message>()
  private val roomHolder = RoomHolder(this)
  private val loginStateHolder = LoginStateHolder(this)

  @Throws(IllegalArgumentException::class)
  override suspend fun start() {
    loginStateHolder.start()
    roomHolder.start()
    coroutineScope.launch {
      actionReceiveChannel.collect { action ->
        when (action.type) {
          ActionType.HELLO -> messageSendChannel.emit(Message(MessageType.HELLO, "Hello, ${action.data}"))
          else -> {}
        }
      }
    }
    coroutineScope.launch {
      messageReceiveChannel.collect { message ->
      }
    }
    println("后端初始化完成")
  }

  @Throws(IllegalArgumentException::class)
  override suspend fun stop() {
    println("后端正在退出")
    roomHolder.stop()
    loginStateHolder.stop()
    coroutineScope.cancel()
    println("后端已退出")
  }
}