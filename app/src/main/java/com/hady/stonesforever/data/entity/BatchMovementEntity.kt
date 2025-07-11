package com.hady.stonesforever.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class BatchMovementEntity(
    @PrimaryKey val barcode: String,
    val productName: String,
    val quantity: Int,
    val meterSquare: Double,
    val height: Int,
    val width: Int
)
