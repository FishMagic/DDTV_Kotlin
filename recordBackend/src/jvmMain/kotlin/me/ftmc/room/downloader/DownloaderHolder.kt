package me.ftmc.room.downloader

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import me.ftmc.message.Message
import me.ftmc.room.Room

class DownloaderHolder(room: Room) {
  private val coroutineScope = CoroutineScope(Job())
  private val messageSendChannel = room.messageReceiveChannel
  val messageReceiveChannel = MutableSharedFlow<Message>()
  private val flvDownloader = FLVDownloader(this)
  private val danmakuDownloader = DanmakuDownloader(this)

  fun start() {
    flvDownloader.start()
    danmakuDownloader.start()
  }

  fun cancel() {
    danmakuDownloader.cancel()
    flvDownloader.cancel()
    coroutineScope.cancel()
  }
}