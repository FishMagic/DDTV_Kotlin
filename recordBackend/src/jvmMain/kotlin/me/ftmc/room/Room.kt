package me.ftmc.room

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import me.ftmc.message.Message
import me.ftmc.room.downloader.DownloaderHolder

class Room(val uid: Long, private val roomHolder: RoomHolder) {
  private val coroutineScope = CoroutineScope(Job())
  private val messageSendChannel = roomHolder.messageReceiveChannel
  val messageReceiveChannel = MutableSharedFlow<Message>()
  private val downloaderHolder = DownloaderHolder(this)
  var userName = ""
  var roomId = 0L
  var title = ""
  var liveState = false
  var recordState = false

  fun start() {
    downloaderHolder.start()
  }

  fun del() {
    downloaderHolder.cancel()
    coroutineScope.cancel()
  }
}