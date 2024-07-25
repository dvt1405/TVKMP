package tv.iptv.tun.tviptv.ui.screens.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.MusicVideo
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemColors
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.vectorResource
import tv.iptv.tun.tviptv.ui.MyApplicationTheme
import tv.iptv.tun.tviptv.ui.screens.history.HistoryScreen
import tv.iptv.tun.tviptv.ui.screens.iptv.IPTVMainScreen
import tv.iptv.tun.tviptv.ui.screens.settings.SettingsScreen
import tv.iptv.tun.tviptv.ui.tvsreen.TVChannelListScreen
import tv__iptv.shared.generated.resources.Res
import tv__iptv.shared.generated.resources.ic_live

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AndroidHomeScreen(nav: NavHostController) {
    val pagerState = rememberPagerState(0) {
        4
    }
    val scope = rememberCoroutineScope()
    Scaffold(
        modifier = Modifier,
        bottomBar = {
            NavigationBar(
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                for (i in 0..3) {
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
                            val icon = when (i) {
                                0 -> Icons.Rounded.Home
                                1 -> Icons.Rounded.History
                                2 -> vectorResource(Res.drawable.ic_live)
                                else -> Icons.Rounded.Settings
                            }
                            Icon(icon, "")
                        }, label = {
                            val lb = when (i) {
                                0 -> "Home"
                                1 -> "History"
                                2 -> "IPTV"
                                else -> "Settings"
                            }
                            Text(lb)
                        })
                }
            }
        },
        content = {
            HorizontalPager(state = pagerState) { page ->
                when (page) {
                    0 -> TVChannelListScreen()
                    1 -> HistoryScreen()
                    2 -> IPTVMainScreen(nav)
                    3 -> SettingsScreen()
                }
            }
        }
    )
}