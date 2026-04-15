package com.solutionium.sharedui.common.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

expect fun platformUsesCupertinoChrome(): Boolean
expect fun platformBackIcon(): ImageVector
expect fun platformShareIcon(): ImageVector
expect fun platformHomeTabIcon(selected: Boolean): ImageVector
expect fun platformCategoryTabIcon(selected: Boolean): ImageVector
expect fun platformCartTabIcon(selected: Boolean): ImageVector
expect fun platformAccountTabIcon(selected: Boolean): ImageVector
expect fun platformMaterialShapes(): androidx.compose.material3.Shapes
expect fun platformBottomNavHeight(): Dp
expect fun platformShowTabLabelsAlways(): Boolean
expect fun platformPrimaryButtonShape(): Shape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlatformTopBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(
        containerColor = if (platformUsesCupertinoChrome()) {
            MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        titleContentColor = MaterialTheme.colorScheme.onSurface,
    ),
    addBottomDivider: Boolean = platformUsesCupertinoChrome(),
) {
    Column(modifier = modifier) {
        if (platformUsesCupertinoChrome()) {
            CenterAlignedTopAppBar(
                title = title,
                colors = colors,
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = platformBackIcon(),
                                contentDescription = "Back",
                            )
                        }
                    }
                },
                actions = actions,
            )
        } else {
            TopAppBar(
                title = title,
                colors = colors,
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = platformBackIcon(),
                                contentDescription = "Back",
                            )
                        }
                    }
                },
                actions = actions,
            )
        }
        if (addBottomDivider) {
            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
            )
        }
    }
}

@Composable
fun PlatformBottomNavigationBar(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    if (platformUsesCupertinoChrome()) {
        val glassColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.38f)
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            shape = RoundedCornerShape(30.dp),
            color = glassColor,
            contentColor = contentColorFor(glassColor),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
            border = BorderStroke(
                width = 0.8.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.16f),
            ),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 8.dp),
                content = content,
            )
        }
    } else {
        NavigationBar(
            modifier = modifier,
            containerColor = MaterialTheme.colorScheme.surface,
            content = content,
        )
    }
}
