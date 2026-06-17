package com.lighter.browser.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface HistoryDao {
    @Query("SELECT * FROM history ORDER BY visitedAt DESC LIMIT 500")
    fun observeRecent(): LiveData<List<HistoryEntity>>

    @Query("SELECT * FROM history ORDER BY visitedAt DESC LIMIT 500")
    suspend fun getRecent(): List<HistoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: HistoryEntity): Long

    @Query("DELETE FROM history WHERE url = :url")
    suspend fun deleteByUrl(url: String)

    @Query("DELETE FROM history")
    suspend fun clear()

    @Query("DELETE FROM history WHERE visitedAt < :before")
    suspend fun deleteOlderThan(before: Long)

    @Query("SELECT * FROM history WHERE title LIKE :q OR url LIKE :q ORDER BY visitedAt DESC LIMIT 100")
    suspend fun search(q: String): List<HistoryEntity>

    @Query("SELECT DISTINCT url, title FROM history WHERE url LIKE :prefix || '%' ORDER BY visitedAt DESC LIMIT 10")
    suspend fun suggest(prefix: String): List<HistoryEntity>
}
