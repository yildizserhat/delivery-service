package com.delivery.service.util.converter

import com.delivery.service.model.DeliveryStatus
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter
class DeliveryStatusConverter: AttributeConverter<DeliveryStatus,String> {
    override fun convertToDatabaseColumn(attribute: DeliveryStatus?): String {
        return attribute?.name.toString()
    }

    override fun convertToEntityAttribute(dbData: String?): DeliveryStatus? {
        return dbData?.let { DeliveryStatus.valueOf(it) }
    }
}