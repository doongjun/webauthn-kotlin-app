package com.app.passkey.controller

import ch.qos.logback.core.model.Model
import com.app.passkey.domain.Authenticator
import com.app.passkey.domain.User
import com.app.passkey.repository.AuthenticatorRepository
import com.app.passkey.repository.UserRepository
import com.fasterxml.jackson.core.JsonProcessingException
import com.yubico.webauthn.*
import com.yubico.webauthn.data.PublicKeyCredential
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions
import com.yubico.webauthn.data.UserIdentity
import com.yubico.webauthn.exception.AssertionFailedException
import com.yubico.webauthn.exception.RegistrationFailedException
import jakarta.servlet.http.HttpSession
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.ModelAndView
import java.io.IOException
import java.util.*


@Controller
class AuthController(
    private val relyingParty: RelyingParty,
    private val userRepository: UserRepository,
    private val authenticatorRepository: AuthenticatorRepository
) {
    @GetMapping("/")
    fun welcome(): String {
        return "index"
    }

    @GetMapping("/login")
    fun loginPage(): String {
        return "login"
    }

    @GetMapping("/register")
    fun registerUser(model: Model?): String {
        return "register"
    }

    @ResponseBody
    @PostMapping("/register")
    fun newUserRegistration(
        session: HttpSession,
        @RequestParam username: String,
        @RequestParam display: String
    ): String {
        val existingUser = userRepository.findByUsername(username)
        if (existingUser != null)
            throw ResponseStatusException(HttpStatus.CONFLICT, "Username $username already exists.")

        val bytes = ByteArray(32)
        Random().nextBytes(bytes)
        val id = com.yubico.webauthn.data.ByteArray(bytes)

        val userIdentity = UserIdentity.builder()
            .name(username)
            .displayName(display)
            .id(id)
            .build()

        val user = userRepository.save(User(userIdentity))

        return newAuthRegistration(session, user)
    }

    @ResponseBody
    @PostMapping("/registerauth")
    fun newAuthRegistration(
        session: HttpSession,
        @RequestParam user: User
    ): String {
        userRepository.findByHandle(user.handle)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "User ${user.username} does not exist. Please register.")

        val userIdentity = user.toUserIdentity()
        val registrationOptions = StartRegistrationOptions.builder()
            .user(userIdentity)
            .build()
        val registration = relyingParty.startRegistration(registrationOptions)

        session.setAttribute(userIdentity.name, registration)

        return try {
            registration.toCredentialsCreateJson()
        } catch (e: JsonProcessingException) {
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error processing JSON.", e)
        }
    }

    @ResponseBody
    @PostMapping("/login")
    fun startLogin(
        session: HttpSession,
        @RequestParam username: String
    ): String {
        val request = relyingParty.startAssertion(
            StartAssertionOptions.builder()
                .username(username)
                .build()
        )

        return try {
            session.setAttribute(username, request)
            val result = request.toCredentialsGetJson()
            println("============")
            println(result)
            println("============")
            result
        } catch (e: JsonProcessingException) {
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error processing JSON.", e)
        }
    }

    @ResponseBody
    @PostMapping("/finishauth")
    fun finishRegistration(
        session: HttpSession,
        @RequestParam credential: String,
        @RequestParam username: String,
        @RequestParam credname: String
    ): ModelAndView {
        return try {
            val user = userRepository.findByUsername(username)!!
            val requestOptions = session.getAttribute(username) as PublicKeyCredentialCreationOptions?
                ?: throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Cached request expired. Try to register again!")

            val pkc = PublicKeyCredential.parseRegistrationResponseJson(credential)
            val options = FinishRegistrationOptions.builder()
                .request(requestOptions)
                .response(pkc)
                .build()
            val result = relyingParty.finishRegistration(options)
            val attestationData = pkc.response.attestation.authenticatorData.attestedCredentialData.get()
            authenticatorRepository.save(Authenticator(result, attestationData, user, credname))

            ModelAndView("redirect:/login", HttpStatus.SEE_OTHER)
        } catch (e: RegistrationFailedException) {
            throw ResponseStatusException(HttpStatus.BAD_GATEWAY, "Registration failed.", e)
        } catch (e: IOException) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to save credenital, please try again!", e)
        }
    }

    @PostMapping("/welcome")
    fun finishLogin(
        session: HttpSession,
        model: Model,
        @RequestParam credential: String,
        @RequestParam username: String
    ): String {
        return try {
            val pkc = PublicKeyCredential.parseAssertionResponseJson(credential)
            val request = session.getAttribute(username) as AssertionRequest
            val result = relyingParty.finishAssertion(
                FinishAssertionOptions.builder()
                    .request(request)
                    .response(pkc)
                    .build()
            )

            if (result.isSuccess) {
                "welcome"
            } else {
                "index"
            }
        } catch (e: IOException) {
            throw RuntimeException("Authentication failed", e)
        } catch (e: AssertionFailedException) {
            throw RuntimeException("Authentication failed", e)
        }
    }
}