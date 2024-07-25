package tv.iptv.tun.tviptv.ui.tvsreen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import tv.iptv.tun.tviptv.models.ChannelDTO
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TVChannelListScreen(modifier: Modifier = Modifier) {
    val lazyListState = rememberLazyListState()
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                modifier = Modifier.padding(horizontal = 16.dp),
                title = {
                    Text(
                        "TV Live", style = MaterialTheme.typography
                            .titleMedium.copy(
                                fontSize = 17.sp
                            )
                    )
                }, actions = {
                    Icon(Icons.Rounded.Search, "Search")
                })
        },
        bottomBar = {}
    ) {
        LazyColumn(
            state = lazyListState,
            modifier = Modifier.padding(
                top = it.calculateTopPadding(),
                start = 16.dp,
                end = 16.dp
            )
        ) {
            stickyHeader {
                Text(
                    "Continue Watching",
                    style = MaterialTheme.typography
                        .titleLarge.copy(
                            fontSize = 15.sp
                        ),
                    fontWeight = FontWeight.Bold
                )
            }
            items(1) {
                ShowCard(ChannelDTO.testObj)
            }
        }
    }
}

fun randomColor(): Int {
    val alpha = 128
    val red = Random.nextInt(256)
    val green = Random.nextInt(256)
    val blue = Random.nextInt(256)
    return (alpha shl 24) or (red shl 16) or (green shl 8) or (blue)
}

internal val backgroundItemContinueWatching = Brush.verticalGradient(
    listOf(
        Color(0x00000000),
        Color(0x66000000),
    )
)

@Composable
fun ShowCard(
    channelDTO: ChannelDTO,
    itemClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .wrapContentSize()
            .padding(end = 8.dp)
            .clickable {
                itemClick()
            },
        elevation = CardDefaults.elevatedCardElevation(4.dp)
    ) {
        Column {
            AsyncImage(
                model = ImageRequest.Builder(LocalPlatformContext.current)
                    .data(channelDTO.channelLogo)
                    .build(),
                contentDescription = null,
                placeholder = ColorPainter(Color(randomColor())),
                error = ColorPainter(Color.Red),
                onSuccess = {
//                placeholder = it.result.memoryCacheKey
                },
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .background(
                        backgroundItemContinueWatching,
                        MaterialTheme.shapes.small
                    )
                    .size(160.dp, 90.dp),
            )
            Text(
                text = channelDTO.channelName,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(8.dp, 4.dp)
            )
            Text(text = channelDTO.channelCategoryName, modifier = Modifier.padding(8.dp, 4.dp))
        }
    }
}