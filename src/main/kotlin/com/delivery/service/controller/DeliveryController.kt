package com.delivery.service.controller

import com.delivery.service.controller.request.DeliveryInvoiceRequest
import com.delivery.service.controller.request.DeliveryRequest
import com.delivery.service.controller.response.DeliverySummaryResponse
import com.delivery.service.controller.response.DeliveryInvoiceResponse
import com.delivery.service.controller.response.DeliveryResponse
import com.delivery.service.service.DeliveryService
import com.delivery.service.service.InvoiceService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/deliveries")
@Validated
@Tag(name = "Delivery", description = "Delivery API")
class DeliveryController(
    private val deliveryService: DeliveryService,
    private val invoiceService: InvoiceService
) {

    @PostMapping
    @Operation(summary = "Create a delivery")
    fun createDelivery(@Valid @RequestBody request: DeliveryRequest): ResponseEntity<DeliveryResponse> {
        val delivery = deliveryService.createDelivery(
            vehicleId = request.vehicleId,
            address = request.address,
            startedAt = request.startedAt,
            status = request.status,
            finishedAt = request.finishedAt
        )
        return ResponseEntity.status(201).body(delivery)
    }

    @PostMapping("/invoice")
    @Operation(summary = "Send invoices for deliveries")
    fun sendInvoices(
        @Valid @RequestBody request: DeliveryInvoiceRequest
    ): ResponseEntity<List<DeliveryInvoiceResponse>> {
        val response = invoiceService.sendInvoices(request.deliveryIds)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/business-summary")
    fun getBusinessSummary(): ResponseEntity<DeliverySummaryResponse> {
        return ResponseEntity.ok(deliveryService.getYesterdayBusinessSummary())
    }
}
