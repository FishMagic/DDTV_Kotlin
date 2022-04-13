package me.ftmc

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import me.ftmc.action.Action
import me.ftmc.message.Message

interface Frontend {
  val coroutineScope: CoroutineScope
  val messageReceiveChannel: MutableSharedFlow<Message>
  val actionChannel: MutableSharedFlow<Action>

  @Throws(IllegalArgumentException::class)
  suspend fun start()

  @Throws(IllegalArgumentException::class)
  suspend fun stop()
}