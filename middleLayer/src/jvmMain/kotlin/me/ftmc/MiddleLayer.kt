package me.ftmc

import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import me.ftmc.action.Action
import me.ftmc.message.Message

@ObsoleteCoroutinesApi
class MiddleLayer {
  lateinit var backEnd: Backend
  val messageChannel = BroadcastChannel<Message>(Channel.BUFFERED)
  val actionChannel = BroadcastChannel<Action>(Channel.BUFFERED)

  fun backendBind(backEnd: Backend) {
    this.backEnd = backEnd
  }

  suspend fun stop() {
    backEnd.stop()
    messageChannel.close()
    actionChannel.close()
  }
}