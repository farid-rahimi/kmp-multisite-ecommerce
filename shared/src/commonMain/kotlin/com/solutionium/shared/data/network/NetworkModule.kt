package com.solutionium.shared.data.network

import com.russhwolf.settings.Settings
import com.solutionium.shared.data.network.clients.DigitsClient
import com.solutionium.shared.data.network.clients.UserClient
import com.solutionium.shared.data.network.clients.WooCategoryClient
import com.solutionium.shared.data.network.clients.WooCheckoutOrderClient
import com.solutionium.shared.data.network.clients.WooOrderClient
import com.solutionium.shared.data.network.clients.WooProductClient
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.qualifier.named
import org.koin.dsl.module
import io.ktor.util.encodeBase64
import com.solutionium.shared.BuildKonfig

fun getNetworkDataModules() = setOf(networkModule)

data class NetworkConfig(
    val baseUrl: String,
    val consumerKey: String,
    val consumerSecret: String,
    val passwordLoginPath: String = "wp-json/digits/v1/login_user",
    val enableNetworkLogs: Boolean = true,
)

fun interface NetworkConfigProvider {
    fun get(): NetworkConfig
}

private fun defaultNetworkConfig() = NetworkConfig(
    baseUrl = BuildKonfig.BASE_URL,
    consumerKey = BuildKonfig.CONSUMER_KEY,
    consumerSecret = BuildKonfig.CONSUMER_SECRET,
    enableNetworkLogs = true,
)


expect val platformEngine: HttpClientEngine

val networkModule = module {

    single {
        Json { ignoreUnknownKeys = true }
    }

    // --- HTTP CLIENTS ---

    single(named("BasicAuthKtorClient")) {
        val networkConfig = getOrNull<NetworkConfigProvider>()?.get() ?: defaultNetworkConfig()
        HttpClient(platformEngine) {
            install(ContentNegotiation) { json(get()) }
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        println("Network: Ktor: $message")
                    }
                }
                level = if (networkConfig.enableNetworkLogs) LogLevel.BODY else LogLevel.NONE
                sanitizeHeader { header -> header == HttpHeaders.Authorization }
            }
            install(DefaultRequest) {
                url(networkConfig.baseUrl)
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                val auth = "${networkConfig.consumerKey}:${networkConfig.consumerSecret}"
                    .encodeToByteArray()
                    .encodeBase64()

                println("Network: Ktor: $auth")

//                val auth = "${consumerKey}:${consumerSecret}"
//                    .encodeToByteArray().let { Base64.encodeToString(it, Base64.NO_WRAP) }
                header(HttpHeaders.Authorization, "Basic $auth")
            }
        }
    }

    single(named("BearerAuthKtorClient")) {
        val networkConfig = getOrNull<NetworkConfigProvider>()?.get() ?: defaultNetworkConfig()
        HttpClient(platformEngine) {
            install(ContentNegotiation) { json(get()) }
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        println("Network: Ktor: $message")
                    }
                }
                level = if (networkConfig.enableNetworkLogs) LogLevel.BODY else LogLevel.NONE
                sanitizeHeader { header -> header == HttpHeaders.Authorization }
            }
            install(DefaultRequest) {
                url(networkConfig.baseUrl)
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                // Inject TokenStore here if needed for dynamic Bearer tokens
            }
        }
    }

    single(named("NoAuthKtorClient")) {
        val networkConfig = getOrNull<NetworkConfigProvider>()?.get() ?: defaultNetworkConfig()
        HttpClient(platformEngine) {
            install(ContentNegotiation) { json(get()) }
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        println("Network: Ktor: $message")
                    }
                }
                level = if (networkConfig.enableNetworkLogs) LogLevel.BODY else LogLevel.NONE
                sanitizeHeader { header -> header == HttpHeaders.Authorization }
            }
            install(DefaultRequest) {
                url(networkConfig.baseUrl)
                header(HttpHeaders.ContentType, ContentType.Application.Json)
            }
        }
    }

    // --- API CLIENTS (Mapping them to the correct HttpClient) ---

    single { WooProductClient(get(named("BasicAuthKtorClient"))) }
    single { WooCategoryClient(get(named("BasicAuthKtorClient"))) }
    single { WooCheckoutOrderClient(get(named("BasicAuthKtorClient"))) }
    single { WooOrderClient(get(named("BasicAuthKtorClient"))) }
    
    single {
        val networkConfig = getOrNull<NetworkConfigProvider>()?.get() ?: defaultNetworkConfig()
        DigitsClient(
            client = get(named("NoAuthKtorClient")),
            passwordLoginPath = networkConfig.passwordLoginPath,
        )
    }
    single { UserClient(get(named("NoAuthKtorClient"))) }

    // --- OTHER ---

    single<Settings> { Settings() }
//    single {
//        val context = androidContext()
//        context.getSharedPreferences(context.packageName + "_preferences", Context.MODE_PRIVATE)
//    }
}
