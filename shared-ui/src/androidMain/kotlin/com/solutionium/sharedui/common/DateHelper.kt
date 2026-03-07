package com.solutionium.sharedui.common

import android.content.Context
import com.solutionium.core.ui.common.R
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.text.iterator

/**
 * A helper object for parsing and formatting dates, with a focus on converting
 * server time (assumed UTC) to local time and formatting for a Persian audience.
 */
object DateHelper {

    fun getRelativeTimeSpanString(context: Context, dateTime: String): String {

        val localDateTime = parseToLocalDateTime(dateTime) ?: return convertDateStringToJalali(dateTime)

        val now = OffsetDateTime.now()
        val daysBetween = ChronoUnit.DAYS.between(localDateTime, now)

        return when {
            daysBetween == 0L -> context.getString(R.string.relative_time_today)
            daysBetween == 1L -> context.getString(R.string.relative_time_yesterday)
            daysBetween < 7 -> {
                val days = daysBetween.toInt()
                context.resources.getQuantityString(R.plurals.relative_time_days_ago, days, days)
            }
            daysBetween < 30 -> {
                val weeks = (daysBetween / 7).toInt()
                context.resources.getQuantityString(R.plurals.relative_time_weeks_ago, weeks, weeks)
            }
            daysBetween < 365 -> {
                val months = (daysBetween / 30).toInt()
                context.resources.getQuantityString(R.plurals.relative_time_months_ago, months, months)
            }
            else -> {
                val years = (daysBetween / 365).toInt()
                context.resources.getQuantityString(R.plurals.relative_time_years_ago, years, years)
            }
        }
    }

    // ---------- Parsers ----------
    private val FORMATTERS = listOf(
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.US),
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss", Locale.US),
        DateTimeFormatter.ofPattern("yyyy-MM-dd['T'][' ']HH:mm:ss", Locale.US) // some JVMs accept optional pattern
    )

    private fun parseToLocalDateTime(input: String): LocalDateTime? {
        val trimmed = input.trim()
        // try common formats
        for (fmt in FORMATTERS) {
            try {
                return LocalDateTime.parse(trimmed, fmt)
            } catch (_: Exception) { /* try next */ }
        }
        // fallback: try brute force replacing 'T' with space
        return try {
            LocalDateTime.parse(trimmed.replace('T', ' '), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.US))
        } catch (_: Exception) {
            null
        }
    }

// ---------- Gregorian -> Jalali (Persian) conversion ----------
    /**
     * Returns Triple(jy, jm, jd)
     * Algorithm based on common Persian calendar conversion (public domain algorithm).
     */
    private fun gregorianToJalali(gy: Int, gm: Int, gd: Int): Triple<Int, Int, Int> {
        val gDaysInMonth = intArrayOf(0,31,59,90,120,151,181,212,243,273,304,334) // cumulative days before month (0-based)
        var gy2 = gy - 1600
        val gm2 = gm - 1
        val gd2 = gd - 1

        var gDayNo = 365 * gy2 + (gy2 + 3) / 4 - (gy2 + 99) / 100 + (gy2 + 399) / 400
        gDayNo += gDaysInMonth[gm2] + gd2

        // leap day for Gregorian
        if (gm2 > 1) {
            val isLeap = (gy % 4 == 0 && gy % 100 != 0) || (gy % 400 == 0)
            if (isLeap) gDayNo += 1
        }

        var jDayNo = gDayNo - 79
        val jNp = jDayNo / 12053
        jDayNo %= 12053

        var jy = 979 + 33 * jNp + 4 * (jDayNo / 1461)
        jDayNo %= 1461

        if (jDayNo >= 366) {
            jy += (jDayNo - 1) / 365
            jDayNo = (jDayNo - 1) % 365
        }

        val jmDays = intArrayOf(31,31,31,31,31,31,30,30,30,30,30,29)
        var jm = 0
        var jdOut = 0
        var i = 0
        while (i < 12 && jDayNo >= jmDays[i]) {
            jDayNo -= jmDays[i]
            i++
        }
        jm = i + 1
        jdOut = jDayNo + 1

        return Triple(jy, jm, jdOut)
    }

    // ---------- Helpers for formatting ----------
    private fun two(n: Int) = if (n < 10) "0$n" else n.toString()

    /**
     * Convert a LocalDateTime (Gregorian) to Jalali date string.
     * - dateOnly = true -> returns "yyyy/MM/dd"
     * - dateOnly = false -> returns "yyyy/MM/dd HH:mm:ss"
     * digitsInPersian = true -> convert numerals to Persian digits (optional)
     */
    private fun localDateTimeToJalaliString(ldt: LocalDateTime, dateOnly: Boolean = false, digitsInPersian: Boolean = false): String {
        val gYear = ldt.year
        val gMonth = ldt.monthValue
        val gDay = ldt.dayOfMonth

        val (jy, jm, jd) = gregorianToJalali(gYear, gMonth, gDay)

        val datePart = "${jy}/${two(jm)}/${two(jd)}"
        if (dateOnly) {
            return if (digitsInPersian) toPersianDigits(datePart) else datePart
        }

        val timePart = "${two(ldt.hour)}:${two(ldt.minute)}" //:${two(ldt.second)}
        val out = "$datePart | $timePart"
        return if (digitsInPersian) toPersianDigits(out) else out
    }

    // optional: convert 0-9 to Persian digits
    private val englishToPersianDigits = mapOf(
        '0' to '۰', '1' to '۱', '2' to '۲', '3' to '۳', '4' to '۴',
        '5' to '۵', '6' to '۶', '7' to '۷', '8' to '۸', '9' to '۹'
    )
    private fun toPersianDigits(input: String): String {
        val sb = StringBuilder()
        for (ch in input) {
            sb.append(englishToPersianDigits[ch] ?: ch)
        }
        return sb.toString()
    }

// ---------- Convenience wrapper: from input string ----------
    /**
     * Main utility. Returns null if input cannot be parsed.
     * - input: "2025-10-06 01:44:14" or "2025-10-06T01:44:14" (or similar)
     * - dateOnly: whether to return only date
     * - persianDigits: whether digits should be Persian
     */
    fun convertDateStringToJalali(input: String, dateOnly: Boolean = false, persianDigits: Boolean = false): String {
        val ldt = parseToLocalDateTime(input) ?: return input
        return localDateTimeToJalaliString(ldt, dateOnly = dateOnly, digitsInPersian = persianDigits)
    }
}