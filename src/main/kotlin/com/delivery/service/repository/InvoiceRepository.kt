package com.delivery.service.repository

import com.delivery.service.model.entity.Invoice
import org.springframework.data.jpa.repository.JpaRepository

interface InvoiceRepository : JpaRepository<Invoice, String> {
}