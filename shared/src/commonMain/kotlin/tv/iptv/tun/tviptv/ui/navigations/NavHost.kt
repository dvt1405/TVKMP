package tv.iptv.tun.tviptv.ui.navigations

import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import tv.iptv.tun.tviptv.PlatformOS
import tv.iptv.tun.tviptv.getPlatform
import tv.iptv.tun.tviptv.storage.KeyValueStorage
import tv.iptv.tun.tviptv.ui.screens.home.AndroidHomeScreen
import tv.iptv.tun.tviptv.ui.screens.home.IOSHomeScreen
import tv.iptv.tun.tviptv.ui.screens.iptv.AddIPTVScreen
import tv.iptv.tun.tviptv.ui.screens.onboard.OnboardScreen
import tv.iptv.tun.tviptv.ui.screens.privacy.PrivacyScreen
import tv.iptv.tun.tviptv.ui.screens.welcome.UIWelcomeData
import tv.iptv.tun.tviptv.ui.screens.welcome.WelcomeScreen
import tv.iptv.tun.tviptv.ui.screens.welcome.WelcomeViewModel

@Composable
fun TVAppNavHost(nav: NavHostController = rememberNavController()) {
    val welcomeViewModel = viewModel { WelcomeViewModel(KeyValueStorage) }
    val ui = welcomeViewModel.uiState.collectAsState()
    NavHost(nav, NavDestinations.Welcome.name,
        enterTransition = { expandVertically(expandFrom = Alignment.Bottom) + fadeIn() },
        exitTransition = {
            fadeOut(targetAlpha = 1f)
        },
        popExitTransition = {
            shrinkVertically(
                shrinkTowards = Alignment.Top
            )
        },
        popEnterTransition = { fadeIn(initialAlpha = 0.3f) }) {
        composable(NavDestinations.Welcome.name) {
            WelcomeScreen(nav, welcomeViewModel)
        }
        composable(NavDestinations.Onboard.name) {
            OnboardScreen(nav, welcomeViewModel)
        }
        composable(NavDestinations.Privacy.name) {
            PrivacyScreen(nav, welcomeViewModel)
        }
        composable(NavDestinations.HomeScreen.name) {
            if (getPlatform().os == PlatformOS.Android) {
                AndroidHomeScreen(nav)
            } else {
                IOSHomeScreen(nav)
            }
        }

        composable(NavDestinations.IPTVScreen.name) {
            AddIPTVScreen(nav)
        }
    }
    when (ui.value) {
        UIWelcomeData.Privacy -> {
            nav.navigate(NavDestinations.Privacy.name) {
                popUpTo(NavDestinations.Welcome.name) {
                    inclusive = true
                }
            }
        }

        UIWelcomeData.Onboard -> {
            nav.navigate(NavDestinations.Onboard.name) {
                popUpTo(NavDestinations.Welcome.name) {
                    inclusive = true
                }
            }
        }

        UIWelcomeData.Done -> {
            nav.navigate(NavDestinations.HomeScreen.name) {
                popUpTo(NavDestinations.Welcome.name) {
                    inclusive = true
                }
            }
        }

        else -> {
        }
    }
}