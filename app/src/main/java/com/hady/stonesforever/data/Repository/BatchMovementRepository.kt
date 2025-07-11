package com.hady.stonesforever.data.Repository

import com.hady.stonesforever.data.dao.BatchMovementDao
import com.hady.stonesforever.data.entity.BatchMovementEntity
import javax.inject.Inject

class BatchMovementRepository @Inject constructor(
    private val dao: BatchMovementDao
) {
    suspend fun getLocalData(): List<BatchMovementEntity> = dao.getAll()

    suspend fun saveToLocal(data: List<BatchMovementEntity>) {
        dao.clearAll()
        dao.insertAll(data)
    }
}
