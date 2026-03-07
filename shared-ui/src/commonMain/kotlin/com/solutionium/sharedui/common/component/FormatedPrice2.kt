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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times

@Composable
fun PriceView2(
    price: Double,
    onSale: Boolean = false,
    regularPrice: Double? = null,
    magnifier: Double = 1.0
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        FormattedPriceV3(price.toLong(), magnifier = magnifier)

        if (onSale && regularPrice != null) {
            Spacer(modifier = Modifier.width(4.dp))
            FormattedPriceV3(
                regularPrice.toLong(),
                currency = "",
                mainStyle = TextStyle(fontSize = magnifier * 12.sp, fontWeight = FontWeight.Normal, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), textDecoration = TextDecoration.LineThrough)

            )
        }
    }
}

@Composable
fun FormattedPriceV3(
    amount: Long,
    modifier: Modifier = Modifier,
    currency: String = "ت",
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
    //val currency = remember(currencyCode) { Currency.getInstance(currencyCode) }
    //val currencySymbol = remember(currency, locale) { currency.getSymbol(locale) }

    val numberString = amount.toString()

    val groupingSeparator = remember(localeTag) {
        if (localeTag.startsWith("fa", ignoreCase = true)) '٬' else ','
    }

    val annotatedString = buildAnnotatedString {


        if (numberString.length > 3) {
            val mainPartValue = numberString.substring(0, numberString.length - 3).toLongOrNull() ?: 0L
            val smallPartValue = numberString.substring(numberString.length - 3)
            //val formattedMainPart = formatWithGrouping(mainPartValue, groupingSeparator)

            withStyle(style = mainStyle.toSpanStyle()) {
                //append(formattedMainPart)
            }

            withStyle(style = smallDigitsSpanStyle) {
                append(",")
                append(smallPartValue)
            }
        } else {
            if (amount == 0L) { // Special case for exactly 0
                withStyle(style = mainStyle.toSpanStyle()){ append("0") }
            } else {
                // For numbers 1-999, make the entire number small as per "last 3 digits smaller"
                // if it's all within those 3 digits.
                withStyle(style = smallDigitsSpanStyle) {
                    append(numberString) // Use original non-formatted number to avoid issues with separators
                }
            }
        }
        append("\u00A0") // Non-breaking space
        withStyle(style = currencySymbolStyle) {
            append(currency)
        }

    }

    Text(text = annotatedString, modifier = modifier) // The mainStyle is implicitly the default for non-styled parts
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
