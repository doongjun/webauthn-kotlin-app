package com.app.passkey.domain

import com.yubico.webauthn.data.ByteArray
import com.yubico.webauthn.data.UserIdentity
import jakarta.persistence.*

@Entity
@Table(
    name = "`user`",
    indexes = [Index(name = "IDX_USER_USERNAME", columnList = "username", unique = true)]
)
class User(
    userIdentity: UserIdentity
) : PrimaryKeyEntity() {
    @Column(nullable = false, unique = true)
    var username: String = userIdentity.name
        protected set

    @Column(nullable = false)
    var displayName: String = userIdentity.displayName
        protected set

    @Lob
    @Column(nullable = false, length = 64)
    var handle: ByteArray = userIdentity.id
        protected set

    fun toUserIdentity() = UserIdentity.builder()
        .name(username)
        .displayName(displayName)
        .id(handle)
        .build()
}