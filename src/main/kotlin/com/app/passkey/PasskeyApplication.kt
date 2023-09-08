package com.app.passkey

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PasskeyApplication

fun main(args: Array<String>) {
	runApplication<PasskeyApplication>(*args)
}