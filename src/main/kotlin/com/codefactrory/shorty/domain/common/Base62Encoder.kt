package com.codefactrory.shorty.domain.common

object Base62Encoder {
    private const val BASE62 = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"

    fun encode(number: Long, minLength: Int = 6): String {
        var n = number
        val sb = StringBuilder()
        do {
            sb.append(BASE62[(n % 62).toInt()])
            n /= 62
        } while (n > 0)

        while (sb.length < minLength) {
            sb.append('0')
        }

        return sb.reverse().toString()
    }
}