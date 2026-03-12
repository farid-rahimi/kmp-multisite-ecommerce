package com.solutionium.sharedui.orders

internal fun beautifiedStatus(status: String): String {
    if (status.isBlank()) return ""
    return status
        .replace('_', ' ')
        .split(' ')
        .filter { it.isNotBlank() }
        .joinToString(" ") { token ->
            token.lowercase().replaceFirstChar { ch ->
                if (ch.isLowerCase()) ch.titlecase() else ch.toString()
            }
        }
}

internal fun beautifiedOrderDate(raw: String): String {
    val datePart = raw.substringBefore('T').substringBefore(' ').takeIf { it.length >= 10 } ?: return raw
    val parts = datePart.split("-")
    if (parts.size < 3) return raw

    val year = parts[0]
    val month = parts[1].toIntOrNull()
    val day = parts[2].toIntOrNull()
    if (month == null || day == null) return raw

    val monthName = when (month) {
        1 -> "Jan"
        2 -> "Feb"
        3 -> "Mar"
        4 -> "Apr"
        5 -> "May"
        6 -> "Jun"
        7 -> "Jul"
        8 -> "Aug"
        9 -> "Sep"
        10 -> "Oct"
        11 -> "Nov"
        12 -> "Dec"
        else -> return raw
    }

    return "$day $monthName $year"
}

