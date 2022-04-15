package me.ftmc.room

import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import me.ftmc.LogHolder
import me.ftmc.RecordBackend
import me.ftmc.RoomConfig
import me.ftmc.RoomData
import me.ftmc.jsonProcessor
import me.ftmc.message.Message
import me.ftmc.message.MessageType
import me.ftmc.recordBackedHTTPClientWithOutCookie

@Serializable
data class RoomInfoResponse(val data: Map<Long, UidInfo>)

@Serializable
data class UidInfo(val live_status: Int, val room_id: Long, val title: String, val uname: String)

class RoomHolder(backend: RecordBackend) {
  private val roomMap = mutableMapOf<Long, Room>()
  private val coroutineScope = CoroutineScope(Job())
  private val messageSendChannel = backend.messageReceiveChannel
  val messageReceiveChannel = MutableSharedFlow<Message>()
  private val logger = LogHolder()

  private val roomInfoUpdater: suspend CoroutineScope.() -> Unit = {
    while (true) {
      if (roomMap.isNotEmpty()) {
        logger.debug("[room holder] 正在获取房间信息")
        val httpResponse =
          recordBackedHTTPClientWithOutCookie.post("https://api.live.bilibili.com/room/v1/Room/get_status_info_by_uids") {
            setBody(jsonProcessor.encodeToString(mutableMapOf(Pair("uids", roomMap.keys.toList()))))
          }.body<RoomInfoResponse>()
        httpResponse.data.forEach { (uid, info) ->
          roomMap[uid]?.updateRoomInfo(RoomInfo(info.uname, info.room_id, info.title, info.live_status == 1))
        }
      }
      delay(10000L)
    }
  }
  private val messageCollection: suspend CoroutineScope.() -> Unit = {
    messageReceiveChannel.collect { message ->
      messageSendChannel.emit(message)
    }
  }

  private var roomInfoUpdateJob: Job? = null
  private var messageCollectionJob: Job? = null

  suspend fun start() {
    runBlocking {
      roomInfoUpdateJob = coroutineScope.launch(block = roomInfoUpdater)
      messageCollectionJob = coroutineScope.launch(block = messageCollection)
    }

  }

  fun setRoomList(roomList: MutableMap<Long, RoomConfig>) {
    roomList.forEach { (uid, config) ->
      roomMap[uid] = Room(uid, config, this)
    }
  }

  fun getRoomList(): MutableMap<Long, RoomData> {
    val tempRoomMap = mutableMapOf<Long, RoomData>()
    roomMap.forEach { (uid, room) ->
      tempRoomMap[uid] = room.getRoomData()
    }
    return tempRoomMap
  }

  fun addRoom(uid: Long, config: RoomConfig) {
    roomMap[uid] = Room(uid, config, this)
    coroutineScope.launch { messageSendChannel.emit(Message(MessageType.ROOM_ADD, uid.toString())) }
  }

  fun delRoom(uid: Long) {
    roomMap.remove(uid)?.del()
    coroutineScope.launch { messageSendChannel.emit(Message(MessageType.ROOM_DEL, uid.toString())) }
  }

  fun stop() {
    roomMap.values.forEach {
      it.del()
    }
    coroutineScope.cancel()
  }
}