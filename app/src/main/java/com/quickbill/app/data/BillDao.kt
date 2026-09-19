package com.quickbill.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BillDao {
    @Insert
    suspend fun insert(bill: Bill): Long

    @Query("SELECT * FROM bills ORDER BY dateMillis DESC")
    fun observeAll(): Flow<List<Bill>>

    @Query("SELECT * FROM bills ORDER BY dateMillis DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<Bill>>

    @Query("SELECT * FROM bills WHERE id = :id")
    suspend fun getById(id: Long): Bill?

    @Query("SELECT COUNT(*) FROM bills")
    suspend fun count(): Int
}
