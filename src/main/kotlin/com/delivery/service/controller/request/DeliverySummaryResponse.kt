package com.delivery.service.controller.request

data class DeliverySummaryResponse(
    val deliveries: Int,
    val averageMinutesBetweenDeliveryStart: Double
)
