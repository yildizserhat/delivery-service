package com.delivery.service.exception

import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    val logger = KotlinLogging.logger {}

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(
        ex: MethodArgumentNotValidException
    ): ResponseEntity<Map<String, Any>> {
        val errors: MutableMap<String, String?> = HashMap()
        ex.bindingResult.allErrors.forEach { error ->
            val fieldName = (error as FieldError).field
            val errorMessage = error.getDefaultMessage()
            errors[fieldName] = errorMessage
        }
        val response = mapOf(
            "timestamp" to System.currentTimeMillis(),
            "status" to HttpStatus.BAD_REQUEST.value(),
            "errors" to errors
        )

        logger.error { errors }
        return ResponseEntity(response, HttpStatus.BAD_REQUEST)
    }


    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(
        ex: IllegalArgumentException
    ): ResponseEntity<Any> {
        val response = mapOf(
            "timestamp" to System.currentTimeMillis(),
            "status" to HttpStatus.BAD_REQUEST.value(),
            "error" to "Argument not valid"
        )
        logger.error { ex.message } // could be more specific
        return ResponseEntity(response, HttpStatus.BAD_REQUEST)
    }


    @ExceptionHandler(DeliveryStatusNotValidException::class)
    fun handleStatusNotValidException(
        ex: DeliveryStatusNotValidException
    ): ResponseEntity<Any> {
        val response = mapOf(
            "timestamp" to System.currentTimeMillis(),
            "status" to HttpStatus.BAD_REQUEST.value(),
            "error" to ex.message
        )
        logger.error { ex.message } // could be more specific
        return ResponseEntity(response, HttpStatus.BAD_REQUEST)
    }


    @ExceptionHandler(DeliveryNotFoundException::class)
    fun handleStatusNotValidException(
        ex: DeliveryNotFoundException
    ): ResponseEntity<Any> {
        val response = mapOf(
            "timestamp" to System.currentTimeMillis(),
            "status" to HttpStatus.BAD_REQUEST.value(),
            "error" to ex.message
        )
        logger.error { ex.message } // could be more specific
        return ResponseEntity(response, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(InvalidDeliveryTimeException::class)
    fun handleStatusNotValidException(
        ex: InvalidDeliveryTimeException
    ): ResponseEntity<Any> {
        val response = mapOf(
            "timestamp" to System.currentTimeMillis(),
            "status" to HttpStatus.BAD_REQUEST.value(),
            "error" to ex.message
        )
        logger.error { ex.message } // could be more specific
        return ResponseEntity(response, HttpStatus.BAD_REQUEST)
    }

}