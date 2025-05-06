package com.delivery.service.model.entity

import com.delivery.service.model.DeliveryStatus
import com.delivery.service.util.converter.DeliveryStatusConverter
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.*

@Entity
@Table(name = "delivery", schema = "ecommerce")
class Delivery() {

    @Id
    @Column(name = "id")
    var id: String? = UUID.randomUUID().toString()

    @Column(name = "vehicle_id", nullable = false)
    lateinit var vehicleId: String

    @Column(name = "address", nullable = false)
    lateinit var address: String

    @Column(name = "started_at", nullable = false)
    lateinit var startedAt: OffsetDateTime

    @Column(name = "finished_at")
    var finishedAt: OffsetDateTime? = null

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    @Convert(converter = DeliveryStatusConverter::class)
    var status: DeliveryStatus = DeliveryStatus.IN_PROGRESS

    constructor(
        id: String? = UUID.randomUUID().toString(),
        vehicleId: String,
        address: String,
        startedAt: OffsetDateTime,
        finishedAt: OffsetDateTime? = null,
        status: DeliveryStatus = DeliveryStatus.IN_PROGRESS
    ) : this() {
        this.id = id
        this.vehicleId = vehicleId
        this.address = address
        this.startedAt = startedAt
        this.finishedAt = finishedAt
        this.status = status
    }
}
