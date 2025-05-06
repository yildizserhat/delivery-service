package com.delivery.service.model.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.*


@Entity
@Table(name = "invoice", schema = "ecommerce")
class Invoice() {

    @Id
    @Column(name = "delivery_id")
    var deliveryId: String? = UUID.randomUUID().toString()

    @Column(name = "invoice_id", nullable = false)
    lateinit var invoiceId: String

    constructor(
        deliveryId: String? = UUID.randomUUID().toString(),
        invoiceId: String
    ) : this() {
        this.deliveryId = deliveryId
        this.invoiceId = invoiceId
    }
}