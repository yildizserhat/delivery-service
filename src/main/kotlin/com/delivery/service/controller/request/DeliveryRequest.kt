package com.delivery.service.controller.request

import com.delivery.service.exception.ErrorMessages
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import java.time.OffsetDateTime

class DeliveryRequest(
    @field:NotBlank(message = ErrorMessages.VEHICLE_ID_REQUIRED)
    val vehicleId: String,

    @field:NotBlank(message = ErrorMessages.ADDRESS_REQUIRED)
    val address: String,

    @field:NotNull(message = ErrorMessages.STARTED_AT_REQUIRED)
    val startedAt: OffsetDateTime,

    @field:NotNull(message = ErrorMessages.STATUS_REQUIRED)
    @field:Pattern(
        regexp = "IN_PROGRESS|DELIVERED",
        message = ErrorMessages.STATUS_INVALID
    )
    val status: String,

    val finishedAt: OffsetDateTime?
) {
}
