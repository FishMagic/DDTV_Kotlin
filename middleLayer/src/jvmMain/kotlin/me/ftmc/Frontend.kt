package me.ftmc

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ReceiveChannel
import me.ftmc.action.Action
import me.ftmc.message.Message

@ObsoleteCoroutinesApi
interface Frontend {
  var isStarted: Boolean
  val coroutineScope: CoroutineScope
  val messageReceiveChannel: ReceiveChannel<Message>
  val actionChannel: BroadcastChannel<Action>

  @Throws(IllegalArgumentException::class)
  suspend fun start()

  @Throws(IllegalArgumentException::class)
  suspend fun stop()
}