package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WaterDao {
    @Query("SELECT * FROM water_intake WHERE timestampMillis >= :startOfDay AND timestampMillis < :endOfDay ORDER BY timestampMillis DESC")
    fun getIntakesForDay(startOfDay: Long, endOfDay: Long): Flow<List<WaterIntake>>

    @Query("SELECT SUM(amountMl) FROM water_intake WHERE timestampMillis >= :startOfDay AND timestampMillis < :endOfDay")
    fun getTotalIntakeForDay(startOfDay: Long, endOfDay: Long): Flow<Int?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIntake(intake: WaterIntake)

    @Query("DELETE FROM water_intake WHERE id = :id")
    suspend fun deleteIntake(id: Int)
}
