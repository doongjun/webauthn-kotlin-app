package com.app.passkey.domain

import com.yubico.webauthn.RegistrationResult
import com.yubico.webauthn.data.AttestedCredentialData
import com.yubico.webauthn.data.ByteArray
import jakarta.persistence.*
import jakarta.persistence.FetchType.LAZY

@Entity
@Table(
    name = "authenticator",
    indexes = [Index(name = "IDX_AUTHENTICATOR_USER_ID", columnList = "user_id")]
)
class Authenticator(
    result: RegistrationResult,
    attestationData: AttestedCredentialData,
    user: User,
    name: String?
) : PrimaryKeyEntity() {
    @Lob
    @Column(nullable = false)
    var credentialId: ByteArray = result.keyId.id
        protected set

    @Lob
    @Column(nullable = false)
    var publicKey: ByteArray = result.publicKeyCose
        protected set

    @Column(nullable = false)
    var count: Long = result.signatureCount
        protected set

    @Lob
    @Column(nullable = false)
    var aaguid: ByteArray = attestationData.aaguid
        protected set

    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "user_id")
    val user: User = user

    @Column
    var name: String? = name
        protected set
}