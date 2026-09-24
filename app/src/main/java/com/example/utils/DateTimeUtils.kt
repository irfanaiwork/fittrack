package com.example.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateTimeUtils {
    private val isoDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val time24Format = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val time12Format = SimpleDateFormat("h:mm a", Locale.getDefault())
    private val displayDateFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
    private val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    private val shortMonthDayFormat = SimpleDateFormat("MMMM d", Locale.getDefault())

    fun getTodayIso(): String {
        return isoDateFormat.format(Date())
    }

    fun formatDisplayDate(dateIso: String): String {
        return try {
            val date = isoDateFormat.parse(dateIso) ?: Date()
            displayDateFormat.format(date)
        } catch (e: Exception) {
            dateIso
        }
    }

    fun formatMonthYear(calendar: Calendar): String {
        return monthYearFormat.format(calendar.time)
    }

    fun formatShortMonthDay(dateIso: String): String {
        return try {
            val date = isoDateFormat.parse(dateIso) ?: Date()
            shortMonthDayFormat.format(date)
        } catch (e: Exception) {
            dateIso
        }
    }

    fun format12Hour(time24: String): String {
        return try {
            val date = time24Format.parse(time24) ?: return time24
            time12Format.format(date)
        } catch (e: Exception) {
            time24
        }
    }

    fun format24Hour(hour: Int, minute: Int): String {
        return String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
    }

    fun parseHourMinute(time24: String): Pair<Int, Int> {
        return try {
            val parts = time24.split(":")
            val hour = parts[0].toIntOrNull() ?: 8
            val min = parts[1].toIntOrNull() ?: 0
            Pair(hour, min)
        } catch (e: Exception) {
            Pair(8, 0)
        }
    }

    /**
     * Returns 1 for Monday, 2 for Tuesday, ..., 7 for Sunday
     */
    fun getCurrentDayOfWeek(): Int {
        val cal = Calendar.getInstance()
        return when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4
            Calendar.FRIDAY -> 5
            Calendar.SATURDAY -> 6
            Calendar.SUNDAY -> 7
            else -> 1
        }
    }

    fun getTodayDayOfWeek(): Int = getCurrentDayOfWeek()

    fun getCurrentTime24(): String {
        return time24Format.format(Date())
    }

    fun getDayOfWeekName(day: Int): String {
        return when (day) {
            1 -> "Monday"
            2 -> "Tuesday"
            3 -> "Wednesday"
            4 -> "Thursday"
            5 -> "Friday"
            6 -> "Saturday"
            7 -> "Sunday"
            else -> "Day"
        }
    }

    fun getDayOfWeekShortName(day: Int): String {
        return when (day) {
            1 -> "Mon"
            2 -> "Tue"
            3 -> "Wed"
            4 -> "Thu"
            5 -> "Fri"
            6 -> "Sat"
            7 -> "Sun"
            else -> ""
        }
    }

    fun getGreeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when {
            hour in 5..11 -> "Good Morning 👋"
            hour in 12..16 -> "Good Afternoon 👋"
            hour in 17..21 -> "Good Evening 👋"
            else -> "Good Night 🌙"
        }
    }

    fun isPastTimeToday(time24: String): Boolean {
        val now = Calendar.getInstance()
        val currentHour = now.get(Calendar.HOUR_OF_DAY)
        val currentMinute = now.get(Calendar.MINUTE)
        val (targetHour, targetMinute) = parseHourMinute(time24)

        return (currentHour > targetHour) || (currentHour == targetHour && currentMinute > targetMinute)
    }

    /**
     * Gets start and end dates (Monday to Sunday) for the given week offset.
     * offset = 0 is current week, -1 is previous week, +1 is next week.
     */
    fun getWeekDateRange(weekOffset: Int = 0): Pair<String, String> {
        val cal = Calendar.getInstance()
        cal.firstDayOfWeek = Calendar.MONDAY
        // Adjust for week offset
        cal.add(Calendar.WEEK_OF_YEAR, weekOffset)
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        val startIso = isoDateFormat.format(cal.time)

        cal.add(Calendar.DAY_OF_WEEK, 6) // Sunday
        val endIso = isoDateFormat.format(cal.time)

        return Pair(startIso, endIso)
    }

    /**
     * Formats week range text e.g. "September 14 – September 20"
     */
    fun formatWeekRangeDisplay(startIso: String, endIso: String): String {
        return "${formatShortMonthDay(startIso)} – ${formatShortMonthDay(endIso)}"
    }

    /**
     * Returns list of 7 dates (Monday through Sunday) for week
     */
    fun getWeekDates(weekOffset: Int = 0): List<Pair<Int, String>> {
        val cal = Calendar.getInstance()
        cal.firstDayOfWeek = Calendar.MONDAY
        cal.add(Calendar.WEEK_OF_YEAR, weekOffset)
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

        val list = mutableListOf<Pair<Int, String>>()
        for (day in 1..7) {
            list.add(Pair(day, isoDateFormat.format(cal.time)))
            cal.add(Calendar.DAY_OF_WEEK, 1)
        }
        return list
    }

    /**
     * Computes the next trigger time in millis for a 24-hour time "HH:mm".
     * If dayOfWeek is provided (1..7), schedules for that day of week.
     * Otherwise schedules for today (or tomorrow if today's time has passed).
     */
    fun computeTriggerMillis(time24: String, targetDayOfWeek: Int? = null): Long {
        val (hour, minute) = parseHourMinute(time24)
        val now = Calendar.getInstance()
        val trigger = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (targetDayOfWeek != null) {
            val calendarDay = when (targetDayOfWeek) {
                1 -> Calendar.MONDAY
                2 -> Calendar.TUESDAY
                3 -> Calendar.WEDNESDAY
                4 -> Calendar.THURSDAY
                5 -> Calendar.FRIDAY
                6 -> Calendar.SATURDAY
                7 -> Calendar.SUNDAY
                else -> Calendar.MONDAY
            }
            trigger.set(Calendar.DAY_OF_WEEK, calendarDay)
            if (trigger.before(now)) {
                trigger.add(Calendar.WEEK_OF_YEAR, 1)
            }
        } else {
            if (trigger.before(now)) {
                trigger.add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        return trigger.timeInMillis
    }
}
