package tv.iptv.tun.tviptv.ui.screens.privacy

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import tv.iptv.tun.tviptv.ui.customview.RoundedButton
import tv.iptv.tun.tviptv.ui.customview.WebViewCompose
import tv.iptv.tun.tviptv.ui.screens.welcome.WelcomeViewModel

expect fun getHtmlPrivacyUrl(): String

@Composable
fun PrivacyScreen(
    nav: NavHostController,
    welcomeViewModel: WelcomeViewModel
) {
    Scaffold(
        bottomBar = {
            NavigationBar {
                RoundedButton(
                    title = "Đồng ý và tiếp tục",
                    onClick = {
                        welcomeViewModel.markDone()
                    },
                    modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
            }
        }
    ) {
        WebViewCompose(
            modifier = Modifier.fillMaxSize()
                .padding(bottom = it.calculateBottomPadding()),
            url = getHtmlPrivacyUrl()
        )
    }
}