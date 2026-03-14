package com.solutionium.sharedui.account

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.solutionium.shared.viewmodel.AccountStage
import com.solutionium.shared.viewmodel.AccountViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountAuthBottomSheet(
    viewModel: AccountViewModel,
    onDismiss: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(state.stage) {
        if (state.stage == AccountStage.LoggedIn) {
            onDismiss()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
    ) {
        AccountScreen(
            onAddressClick = {},
            onFavoriteClick = { _, _ -> },
            onOrdersClick = {},
            onOrderClick = {},
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f),
            viewModel = viewModel,
            onBack = onDismiss,
            clearOnDispose = false,
        )
    }
}
