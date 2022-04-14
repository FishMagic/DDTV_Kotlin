package me.ftmc.action

import kotlinx.serialization.Serializable

/**
 * 通用信息
 * command:
 * 0 -> 添加，选择
 * 1 -> 删除
 */

/**
 * @param second: 消息发送时间（秒）
 * @param msg: 需要发送的信息
 */
@Serializable
data class HelloActionData(
  val second: Long,
  val msg: String
)

/**
 * @param second: 消息发送的时间（秒）
 */
@Serializable
data class HeartbeatActionData(val second: Long)

/**
 * @param command: 操作类型
 * @param msg: 结果说明
 * @param uid: 被操作房间的uid
 */
@Serializable
data class RoomCommandActionData(
  val command: Int,
  val msg: String,
  val uid: Long
)

/**
 * @param uid: 被操作房间的uid
 * @param key: 被操作的配置名
 * @param newValue: 新的值
 */
@Serializable
data class RoomConfigEditActionData(
  val uid: Long,
  val key: String,
  val newValue: String
)

/**
 * @param uid: 发生变化房间的uid
 * @param newValue: 新的值
 */
@Serializable
data class RecordCommandActionData(
  val uid: Long,
  val newValue: String
)

/**
 * @param key: 被操作的配置名
 * @param newValue: 新的值
 */
@Serializable
data class GlobalConfigEditActionData(
  val key: String,
  val newValue: String
)

/**
 * @param name: 服务器名字
 * @param url: 服务器地址
 * @param accessKeyId: 连接服务器的keyId
 * @param accessKeySecret: 连接服务器的keySecret
 */
@Serializable
data class ServerListAddActionData(
  val name: String,
  val url: String,
  val accessKeyId: String,
  val accessKeySecret: String,
)

/**
 * @param command: 操作类型
 * @param id: 服务器id
 */
@Serializable
data class ServerDelSelectActionData(
  val command: Int,
  val id: String
)

/**
 * @param newValue: 新的值
 * @param msg: 需要发送的信息
 */
@Serializable
data class LoginStateChangeActionDate(
  val newValue: Int,
  val msg: String,
)

/**
 * @param second: 消息发送时间（秒）
 * @param msg: 需要发送的信息
 */
@Serializable
data class ByeMessageData(val second: Long, val msg: String)