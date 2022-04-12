package me.ftmc.action

import kotlinx.serialization.Serializable

@Serializable
data class Action(
  val type: ActionType,
  val data: String,
  val from: String = ""
)