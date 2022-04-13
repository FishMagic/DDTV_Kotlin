package me.ftmc

import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

val formatTimeToISO: (Long) -> String = { longTime ->
  val dataTimeFormat = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
  dataTimeFormat.format(LocalDateTime.ofEpochSecond(longTime, 0, ZoneOffset.ofHours(8)))
}

val formatTimeToFileName: (Long) -> String = { longTime ->
  val dataTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd-hh-mm-ss")
  dataTimeFormat.format(LocalDateTime.ofEpochSecond(longTime, 0, ZoneOffset.ofHours(8)))
}

fun formatDataUnit(byte: Long): String {
  return if (byte < 1024L) {
    String.format("%s B", byte.toString())
  } else if (byte < 1024L * 1024L) {
    String.format("%.2f KiB", byte.toFloat() / 1024)
  } else if (byte < 1024L * 1024L * 1024L) {
    String.format("%.2f MiB", (byte / 1024L).toFloat() / 1024)
  } else if (byte < 1024L * 1024L * 1024L * 1024L) {
    String.format("%.2f GiB", (byte / 1024L / 1024L).toFloat() / 1024)
  } else if (byte < 1024L * 1024L * 1024L * 1024L * 1024L) {
    String.format("%.2f TiB", (byte / 1024L / 1024L / 1024L).toFloat() / 1024)
  } else {
    String.format("%.2f PiB", (byte / 1024L / 1024L / 1024L / 1024L).toFloat() / 1024)
  }
}