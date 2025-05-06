package com.delivery.service.controller.response

import com.delivery.service.model.entity.Delivery
import java.time.OffsetDateTime

data class DeliveryResponse(
    val id: String?,
    val vehicleId: String,
    val address: String,
    val startedAt: OffsetDateTime?,
    val finishedAt: OffsetDateTime?,
    val status: String
) {
    companion object {
        fun fromDomain(delivery: Delivery) = DeliveryResponse(
            id = delivery.id,
            vehicleId = delivery.vehicleId,
            address = delivery.address,
            startedAt = delivery.startedAt,
            finishedAt = delivery.finishedAt,
            status = delivery.status.name
        )
    }
}
