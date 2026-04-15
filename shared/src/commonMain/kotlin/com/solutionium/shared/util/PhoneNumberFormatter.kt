package com.solutionium.shared.util

data class PhoneParts(
    val countryCode: String,
    val localNumber: String,
)

object PhoneNumberFormatter {
    private const val TOTAL_DIGITS = 12
    private const val DEFAULT_COUNTRY_CODE = "+971"
    private val STRICT_COUNTRY_CODES = setOf("98", "971")

    fun sanitizeCountryCode(
        raw: String,
        fallback: String = DEFAULT_COUNTRY_CODE,
        keepEmpty: Boolean = true,
    ): String {
        val digits = raw.filter(Char::isDigit)
        return when {
            digits.isBlank() && keepEmpty -> "+"
            digits.isBlank() -> fallback
            else -> "+${digits.take(3)}"
        }
    }

    fun expectedLocalDigits(countryCode: String): Int {
        val codeDigits = sanitizeCountryCode(countryCode, keepEmpty = true).filter(Char::isDigit).length
        return (TOTAL_DIGITS - codeDigits).coerceAtLeast(1)
    }

    // Optional local trunk prefix "0" is allowed as user input but not counted in canonical value.
    fun maxLocalInputDigits(countryCode: String): Int = expectedLocalDigits(countryCode) + 1

    fun normalize(countryCode: String, localNumber: String): String {
        val codeDigits = sanitizeCountryCode(countryCode, keepEmpty = true).filter(Char::isDigit)
        val localDigits = localNumber.filter(Char::isDigit).trimStart('0')
        return "+$codeDigits$localDigits"
    }

    fun normalizeFromRaw(raw: String, fallbackCountryCode: String = DEFAULT_COUNTRY_CODE): String {
        val input = raw.trim()
        if (input.startsWith("+")) {
            val digits = input.drop(1).filter(Char::isDigit)
            if (digits.isBlank()) return sanitizeCountryCode(fallbackCountryCode, keepEmpty = false)
            val fallbackDigits = sanitizeCountryCode(fallbackCountryCode, keepEmpty = false).drop(1)
            if (fallbackDigits.isNotBlank() && digits.startsWith(fallbackDigits)) {
                return normalize("+$fallbackDigits", digits.drop(fallbackDigits.length))
            }
            val guessedCode = digits.take(3)
            return normalize("+$guessedCode", digits.drop(guessedCode.length))
        }

        return normalize(fallbackCountryCode, input.filter(Char::isDigit))
    }

    fun isValid(countryCode: String, localNumber: String): Boolean {
        val codeDigits = sanitizeCountryCode(countryCode, keepEmpty = true).filter(Char::isDigit)
        if (codeDigits.isBlank()) return false

        val localDigits = localNumber.filter(Char::isDigit).trimStart('0')
        if (localDigits.isBlank()) return false

        val expected = expectedLocalDigits(countryCode)
        return if (STRICT_COUNTRY_CODES.contains(codeDigits)) {
            localDigits.length == expected
        } else {
            localDigits.length <= expected
        }
    }

    fun isCanonical(phone: String): Boolean {
        val parts = splitForUi(phone)
        return isValid(parts.countryCode, parts.localNumber)
    }

    fun splitForUi(phone: String, fallbackCountryCode: String = DEFAULT_COUNTRY_CODE): PhoneParts {
        val trimmed = phone.trim()
        if (!trimmed.startsWith("+")) {
            return PhoneParts(
                countryCode = sanitizeCountryCode(fallbackCountryCode, keepEmpty = false),
                localNumber = trimmed.filter(Char::isDigit),
            )
        }

        val digits = trimmed.drop(1).filter(Char::isDigit)
        val fallbackDigits = sanitizeCountryCode(fallbackCountryCode, keepEmpty = false).drop(1)
        return if (fallbackDigits.isNotBlank() && digits.startsWith(fallbackDigits)) {
            PhoneParts(
                countryCode = "+$fallbackDigits",
                localNumber = digits.drop(fallbackDigits.length),
            )
        } else {
            val guessedCode = digits.take(3)
            PhoneParts(
                countryCode = if (guessedCode.isBlank()) sanitizeCountryCode(fallbackCountryCode, keepEmpty = false) else "+$guessedCode",
                localNumber = digits.drop(guessedCode.length),
            )
        }
    }
}
