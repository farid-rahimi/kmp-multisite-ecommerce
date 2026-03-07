package com.solutionium.woo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.messaging.FirebaseMessaging
import com.solutionium.shared.data.local.AppPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(
    private val appPreferences: AppPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            appPreferences.language().collect { langCode ->
                // Check if the language is being collected for the first time.
                val isInitialLaunch = langCode == null

                _uiState.update {
                    it.copy(
                        languageCode = langCode,
                        isLoading = false,
                        // Show language screen only on the very first launch
                        showLanguageScreen = isInitialLaunch && it.showLanguageScreen
                    )
                }
            }
        }

        fetchAndSaveFcmToken()
    }

    private fun fetchAndSaveFcmToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                //Log.w("MainViewModel", "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result
            //Log.d("MainViewModel", "FCM Token fetched: $token")

            // Save the token to SharedPreferences using a coroutine
            viewModelScope.launch {
                appPreferences.setFcmToken(token)
            }
        }
    }

    fun onLanguageSelected(languageCode: String) {
        viewModelScope.launch {
            appPreferences.setLanguage(languageCode)
            // After selection, hide the language screen for the rest of the session
            _uiState.update { it.copy(showLanguageScreen = false) }
        }
    }
}

data class MainUiState(
    val isLoading: Boolean = true,
    val languageCode: String? = null, // e.g., "fa" or "en"
    val showLanguageScreen: Boolean = true // Assume true until we know otherwise
)
