package me.ftmc

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ReceiveChannel
import me.ftmc.action.Action
import me.ftmc.message.Message

@ObsoleteCoroutinesApi
interface Backend {
  var isStarted: Boolean
  val coroutineScope: CoroutineScope
  val messageSendChannel: BroadcastChannel<Message>
  val actionReceiveChannel: ReceiveChannel<Action>

  suspend fun start()
  suspend fun stop()
}