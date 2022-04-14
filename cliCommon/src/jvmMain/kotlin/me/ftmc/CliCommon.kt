package me.ftmc

import java.util.Scanner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import me.ftmc.action.Action
import me.ftmc.action.ActionType
import me.ftmc.action.LoginStateChangeActionDate

class CliCommon(middleLayer: MiddleLayer) : Frontend {
  override val coroutineScope = CoroutineScope(Job())
  override val messageReceiveChannel = middleLayer.messageChannel
  override val actionChannel = middleLayer.actionChannel
  private val scanner = Scanner(System.`in`)
  private val logger = LogHolder()

  private val messageCollection: suspend CoroutineScope.() -> Unit = {
    logger.debug("[cliCommon] 开始监听消息上传队列")
    messageReceiveChannel.collect { message ->
      println(message.data)
    }
  }
  private val inputCollection: suspend CoroutineScope.() -> Unit = {
    while (true) {
      if (scanner.hasNext()) {
        val string = scanner.nextLine().lowercase()
        if (string == "exit") {
          break
        } else if (string == "logout") {
          actionChannel.emit(
            Action(
              ActionType.LOGIN_STATE_CHANGE,
              Json.encodeToString(LoginStateChangeActionDate(3, "退出登录"))
            )
          )
        } else {
          actionChannel.emit(Action(ActionType.HELLO, string))
        }
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
    coroutineScope.cancel()
    scanner.close()
    logger.info("[cliCommon] 已停止")
  }
}