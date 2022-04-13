package me.ftmc

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import me.ftmc.action.Action
import me.ftmc.message.Message

interface Backend {
  val coroutineScope: CoroutineScope
  val messageSendChannel: MutableSharedFlow<Message>
  val actionReceiveChannel: MutableSharedFlow<Action>

  suspend fun start()
  suspend fun stop()
}