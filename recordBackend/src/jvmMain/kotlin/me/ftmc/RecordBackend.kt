package me.ftmc

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import me.ftmc.action.ActionType
import me.ftmc.login.LoginStateHolder
import me.ftmc.message.Message
import me.ftmc.message.MessageType
import me.ftmc.room.RoomHolder

@ObsoleteCoroutinesApi
class RecordBackend(middleLayer: MiddleLayer) : Backend {
  override var isStarted = false
  override val coroutineScope = CoroutineScope(Job())
  override val messageSendChannel = middleLayer.messageChannel
  override val actionReceiveChannel = middleLayer.actionChannel.openSubscription()
  val messageChannel = BroadcastChannel<Message>(Channel.BUFFERED)
  private val messageReceiveChannel = messageChannel.openSubscription()
  private val roomHolder = RoomHolder(this)
  private val loginStateHolder = LoginStateHolder()

  @Throws(IllegalArgumentException::class)
  override suspend fun start() {
    require(!isStarted)
    loginStateHolder.start()
    roomHolder.start()
    coroutineScope.launch {
      while (true) {
        val action = actionReceiveChannel.receive()
        when (action.type) {
          ActionType.HELLO -> messageSendChannel.send(Message(MessageType.HELLO, "Hello, ${action.data}"))
          else -> {}
        }
        yield()
      }
    }
    coroutineScope.launch {
      while (true) {
        val message = messageReceiveChannel.receive()
        yield()
      }
    }
    isStarted = true
    println("后端初始化完成")
  }

  @Throws(IllegalArgumentException::class)
  override suspend fun stop() {
    require(isStarted)
    println("后端正在退出")
    roomHolder.stop()
    loginStateHolder.stop()
    coroutineScope.cancel()
    actionReceiveChannel.cancel()
    messageReceiveChannel.cancel()
    messageChannel.close()
    println("后端已退出")
  }
}