package com.solutionium.sharedui.common.component

import androidx.compose.runtime.Composable

@Composable
expect fun rememberPlatformShareAction(): (title: String, url: String) -> Unit
