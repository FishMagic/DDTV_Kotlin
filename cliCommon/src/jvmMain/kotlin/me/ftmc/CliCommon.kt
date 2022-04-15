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
          println("点击下方链接获取二维码")
          println("https://api.qrserver.com/v1/create-qr-code/?size=150x150&data=${message.data}")
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
        MessageType.LIVE_START -> {
          logger.info("${message.data} 开播了")
        }
        MessageType.LIVE_STOP -> {
          logger.info("${message.data} 下播了")
        }
        MessageType.ROOM_ADD -> {
          logger.info("${message.data} 房间已添加")
        }
        MessageType.ROOM_DEL -> {
          logger.info("${message.data} 房间已删除")
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
        } else {
          inputProcessor(string)
        }
      }
    }
  }

  private var messageCollectionJob: Job? = null
  private var inputCollectionJob: Job? = null

  private suspend fun inputProcessor(inputString: String) {
    val args = inputString.lowercase().split(' ')
    if (args[0] == "room") {
      if (args[1] == "add") {
        actionChannel.emit(Action(ActionType.ROOM_ADD, data = args[2]))
        return
      } else if (args[2] == "del") {
        actionChannel.emit(Action(ActionType.ROOM_DEL, data = args[2]))
        return
      }
    }
    if (args[0] == "logout") {
      actionChannel.emit(Action(ActionType.LOGOUT))
      return
    }
    logger.warn("[cliCommon] 无法识别的指令")
  }

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