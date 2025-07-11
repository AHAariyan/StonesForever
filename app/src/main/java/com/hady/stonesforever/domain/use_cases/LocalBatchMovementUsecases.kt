package com.hady.stonesforever.domain.use_cases

import com.hady.stonesforever.data.Repository.BatchMovementRepository
import com.hady.stonesforever.data.entity.BatchMovementEntity
import javax.inject.Inject

class SaveBatchMovementsUseCase @Inject constructor(
    private val repository: BatchMovementRepository
) {
    suspend operator fun invoke(batchMovements: List<BatchMovementEntity>) {
        repository.saveToLocal(batchMovements)
    }
}

class GetLocalBatchMovementsUseCase @Inject constructor(
    private val repository: BatchMovementRepository
) {
    suspend operator fun invoke(): List<BatchMovementEntity> {
        return repository.getLocalData()
    }
}

data class BatchMovementUseCases @Inject constructor(
    val saveBatchMovements: SaveBatchMovementsUseCase,
    val getLocalBatchMovements: GetLocalBatchMovementsUseCase
)

