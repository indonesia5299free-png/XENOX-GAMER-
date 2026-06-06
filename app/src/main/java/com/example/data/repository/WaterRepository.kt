package com.example.data.repository

import com.example.data.local.WaterDao
import com.example.data.local.WaterIntake
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class WaterRepository(private val waterDao: WaterDao) {
    fun getTodayIntakes(): Flow<List<WaterIntake>> {
        val (start, end) = getTodayStartAndEnd()
        return waterDao.getIntakesForDay(start, end)
    }

    fun getTodayTotalIntake(): Flow<Int?> {
        val (start, end) = getTodayStartAndEnd()
        return waterDao.getTotalIntakeForDay(start, end)
    }

    suspend fun addIntake(amountMl: Int) {
        waterDao.insertIntake(WaterIntake(amountMl = amountMl))
    }

    suspend fun removeIntake(id: Int) {
        waterDao.deleteIntake(id)
    }

    private fun getTodayStartAndEnd(): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val start = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val end = calendar.timeInMillis
        return Pair(start, end)
    }
}
