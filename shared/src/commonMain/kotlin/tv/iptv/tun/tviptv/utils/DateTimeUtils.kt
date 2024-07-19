package tv.iptv.tun.tviptv.utils

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.format.char
import kotlinx.datetime.format.optional
import kotlinx.datetime.format.parse
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

val vietnamTimeZone: TimeZone
    get() = TimeZone.of("UTC+7")

fun String.formatCurrentTime(timeZone: TimeZone = vietnamTimeZone): Long {
    return DateTimeComponents.parse("20240718213000 +0700", DateTimeComponents.Format {
        year()
        monthNumber()
        dayOfMonth()
        hour()
        minute()
        second()
        optional {
            char(' ')
            chars("+0700")
        }
    }
    )
        .toLocalDateTime()
        .toInstant(timeZone)
        .toEpochMilliseconds()
}

fun Long.isToday(timeZone: TimeZone = vietnamTimeZone): Boolean {
    val instant = Instant.fromEpochSeconds(this)
    val convertedDate = instant.toLocalDateTime(timeZone).date
    val currentDate = Clock.System.now().toLocalDateTime(timeZone).date
    return convertedDate == currentDate
}