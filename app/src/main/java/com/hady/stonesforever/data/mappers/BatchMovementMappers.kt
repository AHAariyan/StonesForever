package com.hady.stonesforever.data.mappers

import com.hady.stonesforever.data.entity.BatchMovementEntity
import com.hady.stonesforever.data.model.BatchMovement

fun BatchMovementEntity.toModel(): BatchMovement = BatchMovement(
    productName = productName,
    barcode = barcode,
    height = height,
    width = width,
    quantity = quantity,
    meterSquare = meterSquare
)

fun BatchMovement.toEntity(): BatchMovementEntity = BatchMovementEntity(
    productName = productName,
    barcode = barcode,
    height = height,
    width = width,
    quantity = quantity,
    meterSquare = meterSquare
)
