package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.DailyUsage
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyUsageDao {

    @Query("SELECT * FROM daily_usage ORDER BY date DESC")
    fun getAllDailyUsage(): Flow<List<DailyUsage>>

    @Query("SELECT * FROM daily_usage ORDER BY date DESC LIMIT :limit")
    fun getRecentDailyUsage(limit: Int): Flow<List<DailyUsage>>

    @Query("SELECT * FROM daily_usage WHERE date = :date LIMIT 1")
    suspend fun getUsageForDate(date: String): DailyUsage?

    @Query("SELECT * FROM daily_usage WHERE date = :date LIMIT 1")
    fun getUsageForDateFlow(date: String): Flow<DailyUsage?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(usage: DailyUsage)

    @Update
    suspend fun update(usage: DailyUsage)

    @Query("DELETE FROM daily_usage WHERE date = :date")
    suspend fun deleteDate(date: String)

    @Query("DELETE FROM daily_usage")
    suspend fun clearAll()

    @Query("SELECT SUM(wifiBytesDown + wifiBytesUp) FROM daily_usage")
    fun getTotalWifiBytesFlow(): Flow<Long?>

    @Query("SELECT SUM(mobileBytesDown + mobileBytesUp) FROM daily_usage")
    fun getTotalMobileBytesFlow(): Flow<Long?>
}
