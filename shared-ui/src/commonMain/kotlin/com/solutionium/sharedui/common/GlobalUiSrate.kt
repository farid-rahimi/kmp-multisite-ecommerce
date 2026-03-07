package com.solutionium.sharedui.common

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * A singleton object to manage global UI states, like app-level dialogs.
 */
object GlobalUiState {

    private val _state = MutableStateFlow(GlobalState())
    val state = _state.asStateFlow()

    /**
     * Call this from any ViewModel to request the login prompt to be shown.
     */
    fun showLoginPrompt() {
        _state.update { it.copy(showLoginPrompt = true) }
    }

    /**
     * Call this from the UI to dismiss the prompt.
     */
    fun dismissLoginPrompt() {
        _state.update { it.copy(showLoginPrompt = false) }
    }
}

data class GlobalState(
    val showLoginPrompt: Boolean = false
)