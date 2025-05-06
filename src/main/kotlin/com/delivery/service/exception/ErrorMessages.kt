package com.delivery.service.exception

/**
 * Centralized error messages for the application.
 * This class contains all error messages used in the application to ensure consistency.
 */
object ErrorMessages {
    // Delivery validation errors
    const val DELIVERY_NOT_FOUND = "Delivery with id %s not found"
    const val FINISHED_DATE_BEFORE_STARTED_DATE = "Finished Date cannot be before Started Date"
    const val STATUS_MUST_BE_DELIVERED_WITH_FINISHED_DATE = "If Finished Date is set, status must be DELIVERED"
    const val FINISHED_DATE_REQUIRED_FOR_DELIVERED_STATUS = "Finished Date must be set when status is DELIVERED"
    
    // Invoice validation errors
    const val DELIVERY_MUST_BE_DELIVERED_FOR_INVOICE = "Delivery must be in DELIVERED status to send invoice"
    const val DELIVERY_ID_NOT_VALID = "Delivery Id not valid %s"
    
    // Request validation errors
    const val VEHICLE_ID_REQUIRED = "Vehicle ID must not be blank"
    const val ADDRESS_REQUIRED = "Address must not be blank"
    const val STARTED_AT_REQUIRED = "StartedAt must not be null"
    const val STATUS_REQUIRED = "Status must not be null"
    const val STATUS_INVALID = "Status must be either IN_PROGRESS or DELIVERED"
    const val DELIVERY_IDS_REQUIRED = "DeliveryIds must not be empty"
}