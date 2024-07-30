package tv.iptv.tun.tviptv.ui.screens.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.vectorResource
import tv.iptv.tun.tviptv.PlatformOS
import tv.iptv.tun.tviptv.getPlatform
import tv.iptv.tun.tviptv.ui.screens.history.HistoryScreen
import tv.iptv.tun.tviptv.ui.screens.iptv.IPTVMainScreen
import tv.iptv.tun.tviptv.ui.screens.iptv.IPTVViewModel
import tv.iptv.tun.tviptv.ui.screens.settings.SettingsScreen
import tv.iptv.tun.tviptv.ui.tvsreen.TVChannelListScreen
import tv__iptv.shared.generated.resources.Res
import tv__iptv.shared.generated.resources.ic_live

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    nav: NavHostController,
    iptvViewModel: IPTVViewModel = viewModel { IPTVViewModel() }
) {
    val pagerState = rememberPagerState(0) {
        homePages.size
    }
    val scope = rememberCoroutineScope()
    Scaffold(
        modifier = Modifier,
        bottomBar = {
            NavigationBar(
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                for (i in homePages.indices) {
                    val selectedItem = pagerState.currentPage == i
                    NavigationBarItem(selectedItem,
                        colors = NavigationBarItemDefaults.colors()
                            .copy(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary
                            ),
                        onClick = {
                            scope.launch {
                                pagerState.scrollToPage(i)
                            }
                        }, icon = {
                            val resource = homePages[i].icon
                            val icon = if (resource is DrawableResource) {
                                vectorResource(resource)
                            } else {
                                resource as ImageVector
                            }
                            Icon(icon, "")
                        }, label = {
                            homePages[i].label
                        })
                }
            }
        },
        content = {
            HorizontalPager(state = pagerState) { page ->
                when (homePages[page]) {
                    Page.Home -> TVChannelListScreen()
                    Page.History -> HistoryScreen()
                    Page.IPTV -> IPTVMainScreen(nav, iptvViewModel)
                    Page.Settings -> SettingsScreen()
                    else -> SettingsScreen()
                }
            }
        }
    )
}

enum class Page(val icon: Any, val label: String) {
    Home(Icons.Rounded.Home, "Home"),
    History(Icons.Rounded.History, "History"),
    IPTV(Res.drawable.ic_live, "IPTV"),
    Settings(Icons.Rounded.Settings, "Settings"),
    Favourite(Icons.Rounded.Favorite, "Favorite")
}

internal val homePages by lazy {
    if (homePagesByOS == null) {
        val os = getPlatform().os
        homePagesByOS = when (os) {
            PlatformOS.Android -> {
                listOf(
                    Page.Home,
                    Page.History,
                    Page.IPTV,
                    Page.Settings,
                )
            }

            PlatformOS.IOS -> {
                listOf(
                    Page.IPTV,
                    Page.History,
                    Page.Settings,
                )
            }

            else -> {
                listOf(
                    Page.Home,
                    Page.History,
                    Page.IPTV,
                    Page.Settings,
                )
            }
        }
    }
    homePagesByOS!!
}

internal var homePagesByOS: List<Page>? = null