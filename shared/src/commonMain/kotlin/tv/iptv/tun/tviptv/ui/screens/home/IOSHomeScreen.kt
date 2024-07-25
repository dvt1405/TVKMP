package tv.iptv.tun.tviptv.ui.screens.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import tv.iptv.tun.tviptv.ui.screens.history.HistoryScreen
import tv.iptv.tun.tviptv.ui.screens.iptv.IPTVMainScreen
import tv.iptv.tun.tviptv.ui.screens.settings.SettingsScreen

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun IOSHomeScreen(nav: NavHostController) {
    val pagerState = rememberPagerState(1) {
        4
    }
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = Modifier,
        bottomBar = {
            NavigationBar {
                for (i in 0..3) {
                    val selectedItem = pagerState.currentPage == i
                    NavigationBarItem(selectedItem, onClick = {
                        scope.launch {
                            pagerState.scrollToPage(i)
                        }
                    }, icon = {
                        when (i) {
                            0 -> Icons.Rounded.Home
                            1 -> Icons.Rounded.History
                            2 -> Icons.Rounded.Settings
                        }
                    })
                }
            }
        },
        content = {
            VerticalPager(state = pagerState) { page ->
                when (page) {
                    0 -> IPTVMainScreen(nav)
                    1 -> HistoryScreen()
                    2 -> SettingsScreen()
                }
            }
            Column {

            }
        }
    )
}