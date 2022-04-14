package me.ftmc.message

import kotlinx.serialization.Serializable

@Serializable
data class Message(
  val type: MessageType,
  val data: String = "",
  val to: String? = null
)