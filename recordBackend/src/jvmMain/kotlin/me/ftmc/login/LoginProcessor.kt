package me.ftmc.login

import io.ktor.client.call.NoTransformationFoundException
import io.ktor.client.call.body
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.http.HttpHeaders
import io.ktor.http.Parameters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import me.ftmc.LogHolder
import me.ftmc.jsonProcessor
import me.ftmc.message.LoginStateChangeMessageData
import me.ftmc.message.Message
import me.ftmc.message.MessageType
import me.ftmc.recordBackedHTTPClient

@Serializable
data class LoginQRCodeURL(
  val code: Int, val `data`: LoginQRCodeURLData, val status: Boolean, val ts: Int
)

@Serializable
data class LoginQRCodeURLData(
  val oauthKey: String, val url: String
)

@Serializable
data class LoginResult(
  val `data`: Int, val message: String, val status: Boolean
)

@Serializable
data class LoginResultSuccess(
  val code: Int, val status: Boolean, val ts: Int
)

class LoginProcessor(loginStateHolder: LoginStateHolder) : LoginClass {
  private val coroutineScope = CoroutineScope(Job())
  private val messageSendChannel = loginStateHolder.messageChannel
  private val logger = LogHolder()
  private var oauthKey: String? = null
  private var loginState = Int.MIN_VALUE
  private var qrRetryCount = 0
  private var listenerRetryCount = 0

  private val getQRCodeURL: suspend CoroutineScope.() -> Unit = {
    logger.info("[login processor] 开始获取登录二维码地址")
    while (loginState != 0) {
      val delayTime = if (qrRetryCount < 60) qrRetryCount * 100000L else 60000L
      try {
        getQRCodeURL()
        qrRetryCount = 0
      } catch (e: SocketTimeoutException) {
        logger.warn("[login processor] 发生网络错误")
        qrRetryCount++
        delay(delayTime)
        continue
      } catch (e: ConnectTimeoutException) {
        logger.warn("[login processor] 发生连接错误")
        qrRetryCount++
        delay(delayTime)
        continue
      } catch (e: NoTransformationFoundException) {
        logger.warn("[login processor] 解析登录二维码地址失败")
        qrRetryCount++
        continue
      } catch (e: Exception) {
        logger.errorCatch(e)
        qrRetryCount++
        delay(delayTime)
        continue
      }
      delay(150000L)
      logger.warn("[login processor] 二维码过期，正在重新获取")
    }
  }
  private val getLoginStatus: suspend CoroutineScope.() -> Unit = {
    logger.info("[login processor] 开始监听二维码扫描状态")
    while (loginState != 0) {
      if (oauthKey != null) {
        try {
          checkLoginProgress()
          listenerRetryCount = 0
        } catch (e: SocketTimeoutException) {
          logger.warn("[login processor] 发生网络错误")
          listenerRetryCount++
          delay(1000L)
          continue
        } catch (e: ConnectTimeoutException) {
          logger.warn("[login processor] 发生连接错误")
          listenerRetryCount++
          delay(1000L)
          continue
        } catch (e: NoTransformationFoundException) {
          logger.warn("[login processor] 解析登录二维码地址失败")
          listenerRetryCount++
          continue
        } catch (e: Exception) {
          logger.errorCatch(e)
          listenerRetryCount++
          delay(1000L)
          continue
        }
        delay(1000L)
      }
      yield()
    }
  }

  private var getQRCodeURLJob: Job? = null
  private var getLoginStatusJob: Job? = null

  override fun start() {
    logger.debug("[login processor] 开始初始化")
    runBlocking {
      getQRCodeURLJob = coroutineScope.launch(block = getQRCodeURL)
      getLoginStatusJob = coroutineScope.launch(block = getLoginStatus)
    }
    logger.debug("[login processor] 初始化完成")
  }

  override fun stop() {
    coroutineScope.cancel()
    logger.debug("[login processor] 已停止")
  }

  private suspend fun getQRCodeURL() {
    val qrCodeResponse = withContext(Dispatchers.IO) {
      recordBackedHTTPClient.get("https://passport.bilibili.com/qrcode/getLoginUrl").body<LoginQRCodeURL>()
    }
    val qrCodeData = qrCodeResponse.data
    oauthKey = qrCodeData.oauthKey
    messageSendChannel.emit(
      Message(
        MessageType.LOGIN_QR_CODE_CHANGE, data = qrCodeData.url
      )
    )
  }

  private suspend fun checkLoginProgress() {
    val loginResultResponse = withContext(Dispatchers.IO) {
      recordBackedHTTPClient.submitForm(
        "https://passport.bilibili.com/qrcode/getLoginInfo",
        formParameters = Parameters.build {
          append("oauthKey", oauthKey!!)
        }) {
        headers {
          append(
            HttpHeaders.Referrer, "https://passport.bilibili.com/login"
          )
        }
      }
    }
    val loginResultString = withContext(Dispatchers.IO) { loginResultResponse.body<String>() }
    val loginResultResult = jsonProcessor.parseToJsonElement(loginResultString)
    val loginResultStatus = loginResultResult.jsonObject["status"]?.jsonPrimitive?.boolean ?: false
    if (loginResultStatus) {
      loginSuccess(loginResultResult)
    } else {
      loggingIn(loginResultResult)
    }
  }

  private suspend fun loggingIn(loginResultResult: JsonElement): Boolean {
    val loginResultBody = jsonProcessor.decodeFromJsonElement<LoginResult>(
      loginResultResult
    )
    if (loginResultBody.data != loginState) {
      loginState = loginResultBody.data
      messageSendChannel.emit(
        Message(
          MessageType.LOGIN_STATE_CHANGE, Json.encodeToString(
            LoginStateChangeMessageData(newValue = loginResultBody.data, msg = loginResultBody.message)
          )
        )
      )
    }
    return false
  }

  private suspend fun loginSuccess(loginResultResult: JsonElement) {
    val loginResultBody = jsonProcessor.decodeFromJsonElement<LoginResultSuccess>(
      loginResultResult
    )
    loginState = loginResultBody.code
    messageSendChannel.emit(
      Message(
        MessageType.LOGIN_STATE_CHANGE,
        Json.encodeToString(LoginStateChangeMessageData(newValue = loginResultBody.code, msg = "登录成功"))
      )
    )
    logger.info("[login processor] 登录成功，停止登录流程")
    loginState = 0
  }
}
