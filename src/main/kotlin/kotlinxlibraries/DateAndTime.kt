package kotlinxlibraries

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime
import kotlin.time.Instant


/**
 * ### Date and Time library
 *
 * The library distinguishes between `Instant (an absolute point in time)`, `LocalDateTime (a
 * date and time without timezone)`, `LocalDate (just a date)`, and `LocalTime (just a time)`. This
 * separation prevents common bugs where developers accidentally mix timezone-aware and
 * timezone-naive timestamps.
 * ```
 * Core Types First — Know What You're Working WithInstant   → a single point in time (UTC) — like a timestamp
 * LocalDate        → just a date, no time, no timezone  (2024-03-21)
 * LocalTime        → just a time, no date, no timezone  (14:30:00)
 * LocalDateTime    → date + time, no timezone           (2024-03-21T14:30:00)
 * TimeZone         → a timezone                         (Europe/Dublin)
 * ZonedDateTime    → LocalDateTime + TimeZone (via conversion)
 * DateTimePeriod   → a duration between two dates       (2 years, 3 months)
 * ```
 * */
@OptIn(ExperimentalTime::class)
fun instant() {
    // From epoch milliseconds (e.g. from server timestamp)
    val instant = Instant.fromEpochMilliseconds(1711029000000L)
    println("Instant Millis:: $instant")

    // To epoch milliseconds (e.g. to send to server)
    val now: Instant = Clock.System.now()
    val millis = now.toEpochMilliseconds()
    println("Millis:: $millis")
}

@OptIn(ExperimentalTime::class)
fun getCurrentDateAndTime() {
    val now: Instant = Clock.System.now()
    println("Current instant: $now")

    // Convert to local date/time in a specific timezone
    val timezone = TimeZone.of("America/New_York")
    val localDateTime: LocalDateTime = now.toLocalDateTime(timezone)
    println("Local time in NYC: $localDateTime")
    // Get just the date
    val today: LocalDate = now.toLocalDateTime(timezone).date
    println("Today's date: $today")
}

/**
 * ### Working with Time Zones
 * Time zone handling is one of the most error-prone areas of date/time programming. kotlinxdatetime
 * makes timezone conversions explicit:
 * */
@OptIn(ExperimentalTime::class)
fun convertMeetingTime() {
    // A meeting scheduled at 2:00 PM Tokyo Time
    val tokyoTime = LocalDateTime(2026, 3, 21, 14, 0, 0, 0)
    val tokyoZone = TimeZone.of("Asia/Tokyo")

    // Convert to an instant (absolute point in time)
    val meetingInstant = tokyoTime.toInstant(tokyoZone)

    // What time is that in new york?
    val nyTimeZone = TimeZone.of("America/New_York")
    val nyTime = meetingInstant.toLocalDateTime(nyTimeZone)

    println("Meeting time in Tokyo: $tokyoTime")
    println("Meeting time in New York: $nyTime")

}

/**
 * ### Date Arithmetic
 * Adding and subtracting time is handled through the DateTimePeriod and DateTimeUnit
 * classes:
 * */

@OptIn(ExperimentalTime::class)
fun calculateDates() {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    // Add specific units
    val nextWeek = today.plus(7, DateTimeUnit.DAY)
    val nextMonth = today.plus(1, DateTimeUnit.MONTH)
    val nextYear = today.plus(1, DateTimeUnit.YEAR)

    // Using DatePeriod for complex additions
    val period = DatePeriod(years = 1, months = 2, days = 15)
    val futureDate = today.plus(period)
    println("Today: $today")
    println("Next week: $nextWeek")
    println("In 1 year, 2 months, 15 days: $futureDate")
}

/**
 * ### Duration and Period
 * kotlinx-datetime distinguishes between Duration (an exact amount of time) and DatePeriod
 * (a calendar-based period):
 * */
fun demonstrateDurationVsPeriod() {
    // Duration: exact time measurement
    val meetingLength: kotlin.time.Duration = 1.hours + 30.minutes
    // DatePeriod: calendar-based (varies by month/year)
    val subscriptionPeriod = DatePeriod(months = 1)

    val startDate = LocalDate(2025, 1, 31)
    val endDate = startDate.plus(subscriptionPeriod)
    // endDate is 2025-02-28, not an invalid date

    println("Meeting length: $meetingLength")
    println("Subscription end: $endDate")
}

/**
 * ### Converting instant and local datetime to and from the ISO 8601 string
 * `Instant`, `LocalDateTime`, `LocalDate` and `LocalTime` provide shortcuts for parsing and formatting them using the extended `ISO 8601 format`.
 * The `toString()` function is used to convert the value to a string in that format,
 * and the `parse` function in companion object is used to parse a string representation back.*/

fun parseAndFormat() {
    LocalDate.parse("2010-06-01")
    LocalTime.parse("12:01:03")
    LocalTime.parse("12:00:03.999")

    val date = LocalDate(2024, 3, 21)

    // Using FormatStrings (simple)
    val formatted = date.format(LocalDate.Formats.ISO)
    println(formatted)  // 2024-03-21

    // Custom format using builder
    // Formatting — format() with DateTimeFormat
    val customFormat = LocalDate.Format {
        day()
        char('/')
        monthNumber()
        char('/')
        year()
    }
    println(date.format(customFormat))  // 21/03/2024

    val dateTime = LocalDateTime(2024, 3, 21, 14, 30, 0)
   // Formatting LocalDateTime:
    val format = LocalDateTime.Format {
        day()
        char(' ')
        monthName(MonthNames.ENGLISH_FULL)  // "March"
        char(' ')
        year()
        chars(" at ")
        hour()
        char(':')
        minute()
    }

    println(dateTime.format(format))
// 21 March 2024 at 14:30

}

fun main() {
    getCurrentDateAndTime()
    convertMeetingTime()
    calculateDates()
    // Millis
    println("----- Millis ------")
    instant()
    parseAndFormat()
}