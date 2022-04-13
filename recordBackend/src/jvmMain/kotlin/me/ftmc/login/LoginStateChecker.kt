package me.ftmc.login

import io.ktor.client.call.NoTransformationFoundException
import io.ktor.client.call.body
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.request.get
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import me.ftmc.LogHolder
import me.ftmc.jsonProcessor
import me.ftmc.message.LoginStateChangeMessageData
import me.ftmc.message.Message
import me.ftmc.message.MessageType
import me.ftmc.recordBackedHTTPClient

@Serializable
data class NavResult(
  val code: Int,
  val `data`: NavResultData,
  val message: String,
  val ttl: Int
)

@Serializable
data class NavResultData(
  val isLogin: Boolean
)

class LoginStateChecker(loginStateHolder: LoginStateHolder) {
  private val coroutineScope = CoroutineScope(Job())
  private val messageSendChannel = loginStateHolder.messageChannel
  private val logger = LogHolder()
  private var retryCount = 0

  fun start() {
    coroutineScope.launch {
      logger.info("[login state checker] 开始监听登录状态")
      while (retryCount <= 3) {
        logger.info("[login state checker] 开始获取登录状态")
        try {
          val httpResponse = recordBackedHTTPClient.get("https://api.bilibili.com/x/web-interface/nav")
          val navResult = httpResponse.body<NavResult>()
          if (navResult.data.isLogin) {
            logger.debug("[login state checker] 登录状态无异常")
            delay(3600000)
          } else {
            logger.warn("[login state checker] 登录状态失效")
            messageSendChannel.emit(
              Message(
                MessageType.LOGIN_STATE_CHANGE, jsonProcessor.encodeToString(
                  LoginStateChangeMessageData(2, "登录失效")
                )
              )
            )
            this@LoginStateChecker.stop()
          }
        } catch (e: SocketTimeoutException) {
          logger.warn("[login state checker] 发生网络错误")
          retryCount++
          delay(1000L)
          continue
        } catch (e: ConnectTimeoutException) {
          logger.warn("[login state checker] 发生连接错误")
          retryCount++
          delay(1000L)
          continue
        } catch (e: NoTransformationFoundException) {
          logger.warn("[login state checker] 解析登录状态失败")
          retryCount++
          continue
        } catch (e: Exception) {
          logger.errorCatch(e)
          retryCount++
          delay(1000L)
          continue
        }
      }
    }
  }

  fun stop() {
    coroutineScope.cancel()
  }
}