package com.hady.stonesforever.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hady.stonesforever.data.entity.BatchMovementEntity

@Dao
interface BatchMovementDao {
    @Query("SELECT * FROM BatchMovementEntity")
    suspend fun getAll(): List<BatchMovementEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(data: List<BatchMovementEntity>)

    @Query("DELETE FROM BatchMovementEntity")
    suspend fun clearAll()
}
