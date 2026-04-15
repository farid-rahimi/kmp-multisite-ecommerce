package com.solutionium.woo

import android.app.Activity
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.core.view.WindowCompat
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
    private var paymentReturnStatus by mutableStateOf<String?>(null)
    private var paymentReturnOrderId by mutableStateOf<Int?>(null)

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ : Boolean ->
            // Permission granted. FCM can post notifications.

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

        applyPaymentReturnFromIntent(intent)
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
                WooTheme(brand = brand, languageCode = currentLanguage) {
                    ApplySystemBars()
                    if (uiState.showLanguageScreen) {
                        LanguageSelectionScreen(
                            onLanguageSelected = viewModel::onLanguageSelected,
                        )
                    } else {
                        WooApp(
                            paymentReturnStatus = paymentReturnStatus,
                            paymentReturnOrderId = paymentReturnOrderId,
                            onPaymentReturnConsumed = {
                                paymentReturnStatus = null
                                paymentReturnOrderId = null
                            },
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        applyPaymentReturnFromIntent(intent)
    }

    private fun applyPaymentReturnFromIntent(intent: Intent?) {
        val data = intent?.data ?: return
        if (
            data.scheme?.equals(BuildConfig.PAYMENT_RETURN_SCHEME, ignoreCase = true) != true ||
            data.host != "payment-return"
        ) {
            return
        }

        val queryStatus = data.getQueryParameter("status")
            ?.trim()
            ?.lowercase(Locale.US)
            ?.takeIf { it.isNotBlank() }

        val inferredStatus = when {
            queryStatus != null -> queryStatus
            data.path?.contains("order-received", ignoreCase = true) == true -> "success"
            data.path?.contains("success", ignoreCase = true) == true -> "success"
            data.path?.contains("cancel", ignoreCase = true) == true -> "canceled"
            data.path?.contains("fail", ignoreCase = true) == true -> "failed"
            else -> null
        } ?: return

        val orderId = data.getQueryParameter("order_id")
            ?.trim()
            ?.toIntOrNull()

        paymentReturnStatus = inferredStatus
        paymentReturnOrderId = orderId
    }

    private fun isRtlLanguage(languageCode: String): Boolean {
        return languageCode == "fa" || languageCode == "ar"
    }

    private fun defaultLanguageForBrand(): String {
        return if (BuildConfig.SITE_BRAND == "SITE_B") "ar" else "en"
    }
}

@Composable
private fun ApplySystemBars() {
    val view = LocalView.current
    val darkTheme = isSystemInDarkTheme()
    val barColor = MaterialTheme.colorScheme.background.toArgb()
    if (view.isInEditMode) return

    SideEffect {
        val window = (view.context as Activity).window
        window.statusBarColor = barColor
        window.navigationBarColor = barColor

        val insetsController = WindowCompat.getInsetsController(window, view)
        insetsController.isAppearanceLightStatusBars = !darkTheme
        insetsController.isAppearanceLightNavigationBars = !darkTheme
    }
}
