package com.delivery.service.controller


import com.delivery.service.controller.request.DeliveryInvoiceRequest
import com.delivery.service.controller.request.DeliveryRequest
import com.delivery.service.integration.AbstractIT
import com.delivery.service.model.DeliveryStatus
import com.delivery.service.repository.DeliveryRepository
import com.delivery.service.repository.InvoiceRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.OffsetDateTime

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DeliveryControllerIntegrationTest : AbstractIT() {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var deliveryRepository: DeliveryRepository

    @Autowired
    private lateinit var invoiceRepository: InvoiceRepository

    @AfterEach
    fun cleanup() {

        jdbcTemplate.execute("TRUNCATE TABLE ecommerce.invoice CASCADE")
        jdbcTemplate.execute("TRUNCATE TABLE ecommerce.delivery CASCADE")
    }

    @Test
    fun `test createDelivery endpoint`() {
        val request = DeliveryRequest(
            vehicleId = "vehicle123",
            address = "123 Main St",
            startedAt = OffsetDateTime.now(),
            status = DeliveryStatus.IN_PROGRESS.name,
            finishedAt = null
        )

        val result = mockMvc.post("/v1/deliveries") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }
            .andExpect {
                status { isCreated() }
                content {
                    contentType(MediaType.APPLICATION_JSON)
                    jsonPath("$.id") { exists() }
                    jsonPath("$.vehicleId") { value("vehicle123") }
                    jsonPath("$.address") { value("123 Main St") }
                    jsonPath("$.status") { value("IN_PROGRESS") }
                }
            }
            .andReturn()

        // Extract the delivery ID from the response
        val responseContent = result.response.contentAsString
        val deliveryResponse = objectMapper.readValue(responseContent, Map::class.java)
        val deliveryId = deliveryResponse["id"] as String

        // Verify the delivery is in the database
        val deliveryOptional = deliveryRepository.findById(deliveryId)
        assertThat(deliveryOptional).isPresent
        val delivery = deliveryOptional.get()
        assertThat(delivery.vehicleId).isEqualTo("vehicle123")
        assertThat(delivery.address).isEqualTo("123 Main St")
        assertThat(delivery.status).isEqualTo(DeliveryStatus.IN_PROGRESS)
    }

    @Test
    fun `test sendInvoices endpoint`() {
        // First create a delivery with DELIVERED status
        val deliveryId = createTestDelivery()

        val invoiceRequest = DeliveryInvoiceRequest(
            deliveryIds = listOf(deliveryId)
        )

        val result = mockMvc.post("/v1/deliveries/invoice") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(invoiceRequest)
        }
            .andExpect {
                status { isOk() }
                content {
                    contentType(MediaType.APPLICATION_JSON)
                    jsonPath("$[0].deliveryId") { value(deliveryId) }
                    jsonPath("$[0].invoiceId") { exists() }
                }
            }
            .andReturn()

        // Verify that the Invoice is stored in the database
        val invoiceOptional = invoiceRepository.findById(deliveryId)
        assertThat(invoiceOptional).isPresent
        val invoice = invoiceOptional.get()
        assertThat(invoice.deliveryId).isEqualTo(deliveryId)
        assertThat(invoice.invoiceId).isNotBlank
    }

    @Test
    fun `test getBusinessSummary endpoint`() {
        // Create test deliveries for yesterday
        createTestDeliveriesForYesterday()

        val result = mockMvc.get("/v1/deliveries/business-summary")
            .andExpect {
                status { isOk() }
                content {
                    contentType(MediaType.APPLICATION_JSON)
                    jsonPath("$.deliveries") { value(3) }
                    jsonPath("$.averageMinutesBetweenDeliveryStart") { value(240.0) }
                }
            }
            .andReturn()
    }


    private fun createTestDelivery(): String {
        val request = DeliveryRequest(
            vehicleId = "vehicle123",
            address = "123 Main St",
            startedAt = OffsetDateTime.now().minusDays(1),
            status = DeliveryStatus.DELIVERED.name,
            finishedAt = OffsetDateTime.now()
        )

        val result = mockMvc.post("/v1/deliveries") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }
            .andReturn()

        val responseContent = result.response.contentAsString
        val responseMap = objectMapper.readValue(responseContent, Map::class.java)
        return responseMap["id"] as String
    }

    private fun createTestDeliveriesForYesterday() {
        // clean db before inserting.
        jdbcTemplate.execute("TRUNCATE TABLE ecommerce.delivery CASCADE")

        val now = OffsetDateTime.now()
        val yesterday = now.minusDays(1)

        val times = listOf(
            yesterday.withHour(1).withMinute(0).withSecond(0).withNano(0),
            yesterday.withHour(3).withMinute(0).withSecond(0).withNano(0),
            yesterday.withHour(9).withMinute(0).withSecond(0).withNano(0)
        )

        times.forEach { startedAt ->
            val request = DeliveryRequest(
                vehicleId = "vehicle123",
                address = "123 Main St",
                startedAt = startedAt,
                status = DeliveryStatus.DELIVERED.name,
                finishedAt = startedAt.plusHours(1)
            )

            mockMvc.post("/v1/deliveries") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(request)
            }
        }

        // Verify that deliveries are stored in the database
        val deliveries = deliveryRepository.findAll()
        assertThat(deliveries).hasSize(3)
    }
}