package com.example.data.repository

import com.example.data.db.DailyLog
import com.example.data.db.DailyLogDao
import kotlinx.coroutines.flow.Flow

class LogRepository(private val dailyLogDao: DailyLogDao) {
    val allLogs: Flow<List<DailyLog>> = dailyLogDao.getAllLogs()

    suspend fun getLogByDate(date: String): DailyLog? = dailyLogDao.getLogByDate(date)

    suspend fun toggleLog(date: String) {
        val existing = dailyLogDao.getLogByDate(date)
        if (existing != null) {
            dailyLogDao.deleteLog(existing)
        } else {
            dailyLogDao.insertLog(DailyLog(date = date, isLogged = true, intensity = 1))
        }
    }

    suspend fun setLog(date: String, isLogged: Boolean, intensity: Int = 1) {
        if (isLogged) {
            dailyLogDao.insertLog(DailyLog(date = date, isLogged = true, intensity = intensity))
        } else {
            dailyLogDao.deleteLogByDate(date)
        }
    }
}
