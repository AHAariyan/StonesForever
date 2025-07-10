package com.hady.stonesforever.data.model

data class BatchMovement(
    val productName: String,
    val meterSquare: Double,
    val width: Int,
    val height: Int,
    val quantity: Int,
    val barcode: String // If barcode is not available yet, we’ll keep it as empty for now
)

