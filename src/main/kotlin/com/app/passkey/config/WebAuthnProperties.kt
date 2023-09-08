package com.app.passkey.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "authn")
data class WebAuthnProperties(
    val hostname: String,
    val display: String,
    val origin: Set<String>
)