package com.example.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.db.DailyLog
import com.example.data.repository.LogRepository
import com.example.ui.utils.DateUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class DisciplyStats(
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val totalLogsCount: Int = 0,
    val volumeScore: Float = 0f,    // Overall Density (past 30 days)
    val streakScore: Float = 0f,    // Normalized current streak (relative to 30 days)
    val momentumScore: Float = 0f,  // Past 7 days speed
    val gritScore: Float = 0f,      // Weekend consistency rate
    val focusScore: Float = 0f,     // Weekday consistency rate
    val weeklyHistory: List<WeeklyLogsCount> = emptyList() // Past 8 weeks database logs
)

data class WeeklyLogsCount(
    val label: String, // e.g., "Wk -1"
    val count: Int     // 0 to 7
)

class LogViewModel(private val repository: LogRepository) : ViewModel() {

    val allLogs: StateFlow<List<DailyLog>> = repository.allLogs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _stats = MutableStateFlow(DisciplyStats())
    val stats: StateFlow<DisciplyStats> = _stats.asStateFlow()

    init {
        // Automatically re-compute stats when allLogs change
        allLogs.onEach { logs ->
            calculateStats(logs)
        }.launchIn(viewModelScope)
    }

    fun toggleLog(date: String) {
        viewModelScope.launch {
            repository.toggleLog(date)
        }
    }

    private fun calculateStats(logs: List<DailyLog>) {
        if (logs.isEmpty()) {
            _stats.value = DisciplyStats()
            return
        }

        val loggedDatesSet = logs.filter { it.isLogged }.map { it.date }.toSet()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        // 1. Calculate Streaks
        val currentStreak = computeCurrentStreak(loggedDatesSet, sdf)
        val longestStreak = computeLongestStreak(loggedDatesSet, sdf)

        // 2. Metrics windows
        val calendar = Calendar.getInstance()
        val today = calendar.time

        // Volume score: past 30 days log density
        var volumeCount = 0
        val calVol = Calendar.getInstance()
        for (i in 0 until 30) {
            val dateStr = sdf.format(calVol.time)
            if (loggedDatesSet.contains(dateStr)) {
                volumeCount++
            }
            calVol.add(Calendar.DAY_OF_YEAR, -1)
        }
        val volumeScore = (volumeCount / 30f) * 100f

        // Streak score: currentStreak relative to 30 days
        val streakScore = (currentStreak / 30f * 100f).coerceAtMost(100f)

        // Momentum score: logs in past 7 days
        var momentumCount = 0
        val calMom = Calendar.getInstance()
        for (i in 0 until 7) {
            val dateStr = sdf.format(calMom.time)
            if (loggedDatesSet.contains(dateStr)) {
                momentumCount++
            }
            calMom.add(Calendar.DAY_OF_YEAR, -1)
        }
        val momentumScore = (momentumCount / 7f) * 100f

        // Weekend consistency: gritScore (past 60 days)
        var totalWeekends = 0
        var loggedWeekends = 0
        // Weekday consistency: focusScore (past 60 days)
        var totalWeekdays = 0
        var loggedWeekdays = 0

        val cal60 = Calendar.getInstance()
        for (i in 0 until 60) {
            val dateStr = sdf.format(cal60.time)
            val dOfWeek = cal60.get(Calendar.DAY_OF_WEEK)
            val isWeekend = (dOfWeek == Calendar.SATURDAY || dOfWeek == Calendar.SUNDAY)

            if (isWeekend) {
                totalWeekends++
                if (loggedDatesSet.contains(dateStr)) loggedWeekends++
            } else {
                totalWeekdays++
                if (loggedDatesSet.contains(dateStr)) loggedWeekdays++
            }
            cal60.add(Calendar.DAY_OF_YEAR, -1)
        }

        val gritScore = if (totalWeekends > 0) (loggedWeekends.toFloat() / totalWeekends) * 100f else 0f
        val focusScore = if (totalWeekdays > 0) (loggedWeekdays.toFloat() / totalWeekdays) * 100f else 0f

        // 3. Weekly History (Past 8 Weeks)
        val weeklyHistory = mutableListOf<WeeklyLogsCount>()
        val calWeek = Calendar.getInstance()
        
        // Find Monday of the current week
        val dayOfWeek = calWeek.get(Calendar.DAY_OF_WEEK)
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
        calWeek.add(Calendar.DAY_OF_YEAR, -currentDayIndex) // Snap to Monday

        // Compute starting from 7 weeks ago to this week (8 weeks total)
        calWeek.add(Calendar.WEEK_OF_YEAR, -7)

        for (w in 0 until 8) {
            var weekLogsCount = 0
            val weekTemp = calWeek.clone() as Calendar
            for (d in 0 until 7) {
                val dateStr = sdf.format(weekTemp.time)
                if (loggedDatesSet.contains(dateStr)) {
                    weekLogsCount++
                }
                weekTemp.add(Calendar.DAY_OF_YEAR, 1)
            }
            
            val label = when (w) {
                7 -> "This Wk"
                6 -> "Prev Wk"
                else -> "${8 - w}w ago"
            }
            weeklyHistory.add(WeeklyLogsCount(label = label, count = weekLogsCount))
            calWeek.add(Calendar.WEEK_OF_YEAR, 1)
        }

        _stats.value = DisciplyStats(
            currentStreak = currentStreak,
            longestStreak = longestStreak,
            totalLogsCount = loggedDatesSet.size,
            volumeScore = volumeScore,
            streakScore = streakScore,
            momentumScore = momentumScore,
            gritScore = gritScore,
            focusScore = focusScore,
            weeklyHistory = weeklyHistory
        )
    }

    private fun computeCurrentStreak(loggedSet: Set<String>, sdf: SimpleDateFormat): Int {
        val cal = Calendar.getInstance()
        val todayStr = sdf.format(cal.time)
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val yesterdayStr = sdf.format(cal.time)

        // If today is not logged AND yesterday is not logged, the streak is broken (0)
        if (!loggedSet.contains(todayStr) && !loggedSet.contains(yesterdayStr)) {
            return 0
        }

        // Start counting back from the most recently logged day (either today or yesterday)
        val startCal = Calendar.getInstance()
        if (!loggedSet.contains(todayStr)) {
            startCal.add(Calendar.DAY_OF_YEAR, -1) // Start backward from yesterday
        }

        var streak = 0
        while (true) {
            val checkStr = sdf.format(startCal.time)
            if (loggedSet.contains(checkStr)) {
                streak++
                startCal.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                break
            }
        }
        return streak
    }

    private fun computeLongestStreak(loggedSet: Set<String>, sdf: SimpleDateFormat): Int {
        if (loggedSet.isEmpty()) return 0
        
        // Sort dates in ascending order
        val sortedDates = loggedSet.mapNotNull {
            try { sdf.parse(it) } catch (e: Exception) { null }
        }.sorted()

        if (sortedDates.isEmpty()) return 0

        var maxStreak = 0
        var currentStreak = 1
        val calPrev = Calendar.getInstance()
        val calCurr = Calendar.getInstance()

        calPrev.time = sortedDates[0]

        for (i in 1 until sortedDates.size) {
            calCurr.time = sortedDates[i]
            
            // Check if calCurr is exactly 1 day after calPrev
            val diffMs = calCurr.timeInMillis - calPrev.timeInMillis
            val diffDays = diffMs / (1000 * 60 * 60 * 24)

            if (diffDays == 1L) {
                currentStreak++
            } else if (diffDays > 1L) {
                if (currentStreak > maxStreak) {
                    maxStreak = currentStreak
                }
                currentStreak = 1
            }
            calPrev.time = calCurr.time
        }

        if (currentStreak > maxStreak) {
            maxStreak = currentStreak
        }

        return maxStreak
    }

    // Factory helper
    class Factory(private val context: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LogViewModel::class.java)) {
                val db = AppDatabase.getDatabase(context)
                val repository = LogRepository(db.dailyLogDao())
                @Suppress("UNCHECKED_CAST")
                return LogViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
