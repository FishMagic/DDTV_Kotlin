package me.ftmc

import java.util.Scanner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import me.ftmc.action.Action
import me.ftmc.action.ActionType

class CliCommon(middleLayer: MiddleLayer) : Frontend {
  override val coroutineScope = CoroutineScope(Job())
  override val messageReceiveChannel = middleLayer.messageChannel
  override val actionChannel = middleLayer.actionChannel
  private val scanner = Scanner(System.`in`)
  private val logger = LogHolder()

  private val messageCollection: suspend CoroutineScope.() -> Unit = {
    logger.info("[cliCommon] 开始监听消息上传队列")
    messageReceiveChannel.collect { message ->
      println(message.data)
    }
  }
  private val inputCollection: suspend CoroutineScope.() -> Unit = {
    while (true) {
      if (scanner.hasNext()) {
        val string = scanner.nextLine()
        if (string.lowercase() == "exit") {
          break
        }
        actionChannel.emit(Action(ActionType.HELLO, string))
      }
    }
  }

  private var messageCollectionJob: Job? = null
  private var inputCollectionJob: Job? = null

  override suspend fun start() {
    logger.info("[cliCommon] 初始化开始")
    runBlocking { messageCollectionJob = coroutineScope.launch(block = messageCollection) }
    logger.info("[cliCommon] 初始化完成")
    inputCollectionJob = coroutineScope.launch(block = inputCollection)
    inputCollectionJob?.join()
  }

  override suspend fun stop() {
    logger.info("[cliCommon] 开始尝试停止")
    runBlocking {
      messageCollectionJob?.cancelAndJoin()
      inputCollectionJob?.cancelAndJoin()
    }
    coroutineScope.cancel()
    scanner.close()
    logger.info("[cliCommon] 已停止")
  }
}