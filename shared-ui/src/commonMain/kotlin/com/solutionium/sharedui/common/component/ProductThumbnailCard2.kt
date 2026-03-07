package com.solutionium.sharedui.common.component


import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.outlined.BrightnessAuto
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.Loyalty
import androidx.compose.material.icons.outlined.RocketLaunch
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.solutionium.shared.data.model.FeatureType
import com.solutionium.shared.data.model.ProductCatType
import com.solutionium.shared.data.model.ProductThumbnail
import com.solutionium.sharedui.resources.Res
import com.solutionium.sharedui.resources.feature_authentic
import com.solutionium.sharedui.resources.feature_fast_selling
import com.solutionium.sharedui.resources.feature_free_shipping
import com.solutionium.sharedui.resources.feature_high_quality
import com.solutionium.sharedui.resources.feature_size_exchange
import com.solutionium.sharedui.resources.feature_team_choice
import com.solutionium.sharedui.resources.full_pay
import com.solutionium.sharedui.resources.in_stock
import com.solutionium.sharedui.resources.in_stock_count
import com.solutionium.sharedui.resources.installment_pay
import com.solutionium.sharedui.resources.out_of_stock
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource


@Composable
fun ProductThumbnailCard2(
    product: ProductThumbnail,
    onProductClick: (productId: Int) -> Unit,
    modifier: Modifier = Modifier,
    discountedPrice: (Double?) -> Double? = { null },
    isFavorite: Boolean = false,
    onFavoriteClick: (productId: Int, isFavorite: Boolean) -> Unit,
    inCartQuantity: Int = 0,
    maxQuantity: Int = 12,
    onAddToCartClick: (productId: Int) -> Unit,
    onRemoveFromCartClick: (productId: Int) -> Unit = {},
    priceMagnifier: Double = 1.0,
    showStock: Boolean = false

) {
    Card(
        onClick = { onProductClick(product.id) },
        modifier = modifier
            .fillMaxWidth(), // Will be constrained by LazyVerticalGrid column
        //.aspectRatio(0.5f), // Adjust aspect ratio for desired card height vs width
        shape = RoundedCornerShape(8.dp),
        //elevation = CardDefaults.cardElevation(defaultElevation = 2.dp, hoveredElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    //.weight(1f)
                    .aspectRatio(1f) // Square image
                    .fillMaxWidth()
                    //.padding(12.dp)
                    .background(Color.White)

            ) { // Image container

                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                )
                FavoriteIconButton2(
                    isFavorite = isFavorite,
                    onClick = { onFavoriteClick(product.id, isFavorite) },
                    modifier = Modifier
                        .padding(8.dp)
                        .size(32.dp)
                        .align(Alignment.TopEnd)
                        .background(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(6.dp)
                )

                if (product.isOnSale())
                    SalesBadge2(
                        discount = product.salesPercentage(),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                    )
                // Add to Cart (optional based on product)
                if (product.hasSimpleAddToCart) {

                    // Wrap the conditional UI in AnimatedContent
                    AnimatedContent(
                        targetState = inCartQuantity > 0,
                        label = "CartButtonAnimation",
                        modifier = Modifier
                            .padding(8.dp)
                            .height(32.dp)
                            .align(Alignment.BottomEnd)
                            .background(
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                shape = RoundedCornerShape(4.dp)
                            ),
                        // Define the transition animation
                        transitionSpec = {
                            // Animate in: Slide in from the right and fade in
                            (slideInHorizontally { fullWidth -> fullWidth } + fadeIn())
                                .togetherWith(
                                    // Animate out: Slide out to the right and fade out
                                    slideOutHorizontally { fullWidth -> fullWidth } + fadeOut()
                                )
                        }
                    ) { isInCart ->
                        if (isInCart) {
                            CartIQuantityButtons(
                                quantity = inCartQuantity,
                                maxQuantity = maxQuantity,
                                onIncreaseQuantity = { onAddToCartClick(product.id) },
                                onDecreaseQuantity = { onRemoveFromCartClick(product.id) },
                                modifier = Modifier
//                                    .height(32.dp)
//                                    .align(Alignment.BottomEnd)
//                                    .background(
//                                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
//                                        shape = RoundedCornerShape(4.dp)
//                                    )
                                    //.fillMaxSize()
                                    .padding(6.dp)
                            )
                        } else
                            IconButton(
                                onClick = { onAddToCartClick(product.id) },
                                modifier = Modifier
//                                    .size(32.dp)
//                                    .align(Alignment.BottomEnd)
//                                    .background(
//                                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
//                                        shape = RoundedCornerShape(4.dp)
//                                    )
                                    //.fillMaxSize()
                                    .width(32.dp)
                                    .padding(6.dp),

                                ) {
                                Icon(
                                    Icons.Filled.AddShoppingCart,
                                    contentDescription = "Add to Cart",
                                    //modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                //Spacer(modifier = Modifier.width(6.dp))
                                //Text("Add to Cart", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
                            }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text( // Product Name
                    text = product.name,
                    //style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold, lineHeight = 16.sp, color = MaterialTheme.colorScheme.onSurface),
                    //MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp, lineHeight = 16.sp),
                    maxLines = 2,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.heightIn(min = 30.dp) // Ensure space for 2 lines
                )

                if (showStock) {
                    val stockText = if (product.manageStock && product.stock > 0) {
                        stringResource(Res.string.in_stock_count, product.stock)
                    } else if (!product.manageStock && product.stockStatus == "instock") {
                        stringResource(Res.string.in_stock)
                    } else {
                        stringResource(Res.string.out_of_stock)
                    }
                    Text(
                        text = stockText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (product.manageStock && product.stock > 0 || (!product.manageStock && product.stockStatus == "instock")) Color(
                            0xFF4CAF50
                        ) else Color(0xFFF44336), // Green or Red
                        //color = if (product.stock > 0) Color(0xFF4CAF50) else Color(0xFFF44336), // Green or Red
                        modifier = Modifier.heightIn(min = 18.dp) // Ensure consistent height
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(Res.string.installment_pay), fontSize = 9.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.weight(1f))
                    Text("x 4 ", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    PriceView2(
                        price = product.price / 4,
                        onSale = product.onSale,
                        regularPrice = product.regularPrice?.let { it / 4 },
                        magnifier = priceMagnifier
                    )
                }
                discountedPrice(product.price)?.let {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = stringResource(Res.string.full_pay), fontSize = 9.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.weight(1f))
                        PriceView2(
                            price = discountedPrice(product.price) ?: product.price,
                            onSale = product.onSale,
                            regularPrice = discountedPrice(product.regularPrice)
                                ?: product.regularPrice,
                            magnifier = priceMagnifier * 0.9
                        )
                    }
                }

                // Type-Specific Attributes
                when (product.type) {
                    ProductCatType.PERFUME -> PerfumeAttributes2(product)
                    ProductCatType.SHOES -> ShoeAttributes2(product)
                    else -> Spacer(modifier = Modifier.height(18.dp)) // Placeholder or empty space for other types
                }


                RotatingFeatureText2(features = product.features().map { it.toUiFeature2() })


                // Rotating Features Text
//                if (product.rotatingFeatures.isNotEmpty()) {
//                    RotatingFeatureText(features = product.rotatingFeatures)
//                } else {
//                    Spacer(modifier = Modifier.height(18.dp)) // Ensure consistent height if no features
//                }
            }

//             else {
//                Spacer(modifier = Modifier.height(8.dp)) // (36dp button + 8dp padding)
//            }
        }
    }
}


@Composable
fun PerfumeAttributes2(
    product: ProductThumbnail,
    onlyVolume: Boolean = false
) {
    Column(
        //verticalArrangement = Alignment.CenterVertically,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        //modifier = Modifier.heightIn(min = 18.dp) // Consistent height
    ) {
        if (!onlyVolume) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                product.scentGroup?.forEach {
                    AttributeChip2(text = it)
                }
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),

            ) {
            Spacer(modifier = Modifier.weight(1f))
            product.volume?.let {
                AttributeChip2(text = it, available = product.isInStock)
            }

            product.decants?.let {
                if (it.isNotEmpty()) {
                    it.forEach { size ->
                        AttributeChip2(text = size, icon = null)
                    }
                }
            }
            //product.decants?
            //AttributeChip(text = product.brand, icon = null)
        }

    }
}

@Composable
fun ShoeAttributes2(product: ProductThumbnail) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.heightIn(min = 18.dp) // Consistent height
    ) {
        product.sizingRange?.let {
            AttributeChip2(text = "Sizes: $it", icon = Icons.Filled.Straighten)
        }
        product.availableColorsHex?.let { colors ->
            if (colors.isNotEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.ColorLens,
                        contentDescription = "Available Colors",
                        modifier = Modifier.size(12.sp.value.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    colors.take(5).forEach { hexColor -> // Show max 5 color dots
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                //.background(hexToColor(hexColor))
                                .padding(end = 4.dp)
                        )
                    }
                    if (colors.size > 5) Text(
                        " +${colors.size - 5}",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}


@Composable
fun AttributeChip2(text: String, icon: ImageVector? = null, available: Boolean = true) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier

            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (available) 0.4f else 0.1f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 4.dp)

    ) {
        icon?.let {
            Icon(
                imageVector = it,
                contentDescription = null, // text describes it
                modifier = Modifier.size(12.sp.value.dp), // scale icon with text
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (available) 0.7f else 0.2f)
            )
            Spacer(modifier = Modifier.width(2.dp))
        }
        Text(
            text = text,
            fontSize = 9.sp,
            //style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (available) 0.8f else 0.3f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun RotatingFeatureText2(features: List<UiFeature2>) {
    if (features.isEmpty()) return

    var currentIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(key1 = features) { // Re-launch if features list changes
        while (true) {
            delay(3000) // Change text every 3 seconds
            currentIndex = (currentIndex + 1) % features.size
        }
    }

    AnimatedContent(
        targetState = features[currentIndex],
        transitionSpec = {
            (slideInVertically { height -> height } + fadeIn(animationSpec = tween(800)))
                .togetherWith(slideOutVertically { height -> -height } + fadeOut(
                    animationSpec = tween(
                        800
                    )
                ))
        },
        label = "RotatingFeatureTextAnimation",
        modifier = Modifier.heightIn(min = 18.dp) // Ensure consistent height
    ) { feature ->
        Row(verticalAlignment = Alignment.CenterVertically) {
            feature.icon?.let { iconRes ->

                Icon(
                    imageVector = iconRes,
                    contentDescription = null,
                    modifier = Modifier.size(12.sp.value.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.width(4.dp))

            }
            Text(
                text = feature.text,
                fontSize = 11.sp,
                //style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.secondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun FavoriteIconButton2(
    isFavorite: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Icon(
            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
            contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
            tint = if (isFavorite) Color.Red.copy(alpha = 0.7f) else Color.Gray
        )
    }
}

@Composable
fun SalesBadge2(
    discount: Int,
    modifier: Modifier = Modifier
) {

    Row (
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.9f),
                shape = RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 6.dp, vertical = 0.dp)
    ) {
        Text(
            text = "$discount",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimary
        )

        Text(
            text = " %",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}


data class UiFeature2(
    val text: String,
    val icon: ImageVector?
)

@Composable
fun FeatureType.toUiFeature2(): UiFeature2 =
    when (this) {
        FeatureType.FREE_SHIPPING ->
            UiFeature2(
                text = stringResource(Res.string.feature_free_shipping),
                icon = Icons.Outlined.LocalShipping
            )

        FeatureType.FAST_SELLING ->
            UiFeature2(
                text = stringResource(Res.string.feature_fast_selling),
                icon = Icons.Outlined.RocketLaunch
            )

        FeatureType.AUTHENTIC ->
            UiFeature2(
                text = stringResource(Res.string.feature_authentic),
                icon = Icons.Outlined.Verified
            )

        FeatureType.SIZE_EXCHANGE ->
            UiFeature2(
                text = stringResource(Res.string.feature_size_exchange),
                icon = Icons.Outlined.Sync
            )

        FeatureType.HIGH_QUALITY ->
            UiFeature2(
                text = stringResource(Res.string.feature_high_quality),
                icon = Icons.Outlined.BrightnessAuto
            )

        FeatureType.TEAM_CHOICE ->
            UiFeature2(
                text = stringResource(Res.string.feature_team_choice),
                icon = Icons.Outlined.Loyalty
            )
    }

fun hexToColor2(hex: String): Color {
    val cleanHex = hex.removePrefix("#")
    return when (cleanHex.length) {
        6 -> Color(
            red = cleanHex.substring(0, 2).toInt(16) / 255f,
            green = cleanHex.substring(2, 4).toInt(16) / 255f,
            blue = cleanHex.substring(4, 6).toInt(16) / 255f,
            alpha = 1f
        )
        8 -> Color(
            red = cleanHex.substring(2, 4).toInt(16) / 255f,
            green = cleanHex.substring(4, 6).toInt(16) / 255f,
            blue = cleanHex.substring(6, 8).toInt(16) / 255f,
            alpha = cleanHex.substring(0, 2).toInt(16) / 255f
        )
        else -> Color.Transparent
    }
}
