package com.app.passkey.repository

import com.app.passkey.domain.Authenticator
import com.app.passkey.domain.User
import com.yubico.webauthn.data.ByteArray
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface AuthenticatorRepository : JpaRepository<Authenticator, UUID> {
    fun findAllByUser(user: User): List<Authenticator>
    fun findByCredentialId(credentialId: ByteArray): Authenticator?
    fun findAllByCredentialId(credentialId: ByteArray): List<Authenticator>
}