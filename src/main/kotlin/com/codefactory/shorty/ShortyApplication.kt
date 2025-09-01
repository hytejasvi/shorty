package com.codefactory.shorty

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ShortyApplication

fun main(args: Array<String>) {
	runApplication<ShortyApplication>(*args)
}
