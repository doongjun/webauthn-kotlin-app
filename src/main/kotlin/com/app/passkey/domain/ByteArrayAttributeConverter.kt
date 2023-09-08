package com.app.passkey.domain

import com.yubico.webauthn.data.ByteArray
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter(autoApply = true)
class ByteArrayAttributeConverter : AttributeConverter<ByteArray, kotlin.ByteArray> {
    override fun convertToDatabaseColumn(attribute: ByteArray): kotlin.ByteArray {
        return attribute.bytes
    }

    override fun convertToEntityAttribute(dbData: kotlin.ByteArray): ByteArray {
        return ByteArray(dbData)
    }
}