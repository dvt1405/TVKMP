package tv.iptv.tun.tviptv.ui.screens.iptv

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import tv.iptv.tun.tviptv.ui.screens.iptv.addiptv.AddIPTVBottomSheet
import tv.iptv.tun.tviptv.ui.screens.iptv.addiptv.AddIPTVScreen

@Composable
@Preview(name = "AddIPTV")
fun AddIPTVPreview() {
    AddIPTVScreen()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview(name = "AddIPTVModel")
fun AddIPTVPreViewModel() {
    AddIPTVBottomSheet()
}