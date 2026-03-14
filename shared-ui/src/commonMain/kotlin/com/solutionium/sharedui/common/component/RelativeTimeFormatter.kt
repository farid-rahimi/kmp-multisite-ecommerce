package com.solutionium.sharedui.common.component

import com.solutionium.sharedui.resources.Res
import com.solutionium.sharedui.resources.relative_time_hours_ago
import com.solutionium.sharedui.resources.relative_time_just_now
import com.solutionium.sharedui.resources.relative_time_minutes_ago
import com.solutionium.sharedui.resources.relative_time_months_ago
import com.solutionium.sharedui.resources.relative_time_years_ago
import com.solutionium.sharedui.resources.relative_time_days_ago
import androidx.compose.runtime.Composable
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Composable
fun relativeTimeFromIso(isoDateTime: String): String {
    val targetEpoch = parseEpochSecondsOrNull(isoDateTime) ?: return isoDateTime
    val nowEpoch = Clock.System.now().epochSeconds
    val diffSeconds = (nowEpoch - targetEpoch).coerceAtLeast(0L)

    if (diffSeconds < 60L) {
        return stringResource(Res.string.relative_time_just_now)
    }

    val minutes = (diffSeconds / 60L).toInt()
    if (minutes < 60) {
        return formatRelativePlural(pluralStringResource(Res.plurals.relative_time_minutes_ago, minutes, minutes), minutes)
    }

    val hours = (minutes / 60).coerceAtLeast(1)
    if (hours < 24) {
        return formatRelativePlural(pluralStringResource(Res.plurals.relative_time_hours_ago, hours, hours), hours)
    }

    val days = (hours / 24).coerceAtLeast(1)
    if (days < 30) {
        return formatRelativePlural(pluralStringResource(Res.plurals.relative_time_days_ago, days, days), days)
    }

    val months = (days / 30).coerceAtLeast(1)
    if (months < 12) {
        return formatRelativePlural(pluralStringResource(Res.plurals.relative_time_months_ago, months, months), months)
    }

    val years = (days / 365).coerceAtLeast(1)
    return formatRelativePlural(pluralStringResource(Res.plurals.relative_time_years_ago, years, years), years)
}

@OptIn(ExperimentalTime::class)
private fun parseEpochSecondsOrNull(value: String): Long? {
    val raw = value.trim()
    if (raw.isEmpty()) {
        return null
    }

    val parsedInstant = runCatching { Instant.parse(raw).epochSeconds }.getOrNull()
    if (parsedInstant != null) {
        return parsedInstant
    }

    // Supports formats like:
    // 2026-03-14T02:33:45+00:00, 2026-03-14T02:33:45Z, 2026-03-14 02:33:45
    val localCandidate = Regex("""\d{4}-\d{2}-\d{2}[T ]\d{2}:\d{2}:\d{2}""")
        .find(raw)
        ?.value
        ?.replace(' ', 'T')
        ?: return null

    return runCatching {
        LocalDateTime.parse(localCandidate).toInstant(TimeZone.UTC).epochSeconds
    }.getOrNull()
}

private fun formatRelativePlural(template: String, value: Int): String {
    val count = value.toString()
    return template
        .replace("%1\$d", count)
        .replace("%d", count)
}
