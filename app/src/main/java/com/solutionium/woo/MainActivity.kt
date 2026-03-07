package com.solutionium.woo

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.solutionium.sharedui.designsystem.theme.WooBrand
import com.solutionium.sharedui.designsystem.theme.WooTheme
import com.solutionium.sharedui.common.component.LanguageSelectionScreen
import com.solutionium.shared.data.local.AppPreferences
import com.solutionium.woo.ui.WooApp
import org.koin.compose.viewmodel.koinViewModel
import org.koin.java.KoinJavaComponent.getKoin
import java.util.Locale

//@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission granted. FCM can post notifications.
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    override fun attachBaseContext(newBase: Context) {
        val appPreferences: AppPreferences = getKoin().get()
        val lang = appPreferences.getLanguage()
        val localeToSet = Locale.forLanguageTag(lang ?: defaultLanguageForBrand())
        val config = Configuration(newBase.resources.configuration)
        Locale.setDefault(localeToSet)
        config.setLocale(localeToSet)
        config.setLayoutDirection(localeToSet)

        val updatedContext = newBase.createConfigurationContext(config)
        super.attachBaseContext(updatedContext)
    }

    @SuppressLint("UnsafeOptInUsageError")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        askNotificationPermission()
        enableEdgeToEdge()

        setContent {
            val viewModel: MainViewModel = koinViewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            LaunchedEffect(uiState.languageCode) {
                val currentLang = resources.configuration.locales[0].language
                if (uiState.languageCode != null && uiState.languageCode != currentLang) {
                    recreate()
                }
            }

            if (uiState.isLoading) {
                Surface(modifier = Modifier.fillMaxSize()) { }
                return@setContent
            }

            val currentLanguage = uiState.languageCode ?: defaultLanguageForBrand()
            val layoutDirection = if (isRtlLanguage(currentLanguage)) {
                LayoutDirection.Rtl
            } else {
                LayoutDirection.Ltr
            }
            val brand = when (BuildConfig.SITE_BRAND) {
                "SITE_B" -> WooBrand.SiteB
                else -> WooBrand.SiteA
            }

            CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                WooTheme(brand = brand) {
                    if (uiState.showLanguageScreen) {
                        LanguageSelectionScreen(
                            onLanguageSelected = viewModel::onLanguageSelected,
                        )
                    } else {
                        WooApp()
                    }
                }
            }
        }
    }

    private fun isRtlLanguage(languageCode: String): Boolean {
        return languageCode == "fa" || languageCode == "ar"
    }

    private fun defaultLanguageForBrand(): String {
        return if (BuildConfig.SITE_BRAND == "SITE_B") "ar" else "fa"
    }
}
