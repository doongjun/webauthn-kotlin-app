package com.app.passkey.repository

import com.app.passkey.domain.User
import com.yubico.webauthn.data.ByteArray
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserRepository : JpaRepository<User, UUID> {
    fun findByUsername(username: String): User?
    fun findByHandle(handle: ByteArray): User?
}