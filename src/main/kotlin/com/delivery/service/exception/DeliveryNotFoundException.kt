package com.delivery.service.exception

class DeliveryNotFoundException : RuntimeException {
    constructor(message: String) : super(message)
}