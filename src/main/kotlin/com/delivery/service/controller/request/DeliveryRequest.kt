package com.delivery.service.controller.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import java.time.OffsetDateTime

class DeliveryRequest(
    @field:NotBlank(message = "Vehicle ID must not be blank")
    val vehicleId: String,

    @field:NotBlank(message = "Address must not be blank")
    val address: String,

    @field:NotNull(message = "StartedAt must not be null")
    val startedAt: OffsetDateTime,

    @field:NotNull(message = "Status must not be null")
    @field:Pattern(
        regexp = "IN_PROGRESS|DELIVERED",
        message = "Status must be either IN_PROGRESS or DELIVERED"
    )
    val status: String,

    val finishedAt: OffsetDateTime?
) {
}