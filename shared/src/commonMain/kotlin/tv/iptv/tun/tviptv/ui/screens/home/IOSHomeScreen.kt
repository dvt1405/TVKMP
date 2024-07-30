package tv.iptv.tun.tviptv.ui.screens.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.vectorResource
import tv.iptv.tun.tviptv.ui.screens.history.HistoryScreen
import tv.iptv.tun.tviptv.ui.screens.iptv.IPTVMainScreen
import tv.iptv.tun.tviptv.ui.screens.iptv.IPTVViewModel
import tv.iptv.tun.tviptv.ui.screens.settings.SettingsScreen
import tv__iptv.shared.generated.resources.Res
import tv__iptv.shared.generated.resources.ic_live

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun IOSHomeScreen(
    nav: NavHostController,
    iptvViewModel: IPTVViewModel = viewModel { IPTVViewModel() }
) {
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
                for (i in 0..2) {
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
                                0 -> vectorResource(Res.drawable.ic_live)
                                1 -> Icons.Rounded.History
                                else -> Icons.Rounded.Settings
                            }
                            Icon(icon, "")
                        }, label = {
                            val lb = when (i) {
                                0 -> "Home"
                                1 -> "History"
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
                    0 -> IPTVMainScreen(nav, iptvViewModel)
                    1 -> HistoryScreen()
                    2 -> SettingsScreen()
                    else -> SettingsScreen()
                }
            }
        }
    )
}