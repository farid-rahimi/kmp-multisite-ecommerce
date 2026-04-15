package com.solutionium.sharedui.common.component

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Shapes
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

actual fun platformUsesCupertinoChrome(): Boolean = false

actual fun platformBackIcon(): ImageVector = Icons.AutoMirrored.Filled.ArrowBack

actual fun platformShareIcon(): ImageVector = Icons.Filled.Share

actual fun platformHomeTabIcon(selected: Boolean): ImageVector = Icons.Filled.Home

actual fun platformCategoryTabIcon(selected: Boolean): ImageVector = Icons.Filled.Category

actual fun platformCartTabIcon(selected: Boolean): ImageVector = Icons.Filled.ShoppingCart

actual fun platformAccountTabIcon(selected: Boolean): ImageVector = Icons.Filled.AccountCircle

actual fun platformMaterialShapes(): Shapes = Shapes()

actual fun platformBottomNavHeight(): Dp = 88.dp

actual fun platformShowTabLabelsAlways(): Boolean = true

actual fun platformPrimaryButtonShape(): Shape = RoundedCornerShape(20.dp)
