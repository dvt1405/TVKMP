package tv.iptv.tun.tviptv.ui.customview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import tv.iptv.tun.tviptv.storage.KeyValueStorageForTesting
import tv.iptv.tun.tviptv.ui.screens.welcome.WelcomeScreen
import tv.iptv.tun.tviptv.ui.screens.welcome.WelcomeViewModel

@Composable
@Preview("WelcomeScreen")
fun WelcomeScreenPreview() {
    WelcomeScreen(
        rememberNavController(),
        WelcomeViewModel(KeyValueStorageForTesting)
    )
}