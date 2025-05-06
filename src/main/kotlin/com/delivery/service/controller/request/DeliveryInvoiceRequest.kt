package com.delivery.service.controller.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty

data class DeliveryInvoiceRequest(
    @field:NotEmpty(message = "DeliveryIds must not be empty")
    @field:NotBlank(message = "DeliveryIds must not be empty")
    val deliveryIds: List<@NotEmpty @NotBlank String>
)