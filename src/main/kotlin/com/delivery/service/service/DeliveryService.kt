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
        val (yesterdayStart, yesterdayEnd) = yesterdayRange()
        val deliveries = deliveryRepository.findAllByStartedAtBetween(yesterdayStart, yesterdayEnd)
        val deliveryCount = deliveries.size

        logger.debug { "Found $deliveryCount deliveries that started yesterday" }

        val averageMinutesBetweenDeliveryStart = calculateAverageMinutesBetweenStarts(deliveries)
        logger.debug {
            "Average time between delivery starts: $averageMinutesBetweenDeliveryStart for deliveries: ${deliveries.map { it.id }}"
        }
        return DeliverySummaryResponse(
            deliveries = deliveryCount,
            averageMinutesBetweenDeliveryStart = averageMinutesBetweenDeliveryStart
        )
    }

    private fun yesterdayRange(zoneId: ZoneId = ZoneId.of("Europe/Amsterdam")): Pair<OffsetDateTime, OffsetDateTime> {
        val now = ZonedDateTime.now(zoneId)
        val startOfYesterday = now.minusDays(1).toLocalDate().atStartOfDay(zoneId).toOffsetDateTime()
        val endOfYesterday = startOfYesterday.plusDays(1)
        return startOfYesterday to endOfYesterday
    }

    private fun calculateAverageMinutesBetweenStarts(deliveries: List<Delivery>): Double {
        if (deliveries.size < 2) return 0.0
        val sortedDeliveries = deliveries.sortedBy { it.startedAt }
        val timeDifferences = sortedDeliveries.windowed(2, 1) { (previous, current) ->
            Duration.between(previous.startedAt, current.startedAt).toMinutes().toDouble()
        }
        return timeDifferences.average()
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
