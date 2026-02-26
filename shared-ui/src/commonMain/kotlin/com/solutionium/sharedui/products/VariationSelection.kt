package com.solutionium.sharedui.products

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.solutionium.shared.data.model.ProductAttribute
import com.solutionium.shared.data.model.ProductVariation

@Composable
fun VariationSelectionSection(
    attributes: List<ProductAttribute>,
    variations: List<ProductVariation>,
    selectedOptions: Map<Int, String>,
    isLoading: Boolean,
    onOptionSelected: (attributeId: Int, optionValue: String) -> Unit,
    isOptionAvailable: (attributeId: Int, optionValue: String) -> Boolean,
) {
    if (isLoading) {
        Box(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(
                modifier = Modifier
                    .padding(16.dp)
                    .size(24.dp)
                    .align(Alignment.Center),
                strokeWidth = 2.dp,
            )
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            attributes.sortedByDescending { it.position }.forEach { attribute ->
                VariationAttributeRow(
                    attribute = attribute,
                    variations = variations,
                    selectedOption = selectedOptions[attribute.id],
                    onOptionSelected = { optionValue ->
                        onOptionSelected(attribute.id, optionValue)
                    },
                    isOptionAvailable = { optionValue ->
                        isOptionAvailable(attribute.id, optionValue)
                    },
                )
            }
        }
    }
}

@Composable
private fun VariationAttributeRow(
    attribute: ProductAttribute,
    variations: List<ProductVariation>,
    selectedOption: String?,
    onOptionSelected: (String) -> Unit,
    isOptionAvailable: (String) -> Boolean,
) {
    Column {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = attribute.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            selectedOption?.let {
                Text(
                    text = ": $it",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.padding(start = 4.dp),
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items(attribute.options) { option ->
                val isColorAttribute = attribute.slug.equals("pa_color", ignoreCase = true)

                if (isColorAttribute) {
                    val imageUrl = remember(variations, option) {
                        variations.find { variation ->
                            variation.attributes.any { attr ->
                                attr.id == attribute.id && attr.option == option
                            }
                        }?.image
                    }

                    ColorSwatch(
                        option = option,
                        imageUrl = imageUrl,
                        isSelected = selectedOption == option,
                        isAvailable = isOptionAvailable(option),
                        onClick = { onOptionSelected(option) },
                    )
                } else {
                    OptionChip(
                        text = option,
                        isSelected = selectedOption == option,
                        isAvailable = isOptionAvailable(option),
                        onClick = { onOptionSelected(option) },
                    )
                }
            }
        }
    }
}

@Composable
private fun OptionChip(
    text: String,
    isSelected: Boolean,
    isAvailable: Boolean,
    onClick: () -> Unit,
) {
    val backgroundColor =
        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val contentColor =
        if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    val alpha = if (isAvailable) 1f else 0.4f

    Card(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .alpha(alpha)
            .clickable(enabled = isAvailable, onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(if (isSelected) 4.dp else 1.dp),
    ) {
        Text(
            text = text,
            color = contentColor,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ColorSwatch(
    option: String,
    imageUrl: String?,
    isSelected: Boolean,
    isAvailable: Boolean,
    onClick: () -> Unit,
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
    val alpha = if (isAvailable) 1f else 0.4f

    Surface(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .size(56.dp)
            .alpha(alpha)
            .clickable(enabled = isAvailable, onClick = onClick),
        border = BorderStroke(1.dp, borderColor),
    ) {
        Box {
            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Variation for $option",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .matchParentSize()
                        .padding(4.dp),
                )
            } else {
                val color = remember(option) { parseColor(option) ?: Color.Gray }
                Surface(
                    color = color,
                    modifier = Modifier.matchParentSize(),
                ) {}
            }
        }
    }
}

private fun parseColor(raw: String): Color? {
    val hex = raw.removePrefix("#")
    val value = hex.toLongOrNull(16) ?: return null
    return when (hex.length) {
        6 -> {
            val rgb = value.toInt()
            Color(
                red = ((rgb shr 16) and 0xFF) / 255f,
                green = ((rgb shr 8) and 0xFF) / 255f,
                blue = (rgb and 0xFF) / 255f,
                alpha = 1f,
            )
        }

        8 -> {
            val argb = value.toInt()
            Color(
                red = ((argb shr 16) and 0xFF) / 255f,
                green = ((argb shr 8) and 0xFF) / 255f,
                blue = (argb and 0xFF) / 255f,
                alpha = ((argb shr 24) and 0xFF) / 255f,
            )
        }

        else -> null
    }
}
