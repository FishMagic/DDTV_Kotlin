package me.ftmc

import kotlinx.coroutines.flow.MutableSharedFlow
import me.ftmc.action.Action
import me.ftmc.message.Message

class MiddleLayer {
  val messageChannel = MutableSharedFlow<Message>()
  val actionChannel = MutableSharedFlow<Action>()
}