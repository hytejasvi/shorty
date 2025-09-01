package com.codefactory.shorty.domain.common

import org.apache.commons.validator.routines.UrlValidator

object UrlNormalizer {

    private val urlValidator = UrlValidator(arrayOf("http", "https"))

    fun normalizeAndValidate(url: String): String {
        val normalized = url.trim()
        val fullUrl = if (!normalized.startsWith("http://", ignoreCase = true) &&
            !normalized.startsWith("https://", ignoreCase = true)
        ) {
            "https://$normalized"
        } else {
            normalized
        }

        if (!urlValidator.isValid(fullUrl)) {
            throw IllegalArgumentException("Invalid URL format: $fullUrl")
        }

        return fullUrl
    }
}