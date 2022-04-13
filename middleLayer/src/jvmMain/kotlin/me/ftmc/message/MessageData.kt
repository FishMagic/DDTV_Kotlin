package me.ftmc.message

import kotlinx.serialization.Serializable

/**
 * 通用信息
 * code:
 * 0 -> 操作成功
 * -1 -> 程序内部错误
 * -2 -> Bilibili方面错误
 * -3 -> 网络错误
 *
 * command:
 * 0 -> 添加，选择
 * 1 -> 删除
 */

/**
 * @param second: 消息发送时间（秒）
 * @param msg: 需要发送的信息
 */
@Serializable
data class HelloMessageData(
  val second: Long,
  val msg: String
)

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
data class RoomCommandMessageData(
  val code: Int,
  val command: Int,
  val msg: String,
  val uid: Long
)

/**
 * @param code: 操作结果
 * @param msg: 结果说明
 * @param uid: 被操作房间的uid
 * @param key: 被操作的配置名
 * @param newValue: 新的值
 */
@Serializable
data class RoomConfigEditMessageData(
  val code: Int,
  val msg: String,
  val uid: Long,
  val key: String,
  val newValue: String
)

/**
 * @param code: 操作结果
 * @param msg: 结果附言
 * @param uid: 发生变化房间的uid
 * @param newValue: 新的值
 */
@Serializable
data class RecordCommandMessageData(
  val code: Int,
  val msg: String,
  val uid: Long,
  val newValue: String
)


/**
 * @param uid: 发生变化的房间的uid
 * @param newValue: 新的值
 */
@Serializable
data class LiveStateMessageData(
  val uid: Long,
  val newValue: String
)

/**
 * @param uid: 发生变化的房间的uid
 * @param msg: 变化原因
 * @param newValue: 新的值
 */
@Serializable
data class RecordStateMessageData(
  val uid: Long,
  val msg: String,
  val newValue: String
)

/**
 * @param code: 操作结果
 * @param msg: 结果说明
 * @param key: 被操作的配置名
 * @param newValue: 新的值
 */
@Serializable
data class GlobalConfigEditMessageData(
  val code: Int,
  val msg: String,
  val key: String,
  val newValue: String
)

/**
 * @param msg: 变化原因
 * @param newValue: 新的值
 */
@Serializable
data class LoginStateChangeMessageData(
  val newValue: Int,
  val msg: String
)

/**
 * @param second: 消息发送时间（秒）
 * @param msg: 需要发送的信息
 */
@Serializable
data class ByeMessageData(val second: Long, val msg: String)