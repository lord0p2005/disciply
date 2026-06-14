package com.example.ui.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    private val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    fun getTodayString(): String {
        return sdf.format(Date())
    }

    /**
     * Generates dates representing the past 22 weeks of columns (Monday to Sunday)
     * resulting in exactly 154 dates, ending on the Sunday of the current week.
     */
    fun getCalendarGridDates(weeksCount: Int = 22): List<String> {
        val dateList = mutableListOf<String>()
        val calendar = Calendar.getInstance()
        
        // Android calendar: Sunday = 1, Monday = 2 ... Saturday = 7
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        // Adjust current week to start at Monday (0=Mon, 1=Tue, ..., 6=Sun)
        val currentDayIndex = when (dayOfWeek) {
            Calendar.MONDAY -> 0
            Calendar.TUESDAY -> 1
            Calendar.WEDNESDAY -> 2
            Calendar.THURSDAY -> 3
            Calendar.FRIDAY -> 4
            Calendar.SATURDAY -> 5
            Calendar.SUNDAY -> 6
            else -> 0
        }
        
        // Find Monday of the current week
        calendar.add(Calendar.DAY_OF_YEAR, -currentDayIndex)
        
        // Go back (weeksCount - 1) weeks
        calendar.add(Calendar.WEEK_OF_YEAR, -(weeksCount - 1))
        
        // Generate list
        val startCalendar = calendar.clone() as Calendar
        for (i in 0 until (weeksCount * 7)) {
            dateList.add(sdf.format(startCalendar.time))
            startCalendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        
        return dateList
    }

    /**
     * Checks if a date string is weekday (Monday to Friday) or weekend (Saturday or Sunday)
     */
    fun isWeekend(dateStr: String): Boolean {
        return try {
            val date = sdf.parse(dateStr)
            val cal = Calendar.getInstance()
            if (date != null) {
                cal.time = date
                val d = cal.get(Calendar.DAY_OF_WEEK)
                d == Calendar.SATURDAY || d == Calendar.SUNDAY
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Parse date string and returns nice user formatted string, e.g., "Jun 14, 2026" or "Today" or "Yesterday"
     */
    fun getFormattedDisplayDate(dateStr: String): String {
        return try {
            val date = sdf.parse(dateStr) ?: return dateStr
            val calToday = Calendar.getInstance()
            val calDate = Calendar.getInstance()
            calDate.time = date

            val todayStr = sdf.format(calToday.time)
            calToday.add(Calendar.DAY_OF_YEAR, -1)
            val yesterdayStr = sdf.format(calToday.time)

            if (dateStr == todayStr) {
                "Today"
            } else if (dateStr == yesterdayStr) {
                "Yesterday"
            } else {
                val displayFormat = SimpleDateFormat("MMMM d, yyyy", Locale.US)
                displayFormat.format(date)
            }
        } catch (e: Exception) {
            dateStr
        }
    }
}
