package com.example.smssheettracker.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: TransactionEntity): Long

    @Query("SELECT * FROM transactions WHERE syncState = 'PENDING' ORDER BY id ASC LIMIT :limit")
    suspend fun getPending(limit: Int = 30): List<TransactionEntity>

    @Query("UPDATE transactions SET syncState = :state, lastError = :error WHERE id = :id")
    suspend fun updateSyncState(id: Long, state: String, error: String?)

    @Query("SELECT COUNT(*) FROM transactions")
    suspend fun totalCount(): Int
}
