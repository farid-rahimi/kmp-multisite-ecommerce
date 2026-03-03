package com.solutionium.woo.ui


import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navOptions
import com.solutionium.sharedui.common.GlobalUiState
import com.solutionium.sharedui.common.LoginPromptDialog
import com.solutionium.sharedui.common.component.StoryViewer
import com.solutionium.feature.account.navigateToAccount
import com.solutionium.feature.home.GRAPH_HOME_ROUTE
import com.solutionium.shared.viewmodel.CartViewModel
import com.solutionium.shared.viewmodel.HomeNavigationEvent
import com.solutionium.shared.viewmodel.HomeViewModel
import com.solutionium.feature.product.detail.navigateProductDetail
import com.solutionium.feature.product.list.navigateProductList
import com.solutionium.woo.ui.navigation.RootDestination
import com.solutionium.woo.ui.navigation.WooNavigationBar
import com.solutionium.woo.ui.navigation.RootScreen.Home
import com.solutionium.woo.ui.navigation.RootScreen.Category
import com.solutionium.woo.ui.navigation.RootScreen.Cart
import com.solutionium.woo.ui.navigation.RootScreen.Account
import com.solutionium.woo.ui.navigation.WooNavHost
import org.koin.compose.koinInject

@Composable
fun WooApp(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    val cartViewModel: CartViewModel = koinInject()
    val cartUiState by cartViewModel.uiState.collectAsState()

    val homeViewModel: HomeViewModel = koinInject()
    val homeState by homeViewModel.state.collectAsStateWithLifecycle()

    DisposableEffect(cartViewModel) {
        onDispose { cartViewModel.clear() }
    }

    DisposableEffect(homeViewModel) {
        onDispose { homeViewModel.clear() }
    }

    // 2. Manage the state for showing the StoryViewer here.
    var showStoryViewer by rememberSaveable { mutableStateOf(false) }
    var storyStartIndex by rememberSaveable { mutableStateOf(0) }

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        homeViewModel.navigationEvent.collect { event ->
            when (event) {
                is HomeNavigationEvent.ToProduct -> {
                    navController.navigateProductDetail(rootRoute = GRAPH_HOME_ROUTE, productId = event.productId)
                }
                is HomeNavigationEvent.ToProductList -> {
                    navController.navigateProductList(rootRoute = GRAPH_HOME_ROUTE, params = event.params)
                }
                is HomeNavigationEvent.ToExternalLink -> {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, event.url.toUri())
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
    ) {
        Scaffold(
            modifier = modifier,
            bottomBar = {
                if (currentRoute?.contains("checkout") != true)
                    WooNavigationBar(
                        destinations = RootDestination.entries,
                        currentDestination = navController.currentBackStackEntryAsState().value?.destination,
                        cartCount = cartUiState.cartItemCount,
                        onNavigationSelected = { destination ->
                            val topLevelNavOptions = navOptions {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }

                            when (destination) {
                                Home -> navController.navigate(
                                    Home.destinationRoute.route,
                                    topLevelNavOptions
                                )

                                Category -> navController.navigate(
                                    Category.destinationRoute.route,
                                    topLevelNavOptions
                                )

                                Cart -> navController.navigate(
                                    Cart.destinationRoute.route,
                                    topLevelNavOptions,
                                )

                                Account -> navController.navigate(
                                    Account.destinationRoute.route,
                                    topLevelNavOptions
                                )
                            }
                        },
                    )
            },
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
        ) { padding ->
            WooNavHost(
                modifier = Modifier
                    .padding(padding)
                    .consumeWindowInsets(padding),
                navController = navController,
                homeViewModel = homeViewModel,
                onStoryClick = { clickedStory ->
                    val indexInReorderedList = homeState.storyItems.indexOf(clickedStory)
                    storyStartIndex = indexInReorderedList
                    showStoryViewer = true // Trigger the viewer from here
                },

            )

            val globalState by GlobalUiState.state.collectAsState()

            if (globalState.showLoginPrompt) {
                LoginPromptDialog(
                    onDismiss = { GlobalUiState.dismissLoginPrompt() },
                    onConfirm = {
                        // First dismiss the dialog
                        GlobalUiState.dismissLoginPrompt()
                        // Then navigate to the login/account flow
                        navController.navigateToAccount() // Or navigateToAuth()
                    }
                )
            }
        }

        if (showStoryViewer && homeState.storyItems.isNotEmpty()) {
            StoryViewer(
                stories = homeState.storyItems,
                startIndex = storyStartIndex,
                onLinkClick = {
                    showStoryViewer = false
                    homeViewModel.persistViewedStories()
                    homeViewModel.onLinkClick(it)
                    },
                onClose = {
                    showStoryViewer = false
                    homeViewModel.persistViewedStories()
                },
                onStoryViewed = { storyId -> homeViewModel.setStoryAsViewedInSession(storyId) }
            )
        }
    }
}
