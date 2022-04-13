package me.ftmc.login

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import me.ftmc.RecordBackend
import me.ftmc.message.Message

class LoginStateHolder(recordBackend: RecordBackend) {
  private val coroutineScope = CoroutineScope(Job())
  private val messageSendChange = recordBackend.messageReceiveChannel
  val messageChannel = MutableSharedFlow<Message>()
  private var loginState = 0

  fun start() {
    TODO()
  }

  fun stop() {
  }

}