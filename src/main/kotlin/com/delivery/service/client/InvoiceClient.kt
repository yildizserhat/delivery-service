package com.delivery.service.client

import com.delivery.service.controller.request.InvoiceRequest
import com.delivery.service.controller.response.InvoiceResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody


@FeignClient(name = "invoice-client", url = "\${invoice-service.url}")
interface InvoiceClient {

    @PostMapping(
        "/v1/invoices",
        produces = arrayOf(MediaType.APPLICATION_JSON_VALUE),
        consumes = arrayOf(MediaType.APPLICATION_JSON_VALUE)
    )
    fun createInvoice(@RequestBody request: InvoiceRequest): InvoiceResponse
}