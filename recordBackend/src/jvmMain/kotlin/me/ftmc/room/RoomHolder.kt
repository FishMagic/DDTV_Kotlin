package me.ftmc.room

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import me.ftmc.RecordBackend
import me.ftmc.message.Message

@ObsoleteCoroutinesApi
class RoomHolder(backend: RecordBackend) {
  private val isStarted = false
  private val roomMap = mutableMapOf<Long, Room>()
  private val coroutineScope = CoroutineScope(Job())
  private val messageSendChannel = backend.messageChannel
  val messageChannel = BroadcastChannel<Message>(Channel.BUFFERED)
  private val messageReceiveChannel = messageChannel.openSubscription()

  suspend fun start() {
    require(!isStarted)
    coroutineScope.launch {
      while (true) {
        val message = messageReceiveChannel.receive()
        yield()
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
    messageReceiveChannel.cancel()
    messageChannel.close()
  }
}