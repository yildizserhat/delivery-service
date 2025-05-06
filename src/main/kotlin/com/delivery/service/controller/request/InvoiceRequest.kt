package com.delivery.service.controller.request

data class InvoiceRequest(
    val deliveryId: String,
    val address: String
) {
}
