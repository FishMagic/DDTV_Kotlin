package me.ftmc.room

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import me.ftmc.message.Message
import me.ftmc.room.downloader.DownloaderHolder

@ObsoleteCoroutinesApi
class Room(val uid: Long, private val roomHolder: RoomHolder) {
  private val coroutineScope = CoroutineScope(Job())
  private val messageSendChannel = roomHolder.messageChannel
  val messageChannel = BroadcastChannel<Message>(Channel.BUFFERED)
  private val messageReceiveChannel = messageChannel.openSubscription()
  val downloaderHolder = DownloaderHolder(this)
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
    messageReceiveChannel.cancel()
    messageChannel.close()
  }
}