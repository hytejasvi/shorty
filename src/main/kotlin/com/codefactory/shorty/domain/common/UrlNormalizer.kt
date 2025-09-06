package com.codefactory.shorty.domain.common

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import org.apache.commons.validator.routines.UrlValidator

object UrlNormalizer {

    private val urlValidator = UrlValidator(arrayOf("http", "https"))

    fun normalizeAndValidate(url: String): Either<Error, String> =
        Either.catch {
            url.trim()
                .let { if (it.startsWith("http", ignoreCase = true)) it else "https://$it" }
                .also { require(urlValidator.isValid(it)) }
        }.mapLeft { Error.InvalidUrl(url) }

    sealed class Error {
        data class InvalidUrl(val url: String) : Error()
    }
}