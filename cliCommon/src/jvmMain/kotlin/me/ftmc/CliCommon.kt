package me.ftmc

import java.util.Scanner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import me.ftmc.action.Action
import me.ftmc.action.ActionType
import me.ftmc.message.MessageType

class CliCommon(middleLayer: MiddleLayer) : Frontend {
  override val coroutineScope = CoroutineScope(Job())
  override val messageReceiveChannel = middleLayer.messageChannel
  override val actionChannel = middleLayer.actionChannel
  private val scanner = Scanner(System.`in`)
  private var isStarted = false

  @Throws(IllegalArgumentException::class)
  override suspend fun start() {
    coroutineScope.launch {
      messageReceiveChannel.collect { message ->
        when (message.type) {
          MessageType.HELLO -> println(message.data)
          else -> {}
        }
      }
    }
    println("前端初始化完成，开始监听输入")
    isStarted = true
    while (isStarted) {
      if (scanner.hasNext()) {
        actionChannel.emit(Action(ActionType.HELLO, scanner.next()))
      }
    }
  }

  @Throws(IllegalArgumentException::class)
  override suspend fun stop() {
    isStarted = false
    println("前端正在退出")
    coroutineScope.cancel()
    scanner.close()
    println("前端已退出")
  }
}