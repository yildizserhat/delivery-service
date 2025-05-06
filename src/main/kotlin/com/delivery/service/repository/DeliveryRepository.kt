package com.delivery.service.repository

import com.delivery.service.model.entity.Delivery
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime

@Repository
interface DeliveryRepository : JpaRepository<Delivery, String> {
    fun findAllByStartedAtBetween(start: OffsetDateTime, end: OffsetDateTime): List<Delivery>
}