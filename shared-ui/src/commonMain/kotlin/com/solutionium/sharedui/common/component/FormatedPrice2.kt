package com.solutionium.sharedui.common.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import com.solutionium.sharedui.designsystem.theme.LocalWooBrand
import com.solutionium.sharedui.designsystem.theme.WooBrand
import com.solutionium.sharedui.resources.Res
import com.solutionium.sharedui.resources.dirham_currency_symbol
import kotlin.math.absoluteValue
import kotlin.math.roundToLong
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.Font

private const val UAE_DIRHAM_SYMBOL = "\uE900"

@Composable
fun PriceView2(
    price: Double,
    onSale: Boolean = false,
    regularPrice: Double? = null,
    magnifier: Double = 1.0
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        FormattedPriceV3(price, magnifier = magnifier)

        if (onSale && regularPrice != null) {
            Spacer(modifier = Modifier.width(4.dp))
            FormattedPriceV3(
                regularPrice,
                currency = "",
                mainStyle = TextStyle(fontSize = magnifier * 12.sp, fontWeight = FontWeight.Normal, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), textDecoration = TextDecoration.LineThrough)

            )
        }
    }
}

@Composable
fun FormattedPriceV3(
    amount: Double,
    modifier: Modifier = Modifier,
    currency: String? = null,
    localeTag: String = defaultLocaleTag(),
    magnifier: Double = 1.0,
    mainStyle: TextStyle = TextStyle(fontSize = magnifier*16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface),
    currencySymbolStyle:  SpanStyle = SpanStyle(
        fontSize = mainStyle.fontSize * 0.8f,
        fontWeight = mainStyle.fontWeight,
        color = mainStyle.color
    ),
    smallDigitsSpanStyle: SpanStyle = SpanStyle(
        fontSize = mainStyle.fontSize * 0.70f,
        fontWeight = mainStyle.fontWeight,
        color = mainStyle.color,
        textDecoration = mainStyle.textDecoration
    )
) {
    val brand = LocalWooBrand.current
    @OptIn(ExperimentalResourceApi::class)
    val dirhamSymbolFont = FontFamily(Font(Res.font.dirham_currency_symbol))
    val effectiveCurrency = currency ?: when (brand) {
        WooBrand.SiteA -> "ت"
        WooBrand.SiteB -> UAE_DIRHAM_SYMBOL
    }
    val groupingSeparator = remember(localeTag) { ',' }
    val isNegative = amount < 0
    val absAmount = amount.absoluteValue

    val annotatedString = buildAnnotatedString {
        if (brand == WooBrand.SiteB && effectiveCurrency.isNotBlank()) {
            // As per UAE guideline: symbol before amount with spacing and same height as digits.
            withStyle(style = mainStyle.toSpanStyle().copy(fontFamily = dirhamSymbolFont)) {
                append(effectiveCurrency)
            }
            //append("\u00A0")
        }

        when (brand) {
            WooBrand.SiteA -> {
                val integerAmount = absAmount.toLong()
                val rawDigits = integerAmount.toString()
                val hasMainPart = rawDigits.length > 3
                val mainRaw = if (hasMainPart) rawDigits.dropLast(3) else ""
                val smallRaw = if (hasMainPart) rawDigits.takeLast(3) else rawDigits

                withStyle(style = mainStyle.toSpanStyle()) {
                    if (isNegative) append("-")
                    if (mainRaw.isNotEmpty()) {
                        append(formatWithGrouping(mainRaw.toLong(), groupingSeparator))
                        append(groupingSeparator)
                    }
                }
                withStyle(style = smallDigitsSpanStyle) {
                    append(smallRaw)
                }
            }
            WooBrand.SiteB -> {
                val scaled = (absAmount * 100.0).roundToLong()
                val integerPart = scaled / 100
                val decimalPart = (scaled % 100).toString().padStart(2, '0')
                withStyle(style = smallDigitsSpanStyle) {
                    if (isNegative) append("-")
                }
                withStyle(style = mainStyle.toSpanStyle()) {
                    append(formatWithGrouping(integerPart, groupingSeparator))
                }
                withStyle(style = smallDigitsSpanStyle) {
                    append(".")
                    append(decimalPart)
                }
            }
        }

        if (brand != WooBrand.SiteB && effectiveCurrency.isNotBlank()) {
            append("\u00A0")
            withStyle(style = currencySymbolStyle) {
                append(effectiveCurrency)
            }
        }
    }

    Text(text = annotatedString, modifier = modifier) // The mainStyle is implicitly the default for non-styled parts
}

@Composable
fun FormattedPriceV3(
    amount: Long,
    modifier: Modifier = Modifier,
    currency: String? = null,
    localeTag: String = defaultLocaleTag(),
    magnifier: Double = 1.0,
    mainStyle: TextStyle = TextStyle(fontSize = magnifier * 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface),
    currencySymbolStyle: SpanStyle = SpanStyle(
        fontSize = mainStyle.fontSize * 0.8f,
        fontWeight = mainStyle.fontWeight,
        color = mainStyle.color
    ),
    smallDigitsSpanStyle: SpanStyle = SpanStyle(
        fontSize = mainStyle.fontSize * 0.70f,
        fontWeight = mainStyle.fontWeight,
        color = mainStyle.color,
        textDecoration = mainStyle.textDecoration
    )
) {
    FormattedPriceV3(
        amount = amount.toDouble(),
        modifier = modifier,
        currency = currency,
        localeTag = localeTag,
        magnifier = magnifier,
        mainStyle = mainStyle,
        currencySymbolStyle = currencySymbolStyle,
        smallDigitsSpanStyle = smallDigitsSpanStyle
    )
}

private fun formatWithGrouping(value: Long, separator: Char): String {
    val raw = value.toString()
    if (raw.length <= 3) return raw
    return buildString(raw.length + raw.length / 3) {
        raw.forEachIndexed { index, c ->
            append(c)
            val remaining = raw.length - index - 1
            if (remaining > 0 && remaining % 3 == 0) append(separator)
        }
    }
}
