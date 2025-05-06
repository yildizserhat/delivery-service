package com.delivery.service.service

import com.delivery.service.exception.DeliveryNotFoundException
import com.delivery.service.exception.DeliveryStatusNotValidException
import com.delivery.service.exception.InvalidDeliveryTimeException
import com.delivery.service.model.DeliveryStatus
import com.delivery.service.model.entity.Delivery
import com.delivery.service.repository.DeliveryRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.any
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

@ExtendWith(MockitoExtension::class)
class DeliveryServiceTest {

    @Mock
    lateinit var deliveryRepository: DeliveryRepository

    @InjectMocks
    lateinit var deliveryService: DeliveryService

    @Captor
    lateinit var deliveryCaptor: ArgumentCaptor<Delivery>

    @Test
    fun `test createDelivery with valid data`() {
        // Arrange
        val vehicleId = "vehicle123"
        val address = "123 Main St"
        val startedAt = OffsetDateTime.now()
        val status = "IN_PROGRESS"
        val finishedAt: OffsetDateTime? = null

        val savedDelivery = Delivery(
            id = UUID.randomUUID().toString(),
            vehicleId = vehicleId,
            address = address,
            startedAt = startedAt,
            status = DeliveryStatus.IN_PROGRESS,
            finishedAt = finishedAt
        )

        `when`(deliveryRepository.save(any(Delivery::class.java))).thenReturn(savedDelivery)

        // Act
        val response = deliveryService.createDelivery(
            vehicleId = vehicleId,
            address = address,
            startedAt = startedAt,
            status = status,
            finishedAt = finishedAt
        )

        // Assert
        assertThat(response).isNotNull
        assertThat(response.id).isEqualTo(savedDelivery.id)
        assertThat(response.vehicleId).isEqualTo(vehicleId)
        assertThat(response.address).isEqualTo(address)
        assertThat(response.startedAt).isEqualTo(startedAt)
        assertThat(response.status).isEqualTo(status)

        verify(deliveryRepository).save(deliveryCaptor.capture())
        val capturedDelivery = deliveryCaptor.value
        assertThat(capturedDelivery.vehicleId).isEqualTo(vehicleId)
        assertThat(capturedDelivery.address).isEqualTo(address)
        assertThat(capturedDelivery.startedAt).isEqualTo(startedAt)
        assertThat(capturedDelivery.status).isEqualTo(DeliveryStatus.IN_PROGRESS)
    }

    @Test
    fun `test createDelivery with invalid status`() {
        // Arrange
        val vehicleId = "vehicle123"
        val address = "123 Main St"
        val startedAt = OffsetDateTime.now()
        val status = "INVALID_STATUS"
        val finishedAt: OffsetDateTime? = null

        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            deliveryService.createDelivery(
                vehicleId, address, startedAt, status, finishedAt
            )
        }
        assertThat(exception.message).contains("No enum constant")
        verifyNoInteractions(deliveryRepository)
    }

    @Test
    fun `test createDelivery with finishedAt before startedAt`() {
        // Arrange
        val vehicleId = "vehicle123"
        val address = "123 Main St"
        val startedAt = OffsetDateTime.now()
        val finishedAt = startedAt.minusHours(1)
        val status = "DELIVERED"

        // Act & Assert
        val exception = assertThrows<InvalidDeliveryTimeException> {
            deliveryService.createDelivery(
                vehicleId, address, startedAt, status, finishedAt
            )
        }
        assertThat(exception.message).isEqualTo("Finished Date cannot be before Started Date")
        verifyNoInteractions(deliveryRepository)
    }

    @Test
    fun `test createDelivery with DELIVERED status but no finishedAt`() {
        // Arrange
        val vehicleId = "vehicle123"
        val address = "123 Main St"
        val startedAt = OffsetDateTime.now()
        val status = "DELIVERED"
        val finishedAt: OffsetDateTime? = null

        // Act & Assert
        val exception = assertThrows<InvalidDeliveryTimeException> {
            deliveryService.createDelivery(
                vehicleId, address, startedAt, status, finishedAt
            )
        }
        assertThat(exception.message).isEqualTo("Finished Date must be set when status is DELIVERED")
        verifyNoInteractions(deliveryRepository)
    }

    @Test
    fun `test createDelivery with finishedAt set but status not DELIVERED`() {
        // Arrange
        val vehicleId = "vehicle123"
        val address = "123 Main St"
        val startedAt = OffsetDateTime.now()
        val finishedAt = startedAt.plusHours(2)
        val status = "IN_PROGRESS"

        // Act & Assert
        val exception = assertThrows<DeliveryStatusNotValidException> {
            deliveryService.createDelivery(
                vehicleId, address, startedAt, status, finishedAt
            )
        }
        assertThat(exception.message).isEqualTo("If Finished Date is set, status must be DELIVERED")
        verifyNoInteractions(deliveryRepository)
    }

    @Test
    fun `test findById with existing delivery`() {
        // Arrange
        val deliveryId = UUID.randomUUID().toString()
        val delivery = Delivery(
            id = deliveryId,
            vehicleId = "vehicle123",
            address = "123 Main St",
            startedAt = OffsetDateTime.now(),
            status = DeliveryStatus.IN_PROGRESS
        )

        `when`(deliveryRepository.findById(deliveryId)).thenReturn(Optional.of(delivery))

        // Act
        val response = deliveryService.findById(deliveryId)

        // Assert
        assertThat(response).isNotNull
        assertThat(response.id).isEqualTo(deliveryId)
        assertThat(response.vehicleId).isEqualTo("vehicle123")
        verify(deliveryRepository).findById(deliveryId)
    }

    @Test
    fun `test findById with non-existing delivery`() {
        // Arrange
        val deliveryId = UUID.randomUUID().toString()
        `when`(deliveryRepository.findById(deliveryId)).thenReturn(Optional.empty())

        // Act & Assert
        val exception = assertThrows<DeliveryNotFoundException> {
            deliveryService.findById(deliveryId)
        }
        assertThat(exception.message).isEqualTo("Delivery with id $deliveryId not found")
        verify(deliveryRepository).findById(deliveryId)
    }

    @Test
    fun `test getYesterdayBusinessSummary with less than 2 deliveries`() {
        // Arrange
        val zoneId = ZoneId.of("Europe/Amsterdam")
        val now = ZonedDateTime.now(zoneId)
        val yesterdayStart = now.minusDays(1).toLocalDate().atStartOfDay(zoneId).toOffsetDateTime()
        val yesterdayEnd = yesterdayStart.plusDays(1)

        `when`(
            deliveryRepository.findAllByStartedAtBetween(
                yesterdayStart, yesterdayEnd
            )
        ).thenReturn(emptyList())

        // Act
        val summary = deliveryService.getYesterdayBusinessSummary()

        // Assert
        assertThat(summary.deliveries).isEqualTo(0)
        assertThat(summary.averageMinutesBetweenDeliveryStart).isEqualTo(0.0)
        verify(deliveryRepository).findAllByStartedAtBetween(yesterdayStart, yesterdayEnd)
    }
}