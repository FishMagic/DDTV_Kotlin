package me.ftmc.message

import kotlinx.serialization.Serializable

/**
 * @param second: 消息发送时间（秒）
 * @param msg: 需要发送的信息
 */
@Serializable
data class HelloMessageData(val second: Long, val msg: String)

/**
 * @param second: 消息发送的时间（秒）
 */
@Serializable
data class HeartbeatMessageData(val second: Long)

/**
 * @param code: 操作结果
 * @param command: 操作类型
 * @param msg: 结果说明
 * @param uid: 被操作房间的uid
 */
@Serializable
data class RoomCommandMessageData(val code: Int, val command: Int, val msg: String, val uid: Long)

/**
 * @param code: 操作结果
 * @param msg: 结果说明
 * @param uid: 被操作房间的uid
 * @param key: 被操作的配置名
 * @param newValue: 新的
 */
@Serializable
data class RoomConfigEditMessageData(
  val code: Int,
  val msg: String,
  val uid: Long,
  val key: String,
  val newValue: String
)

@Serializable
data class RecordCommandMessageData(val code: Int, val msg: String, val uid: Long, val newValue: String)

@Serializable
data class LiveStateMessageData(val uid: Long, val newValue: String)

@Serializable
data class RecordStateMessageData(val uid: Long, val newValue: String, val msg: String)

@Serializable
data class GlobalConfigEditMessageData(val code: Int, val msg: String, val key: String, val newValue: String)

@Serializable
data class LoginStateChange(val newValue: String, val msg: String)

@Serializable
data class ByeMessageData(val second: Long, val msg: String)