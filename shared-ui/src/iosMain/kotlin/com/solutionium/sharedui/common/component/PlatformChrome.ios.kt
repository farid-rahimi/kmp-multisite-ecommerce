package com.solutionium.sharedui.common.component

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Shapes
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

actual fun platformUsesCupertinoChrome(): Boolean = true

actual fun platformBackIcon(): ImageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft

actual fun platformHomeTabIcon(selected: Boolean): ImageVector =
    if (selected) Icons.Filled.Home else Icons.Outlined.Home

actual fun platformCategoryTabIcon(selected: Boolean): ImageVector =
    if (selected) Icons.Filled.GridView else Icons.Outlined.GridView

actual fun platformCartTabIcon(selected: Boolean): ImageVector =
    if (selected) Icons.Filled.ShoppingCart else Icons.Outlined.ShoppingCart

actual fun platformAccountTabIcon(selected: Boolean): ImageVector =
    if (selected) Icons.Filled.Person else Icons.Outlined.Person

actual fun platformMaterialShapes(): Shapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(26.dp),
)

actual fun platformBottomNavHeight(): Dp = 90.dp

actual fun platformShowTabLabelsAlways(): Boolean = false

actual fun platformPrimaryButtonShape(): Shape = RoundedCornerShape(16.dp)
