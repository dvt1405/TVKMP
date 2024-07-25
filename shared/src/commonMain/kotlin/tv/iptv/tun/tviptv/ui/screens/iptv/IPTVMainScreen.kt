package tv.iptv.tun.tviptv.ui.screens.iptv

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IPTVMainScreen(
    nav: NavHostController = rememberNavController(),
    iptvViewModel: IPTVViewModel = viewModel { IPTVViewModel() }
) {
    AddIPTVScreen(nav)
}