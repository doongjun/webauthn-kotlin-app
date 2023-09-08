package com.app.passkey.config

import com.yubico.webauthn.RelyingParty
import com.yubico.webauthn.data.RelyingPartyIdentity
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(WebAuthnProperties::class)
class WebAuthnConfig {
    @Bean
    fun relyingParty(
        registrationRepository: RegistrationRepository,
        properties: WebAuthnProperties
    ): RelyingParty {
        val rpIdentity = RelyingPartyIdentity.builder()
            .id(properties.hostname)
            .name(properties.display)
            .build()

        return RelyingParty.builder()
            .identity(rpIdentity)
            .credentialRepository(registrationRepository)
            .origins(properties.origin)
            .build()
    }
}