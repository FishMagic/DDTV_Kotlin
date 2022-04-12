package me.ftmc

import java.util.Scanner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import me.ftmc.action.Action
import me.ftmc.action.ActionType
import me.ftmc.message.MessageType

@ObsoleteCoroutinesApi
class CliCommon(middleLayer: MiddleLayer) : Frontend {
  override var isStarted = false
  override val coroutineScope = CoroutineScope(Job())
  override val messageReceiveChannel = middleLayer.messageChannel.openSubscription()
  override val actionChannel = middleLayer.actionChannel
  private val scanner = Scanner(System.`in`)

  @Throws(IllegalArgumentException::class)
  override suspend fun start() {
    require(!isStarted)
    coroutineScope.launch {
      while (true) {
        val message = messageReceiveChannel.receive()
        when (message.type) {
          MessageType.HELLO -> println(message.data)
          else -> {}
        }
        yield()
      }
    }
    println("前端初始化完成，开始监听输入")
    isStarted = true
    while (true) {
      if (scanner.hasNext()) {
        actionChannel.send(Action(ActionType.HELLO, scanner.next()))
      }
    }
  }

  @Throws(IllegalArgumentException::class)
  override suspend fun stop() {
    require(isStarted)
    println("前端正在退出")
    scanner.close()
    messageReceiveChannel.cancel()
    println("前端已退出")
  }
}