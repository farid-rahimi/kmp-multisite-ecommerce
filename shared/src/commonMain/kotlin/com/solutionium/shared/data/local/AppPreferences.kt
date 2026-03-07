package com.solutionium.shared.data.local

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.Settings
import com.russhwolf.settings.coroutines.getStringOrNullFlow
import kotlinx.coroutines.flow.Flow


// In your core/data module
interface AppPreferences {
    fun language(): Flow<String?>
    fun getLanguage(): String?
    fun setLanguage(languageCode: String)

    // --- ADD THESE NEW METHODS ---
    fun getFcmToken(): String?
    fun setFcmToken(token: String)
}

class AppPreferencesImpl(
    private val settings: Settings
) : AppPreferences {

    // Cast to ObservableSettings to enable Flow support
    private val observableSettings: ObservableSettings by lazy {
        settings as? ObservableSettings
            ?: throw IllegalStateException("Settings must be observable for Flow support")
    }

    companion object {
        private const val KEY_LANGUAGE = "app_language"
        private const val KEY_FCM_TOKEN = "fcm_token"
    }

    // Replaces manual callbackFlow logic
    @OptIn(ExperimentalSettingsApi::class)
    override fun language(): Flow<String?> {
        return observableSettings.getStringOrNullFlow(KEY_LANGUAGE)
    }

    override fun getLanguage(): String? {
        return settings.getStringOrNull(KEY_LANGUAGE)
    }

    override fun setLanguage(languageCode: String) {
        settings.putString(KEY_LANGUAGE, languageCode)
        applyPlatformLanguage(languageCode)
    }

    override fun getFcmToken(): String? {
        return settings.getStringOrNull(KEY_FCM_TOKEN)
    }

    override fun setFcmToken(token: String) {
        settings.putString(KEY_FCM_TOKEN, token)
    }
}
