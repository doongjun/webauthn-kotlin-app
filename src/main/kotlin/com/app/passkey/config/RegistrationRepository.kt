package com.app.passkey.config

import com.app.passkey.repository.AuthenticatorRepository
import com.app.passkey.repository.UserRepository
import com.yubico.webauthn.CredentialRepository
import com.yubico.webauthn.RegisteredCredential
import com.yubico.webauthn.data.ByteArray
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class RegistrationRepository(
    private val userRepository: UserRepository,
    private val authenticatorRepository: AuthenticatorRepository
) : CredentialRepository {
    override fun getCredentialIdsForUsername(username: String): Set<PublicKeyCredentialDescriptor> {
        val user = userRepository.findByUsername(username)
        val authenticators = user?.let { authenticatorRepository.findAllByUser(it) } ?: emptyList()

        return authenticators.map { authenticator ->
            PublicKeyCredentialDescriptor.builder()
                .id(authenticator.credentialId)
                .build()
        }.toSet()
    }

    override fun getUserHandleForUsername(username: String): Optional<ByteArray> {
        val user = userRepository.findByUsername(username)

        return Optional.ofNullable(user?.handle)
    }

    override fun getUsernameForUserHandle(userHandle: ByteArray): Optional<String> {
        val user = userRepository.findByHandle(userHandle)

        return Optional.ofNullable(user?.username)
    }

    override fun lookup(credentialId: ByteArray, userHandle: ByteArray): Optional<RegisteredCredential> {
        val authenticator = authenticatorRepository.findByCredentialId(credentialId)
        val registeredCredential = authenticator?.let {
            RegisteredCredential.builder()
                .credentialId(it.credentialId)
                .userHandle(it.user.handle)
                .publicKeyCose(it.publicKey)
                .signatureCount(it.count)
                .build()
        }

        return Optional.ofNullable(registeredCredential)
    }

    override fun lookupAll(credentialId: ByteArray): Set<RegisteredCredential> {
        val authenticators = authenticatorRepository.findAllByCredentialId(credentialId)
        return authenticators.map { authenticator ->
            RegisteredCredential.builder()
                .credentialId(authenticator.credentialId)
                .userHandle(authenticator.user.handle)
                .publicKeyCose(authenticator.publicKey)
                .signatureCount(authenticator.count)
                .build()
        }.toSet()
    }
}