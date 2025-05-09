package com.delivery.service.service

import com.delivery.service.client.InvoiceClient
import com.delivery.service.controller.request.InvoiceRequest
import com.delivery.service.controller.response.DeliveryInvoiceResponse
import com.delivery.service.controller.response.InvoiceResponse
import com.delivery.service.exception.DeliveryStatusNotValidException
import com.delivery.service.exception.ErrorMessages
import com.delivery.service.model.DeliveryStatus
import com.delivery.service.model.entity.Delivery
import com.delivery.service.model.entity.Invoice
import com.delivery.service.repository.InvoiceRepository
import jakarta.transaction.Transactional
import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
class InvoiceService(
    private val deliveryService: DeliveryService,
    private val invoiceClient: InvoiceClient,
    private val invoiceRepository: InvoiceRepository
) {

    val logger = KotlinLogging.logger {}

    @Transactional
    fun sendInvoices(deliveryIds: List<String>): List<DeliveryInvoiceResponse> {
        val responses = deliveryIds.map { deliveryId ->
            val delivery = deliveryService.findById(deliveryId)
            try {
                validateDeliveryStatus(delivery, deliveryId)
                val invoiceId = sendInvoiceToThirdParty(delivery)
                saveInvoice(invoiceId, deliveryId)
                DeliveryInvoiceResponse(deliveryId = delivery.id, invoiceId = invoiceId)
            } catch (ex: IllegalArgumentException) {
                logger.error { String.format(ErrorMessages.DELIVERY_ID_NOT_VALID, deliveryId) }
                throw IllegalArgumentException(String.format(ErrorMessages.DELIVERY_ID_NOT_VALID, deliveryId))
            }
        }
        return responses
    }

    private fun validateDeliveryStatus(delivery: Delivery, deliveryId: String) {
        if (delivery.status != DeliveryStatus.DELIVERED) {
            logger.error { "${ErrorMessages.DELIVERY_MUST_BE_DELIVERED_FOR_INVOICE} : ${deliveryId}" }
            throw DeliveryStatusNotValidException(ErrorMessages.DELIVERY_MUST_BE_DELIVERED_FOR_INVOICE)
        }
    }

    private fun sendInvoiceToThirdParty(delivery: Delivery): String {
        val invoiceRequest = InvoiceRequest(
            deliveryId = delivery.id.toString(),
            address = delivery.address
        )

        val response: InvoiceResponse = invoiceClient.createInvoice(invoiceRequest)
        logger.debug { "Invoice created with id: ${response.id}, deliveryId: ${delivery.id}" }
        return response.id
    }


    private fun saveInvoice(invoiceId: String, deliveryId: String) {
        invoiceRepository.save(Invoice(deliveryId = deliveryId, invoiceId = invoiceId))
    }
}
