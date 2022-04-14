package me.ftmc

import java.util.Scanner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import me.ftmc.action.Action
import me.ftmc.action.ActionType
import me.ftmc.message.MessageType

class CliCommon(middleLayer: MiddleLayer) : Frontend {
  override val coroutineScope = CoroutineScope(Job())
  override val messageReceiveChannel = middleLayer.messageChannel
  override val actionChannel = middleLayer.actionChannel
  private val scanner = Scanner(System.`in`)
  private val logger = LogHolder()

  private val messageCollection: suspend CoroutineScope.() -> Unit = {
    logger.debug("[cliCommon] 开始监听消息上传队列")
    messageReceiveChannel.collect { message ->
      when (message.type) {
        MessageType.HELLO -> {
          println(message.data)
        }
        MessageType.LOGIN_QR_CODE_CHANGE -> {
          logger.info("已获取到新的登录地址")
          println("请自行生成该链接的二维码并扫描")
          println("可以使用： 草料二维码")
          println(message.data)
          println("该链接有效期150秒")
        }
        MessageType.LOGIN_QR_SCAN -> {
          logger.info("二维码已被扫描")
        }
        MessageType.LOGIN_SUCCESS -> {
          logger.info("登录成功")
        }
        MessageType.LOGIN_FAILURE -> {
          logger.warn("登录已失效")
        }
      }
    }
  }
  private val inputCollection: suspend CoroutineScope.() -> Unit = {
    while (true) {
      if (scanner.hasNext()) {
        val string = scanner.nextLine().lowercase()
        if (string == "exit") {
          break
        } else if (string == "logout") {
          actionChannel.emit(Action(ActionType.LOGOUT))
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