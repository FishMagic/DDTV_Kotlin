package me.ftmc

import java.util.Scanner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import me.ftmc.action.Action
import me.ftmc.action.ActionType

class CliCommon(middleLayer: MiddleLayer) : Frontend {
  override val coroutineScope = CoroutineScope(Job())
  override val messageReceiveChannel = middleLayer.messageChannel
  override val actionChannel = middleLayer.actionChannel
  private val scanner = Scanner(System.`in`)
  private val logger = LogHolder()
  private var isStarted = false

  @Throws(IllegalArgumentException::class)
  override suspend fun start() {
    logger.info("[cliCommon] 初始化开始")
    coroutineScope.launch {
      logger.info("[cliCommon] 开始监听消息上传队列")
      messageReceiveChannel.collect { message ->
        println(message.data)
      }
    }
    logger.info("[cliCommon] 初始化完成")
    isStarted = true
    coroutineScope.launch {
      while (isStarted) {
        if (scanner.hasNext()) {
          actionChannel.emit(Action(ActionType.HELLO, scanner.next()))
        }
      }
    }.join()
  }

  @Throws(IllegalArgumentException::class)
  override suspend fun stop() {
    isStarted = false
    coroutineScope.cancel()
    scanner.close()
    logger.info("[cliCommon] 已停止")
  }
}