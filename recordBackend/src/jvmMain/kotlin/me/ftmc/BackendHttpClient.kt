package me.ftmc

import io.ktor.client.HttpClient
import io.ktor.client.plugins.UserAgent
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.CookiesStorage
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.http.Cookie
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

fun Cookie.fillDefaults(requestUrl: Url): Cookie {
  var result = this

  if (result.path?.startsWith("/") != true) {
    result = result.copy(path = requestUrl.encodedPath)
  }

  if (result.domain.isNullOrBlank()) {
    result = result.copy(domain = requestUrl.host)
  }

  return result
}

class CustomCookiesStorage(vararg cookies: Cookie) : CookiesStorage {
  private val storage = cookies.map {
    it.fillDefaults(
      URLBuilder().build()
    )
  }.toMutableList()

  override suspend fun get(requestUrl: Url): List<Cookie> = storage

  override suspend fun addCookie(requestUrl: Url, cookie: Cookie) {
    storage.add(cookie.fillDefaults(requestUrl))
  }

  fun addCookie(cookie: Cookie) {
    storage.add((cookie))
  }

  fun getCookie(): List<Cookie> = storage

  fun clearCookie() = storage.clear()

  override fun close() {}

}

val cookiesStorage = CustomCookiesStorage()

val recordBackedHTTPClient = HttpClient {
  install(ContentNegotiation) {
    json(Json {
      ignoreUnknownKeys = true
    })
  }
  install(WebSockets)
  install(UserAgent) {
    agent =
      "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.4896.75 Safari/537.36 Edg/100.0.1185.36"
  }
  install(HttpCookies) {
    storage = cookiesStorage
  }
}

val recordBackedHTTPClientWithOutCookie = HttpClient {
  install(ContentNegotiation) {
    json(Json {
      ignoreUnknownKeys = true
    })
  }
  install(UserAgent) {
    agent =
      "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.4896.75 Safari/537.36 Edg/100.0.1185.36"
  }
}

val jsonProcessor = Json {
  ignoreUnknownKeys = true
}