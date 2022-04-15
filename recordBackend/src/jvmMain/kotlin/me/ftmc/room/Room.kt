package me.ftmc.room

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import me.ftmc.RoomConfig
import me.ftmc.RoomData
import me.ftmc.message.Message
import me.ftmc.message.MessageType
import me.ftmc.room.downloader.DownloaderHolder


class Room(private val uid: Long, val config: RoomConfig, private val roomHolder: RoomHolder) {
  private val roomInfo = RoomInfo()
  private val coroutineScope = CoroutineScope(Job())
  private val messageSendChannel = roomHolder.messageReceiveChannel
  val messageReceiveChannel = MutableSharedFlow<Message>()
  private val downloaderHolder = DownloaderHolder(this)

  fun updateRoomInfo(newRoomInfo: RoomInfo) {
    roomInfo.roomId = newRoomInfo.roomId
    roomInfo.title = newRoomInfo.title
    roomInfo.username = newRoomInfo.username
    if (roomInfo.liveState != newRoomInfo.liveState) {
      roomInfo.liveState = newRoomInfo.liveState
      coroutineScope.launch {
        messageSendChannel.emit(
          Message(
            if (roomInfo.liveState) {
              MessageType.LIVE_START
            } else {
              MessageType.LIVE_STOP
            }, uid.toString()
          )
        )
      }
      if (config.isAutoRecord) {
        downloaderHolder.recordStart()
      }
    }
  }

  fun getRoomData(): RoomData {
    return RoomData(
      roomInfo.username,
      roomInfo.roomId,
      roomInfo.title,
      downloaderHolder.getIsRecording(),
      downloaderHolder.recordStartTime,
      config.isAutoRecord,
      config.isDanmakuRecord
    )
  }

  fun del() {
    downloaderHolder.cancel()
    coroutineScope.cancel()
  }
}