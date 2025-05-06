package com.delivery.service.controller.request

import com.delivery.service.exception.ErrorMessages
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty

data class DeliveryInvoiceRequest(
    @field:NotEmpty(message = ErrorMessages.DELIVERY_IDS_REQUIRED)
    val deliveryIds: List<@NotBlank String>
)
