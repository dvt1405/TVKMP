package tv.iptv.tun.tviptv.ui.navigations

import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import tv.iptv.tun.tviptv.storage.KeyValueStorage
import tv.iptv.tun.tviptv.ui.screens.home.HomeScreen
import tv.iptv.tun.tviptv.ui.screens.iptv.IPTVViewModel
import tv.iptv.tun.tviptv.ui.screens.iptv.addiptv.AddIPTVBottomSheet
import tv.iptv.tun.tviptv.ui.screens.iptv.addiptv.AddIPTVScreen
import tv.iptv.tun.tviptv.ui.screens.onboard.OnboardScreen
import tv.iptv.tun.tviptv.ui.screens.privacy.PrivacyScreen
import tv.iptv.tun.tviptv.ui.screens.welcome.UIWelcomeData
import tv.iptv.tun.tviptv.ui.screens.welcome.WelcomeScreen
import tv.iptv.tun.tviptv.ui.screens.welcome.WelcomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TVAppNavHost(nav: NavHostController = rememberNavController()) {
    val welcomeViewModel = viewModel { WelcomeViewModel(KeyValueStorage) }
    val iptvViewModel = viewModel { IPTVViewModel() }
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
        composable(NavDestinations.AddIPTVScreen.name) {
            AddIPTVScreen(nav, iptvViewModel)
        }
        composable(NavDestinations.AddIPTVScreenBottomSheet.name) {
            AddIPTVBottomSheet(nav, iptvViewModel)
        }
        composable(
            "${NavDestinations.AddIPTVScreenBottomSheet.name}?url={url}&name={name}",
            arguments = listOf(
                navArgument("url") {
                    nullable = false
                    type = NavType.StringType
                },
                navArgument("name") {
                    nullable = true
                    type = NavType.StringType
                }
            )
        ) {
            val url = it.arguments?.getString("url")
            val name = it.arguments?.getString("name")
            AddIPTVBottomSheet(
                nav, iptvViewModel,
                defName = name,
                defUrl = url,
                onCancel = {
                    nav.popBackStack()
                }
            )
        }
        composable(NavDestinations.HomeScreen.name) {
            HomeScreen(nav, iptvViewModel)
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