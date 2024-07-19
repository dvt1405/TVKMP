package tv.iptv.tun.tviptv.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.toCollection
import kotlinx.coroutines.launch
import tv.iptv.tun.tviptv.Greeting
import tv.iptv.tun.tviptv.database.DatabaseQueries
import tv.iptv.tun.tviptv.database.sqlDriverFactory
import tv.iptv.tun.tviptv.jsonxml.ProgrammeResponseDTO
import tv.iptv.tun.tviptv.network.HttpClientManager
import tv.iptv.tun.tviptv.repository.iptv.ExtensionsChannel
import tv.iptv.tun.tviptv.repository.iptv.IPTVSourceConfig
import tv.iptv.tun.tviptv.repository.iptv.ParserIPTVDataSource

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    GreetingView(Greeting().greet())
                }
            }
        }
        lifecycleScope.launch(Dispatchers.IO) {
            val parser = ParserIPTVDataSource()
            parser.parseSource(
                IPTVSourceConfig(
                    "Test",
                    "https://tv.volam.pro/playlist/"
                )
            ).toCollection(mutableListOf()).forEach {
                println(it)
            }
            DatabaseQueries(sqlDriverFactory)
                .selectIPTVChannel()
                .executeAsList()
                .forEach {
                    println(it)
                }
        }
    }
}

@Composable
fun GreetingView(text: String) {
    Text(text = text)
}

@Preview
@Composable
fun DefaultPreview() {
    MyApplicationTheme {
        GreetingView("Hello, Android!")
    }
}
