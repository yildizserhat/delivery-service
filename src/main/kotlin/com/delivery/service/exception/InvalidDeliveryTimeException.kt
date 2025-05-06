package com.delivery.service.exception

class InvalidDeliveryTimeException : RuntimeException {
    constructor(message: String) : super(message)
}