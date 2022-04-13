package me.ftmc.room

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import me.ftmc.RecordBackend
import me.ftmc.message.Message

class RoomHolder(backend: RecordBackend) {
  private val roomMap = mutableMapOf<Long, Room>()
  private val coroutineScope = CoroutineScope(Job())
  private val messageSendChannel = backend.messageReceiveChannel
  val messageReceiveChannel = MutableSharedFlow<Message>()

  suspend fun start() {
    coroutineScope.launch {
      messageReceiveChannel.collect { message ->
      }
    }
  }

  private fun addRoom(uid: Long) {
    return try {
      val newRoom = Room(uid, this)
      newRoom.start()
      roomMap[uid] = newRoom
    } catch (e: Exception) {
    }
  }

  fun delRoom(uid: Long) {
    return try {
      val theRoom = roomMap.remove(uid)
      if (theRoom != null) {
        theRoom.del()
      } else {
      }
    } catch (e: Exception) {
    }
  }

  fun stop() {
    roomMap.values.forEach {
      it.del()
    }
    coroutineScope.cancel()
  }
}