package me.ftmc.room

import kotlinx.serialization.Serializable

@Serializable
class RoomInfo(
  var username: String = "",
  var roomId: Long = 0,
  var title: String = "",
  var liveState: Boolean = false,
  var recordState: Boolean = false
)