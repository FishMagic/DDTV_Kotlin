package me.ftmc.room.downloader

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import me.ftmc.message.Message
import me.ftmc.room.Room

@ObsoleteCoroutinesApi
class DownloaderHolder(room: Room) {
  private val coroutineScope = CoroutineScope(Job())
  private val messageSendChannel = room.messageChannel
  val messageChannel = BroadcastChannel<Message>(Channel.BUFFERED)
  private val messageReceiveChannel = messageChannel.openSubscription()
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
    messageReceiveChannel.cancel()
    messageChannel.close()
  }
}