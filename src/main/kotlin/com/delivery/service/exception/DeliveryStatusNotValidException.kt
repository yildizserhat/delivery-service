package com.delivery.service.exception

class DeliveryStatusNotValidException : RuntimeException {
    constructor(message: String) : super(message)
}