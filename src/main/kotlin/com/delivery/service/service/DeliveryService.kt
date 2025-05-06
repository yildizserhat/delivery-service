package com.delivery.service.service

import com.delivery.service.controller.response.DeliverySummaryResponse
import com.delivery.service.controller.response.DeliveryResponse
import com.delivery.service.exception.DeliveryNotFoundException
import com.delivery.service.exception.DeliveryStatusNotValidException
import com.delivery.service.exception.ErrorMessages
import com.delivery.service.exception.InvalidDeliveryTimeException
import com.delivery.service.model.DeliveryStatus
import com.delivery.service.model.entity.Delivery
import com.delivery.service.repository.DeliveryRepository
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

@Service
class DeliveryService(
    val deliveryRepository: DeliveryRepository
) {
    val logger = KotlinLogging.logger {}

    fun createDelivery(
        vehicleId: String,
        address: String,
        startedAt: OffsetDateTime,
        status: String,
        finishedAt: OffsetDateTime?
    ): DeliveryResponse {
        val deliveryStatus = DeliveryStatus.valueOf(status)
        validatedDatesAndStatus(startedAt, finishedAt, deliveryStatus)

        val delivery = Delivery(
            vehicleId = vehicleId,
            address = address,
            startedAt = startedAt,
            status = deliveryStatus,
            finishedAt = finishedAt
        )
        val savedDelivery = deliveryRepository.save(delivery)

        logger.debug { "Delivery created with id: ${savedDelivery.id}, vehicleId: ${savedDelivery.vehicleId}, status: ${savedDelivery.status} " }
        return DeliveryResponse.fromDomain(savedDelivery)
    }

    fun getYesterdayBusinessSummary(): DeliverySummaryResponse {
        // Assume it's Amsterdam time zone
        val zoneId = ZoneId.of("Europe/Amsterdam")

        val now = ZonedDateTime.now(zoneId)
        val yesterdayStart = now.minusDays(1).toLocalDate().atStartOfDay(zoneId)
        val yesterdayEnd = yesterdayStart.plusDays(1)

        // Fetch deliveries that started yesterday
        val deliveries = deliveryRepository.findAllByStartedAtBetween(
            yesterdayStart.toOffsetDateTime(),
            yesterdayEnd.toOffsetDateTime()
        )

        val deliveryCount = deliveries.size

        logger.debug { "Found $deliveryCount deliveries that started yesterday" }
        // If there are fewer than 2 deliveries, average time is 0
        if (deliveryCount < 2) {
            return DeliverySummaryResponse(
                deliveries = deliveryCount,
                averageMinutesBetweenDeliveryStart = 0.0
            )
        }
        // Sort deliveries by start time
        val sortedDeliveries = deliveries.sortedBy { it.startedAt }

        // Calculate time differences between consecutive deliveries
        val timeDifferences = sortedDeliveries.windowed(2, 1) { pair ->
            val previousStart = pair[0].startedAt
            val currentStart = pair[1].startedAt
            Duration.between(previousStart, currentStart).toMinutes().toDouble()
        }

        // Calculate average time difference
        val averageMinutesBetweenDeliveryStart = timeDifferences.average()
        logger.debug {
            "Average time between delivery starts: $averageMinutesBetweenDeliveryStart for deliveries: ${deliveries.map { it.id }}"
        }
        return DeliverySummaryResponse(
            deliveries = deliveryCount,
            averageMinutesBetweenDeliveryStart = averageMinutesBetweenDeliveryStart
        )
    }

    private fun validatedDatesAndStatus(
        startedAt: OffsetDateTime,
        finishedAt: OffsetDateTime?,
        status: DeliveryStatus
    ) {
        if (finishedAt != null) {
            if (finishedAt.isBefore(startedAt)) {
                throw InvalidDeliveryTimeException(ErrorMessages.FINISHED_DATE_BEFORE_STARTED_DATE)
            }
            if (status != DeliveryStatus.DELIVERED) {
                throw DeliveryStatusNotValidException(ErrorMessages.STATUS_MUST_BE_DELIVERED_WITH_FINISHED_DATE)
            }
        } else {
            if (status == DeliveryStatus.DELIVERED) {
                throw InvalidDeliveryTimeException(ErrorMessages.FINISHED_DATE_REQUIRED_FOR_DELIVERED_STATUS)
            }
        }
    }


    fun findById(deliveryId: String): Delivery {
        return deliveryRepository.findById(deliveryId)
            .orElseThrow { throw DeliveryNotFoundException(String.format(ErrorMessages.DELIVERY_NOT_FOUND, deliveryId)) }
    }
}
