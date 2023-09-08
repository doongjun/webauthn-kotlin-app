package com.app.passkey.extension

import com.app.passkey.config.RegistrationRepository
import com.yubico.webauthn.RegisteredCredential
import com.yubico.webauthn.data.ByteArray
import kotlin.jvm.optionals.getOrNull

fun RegistrationRepository.getUserHandleForUsernameOrNull(username: String): ByteArray? =
    getUserHandleForUsername(username).getOrNull()

fun RegistrationRepository.getUsernameForUserHandleOrNull(userHandle: ByteArray): String? =
    getUsernameForUserHandle(userHandle).getOrNull()

fun RegistrationRepository.lookupOrNull(credentialId: ByteArray, userHandle: ByteArray): RegisteredCredential? =
    lookup(credentialId, userHandle).getOrNull()