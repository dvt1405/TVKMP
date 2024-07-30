package tv.iptv.tun.tviptv.ui.screens.iptv

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import tv.iptv.tun.tviptv.repository.iptv.IPTVChannel
import tv.iptv.tun.tviptv.repository.iptv.IPTVSourceConfig
import tv.iptv.tun.tviptv.ui.customview.CommonTopAppBar
import tv.iptv.tun.tviptv.ui.customview.IconButtonPositive
import tv.iptv.tun.tviptv.ui.screens.iptv.addiptv.AddIPTVBottomSheet
import tv.iptv.tun.tviptv.utils.getHost
import tv__iptv.shared.generated.resources.Res
import tv__iptv.shared.generated.resources.add_iptv_btn_title
import tv__iptv.shared.generated.resources.discover_iptv_description
import tv__iptv.shared.generated.resources.discover_iptv_title
import tv__iptv.shared.generated.resources.empty_iptv_source_description
import tv__iptv.shared.generated.resources.ic_launcher_playstore
import tv__iptv.shared.generated.resources.iptv_screen_main_title
import tv__iptv.shared.generated.resources.popular_iptv_title
import tv__iptv.shared.generated.resources.tv_entertainment
import tv__iptv.shared.generated.resources.what_is_iptv_description
import tv__iptv.shared.generated.resources.what_is_iptv_title

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IPTVMainScreen(
    nav: NavHostController = rememberNavController(),
    iptvViewModel: IPTVViewModel = viewModel { IPTVViewModel() }
) {
    val iptvUIStateState = iptvViewModel.uiState.collectAsState()
    val showBottomSheetState = mutableStateOf(false)
    val iptvSourceConfig = mutableStateOf<IPTVSourceConfig?>(null)
    val appBarTitle = mutableStateOf(stringResource(Res.string.iptv_screen_main_title))
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    Scaffold(
        topBar = {
            CommonTopAppBar(
                title = appBarTitle.value,
                scrollBehavior = scrollBehavior
            )
        },
        modifier = Modifier.fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
    ) {
        when (val state = iptvUIStateState.value) {
            is IPTVUIState.Empty, is IPTVUIState.LoadingIPTVChannel -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    IPTVMainEmptyContent(
                        nav, iptvViewModel,
                        state,
                        it,
                        showBottomSheetState,
                        iptvSourceConfig
                    )
                    if (state is IPTVUIState.LoadingIPTVChannel) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(50.dp)
                                .align(Alignment.Center)
                        )
                    }
                }

            }

            is IPTVUIState.IPTVWithListChannel -> {
                IPTVMainContent(
                    nav,
                    appBarTitle,
                    iptvViewModel,
                    state,
                    showBottomSheetState,
                    iptvSourceConfig,
                    it
                )
            }

            else -> {

            }
        }
        if (showBottomSheetState.value) {
            AddIPTVBottomSheet(
                nav, iptvViewModel,
                onCancel = {
                    showBottomSheetState.value = false
                }, sourceConfig = iptvSourceConfig.value
            )
        }
    }
}

@Composable
fun IPTVMainContent(
    nav: NavHostController,
    appBarTitle: MutableState<String>,
    iptvViewModel: IPTVViewModel,
    state: IPTVUIState.IPTVWithListChannel,
    showBottomSheetState: MutableState<Boolean>,
    iptvSourceConfig: MutableState<IPTVSourceConfig?>,
    paddingValues: PaddingValues
) {
    showBottomSheetState.value = false
    appBarTitle.value = iptvSourceConfig.value?.sourceName ?: appBarTitle.value
    val list = state.listChannel.groupBy {
        it.tvGroup
    }
    val spanCount = 2
    LazyVerticalGrid(
        modifier = Modifier.fillMaxSize()
            .padding(paddingValues)
            .padding(bottom = paddingValues.calculateBottomPadding() * 2),
        userScrollEnabled = true,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        columns = GridCells.Fixed(spanCount)
    ) {
        list.forEach { entry ->
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    entry.key,
                    style = MaterialTheme.typography
                        .titleMedium
                        .copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 16.sp
                        ),
                    modifier = Modifier.fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme
                                .surfaceTint
                                .copy(alpha = 0.1f)
                        )
                        .padding(
                            vertical = 12.dp,
                            horizontal = 16.dp
                        )
                )
            }
            items(span = {
                GridItemSpan(1)
            }, count = entry.value.size) {
                val channel = entry.value[it]
                val itemPaddingValues = if (it % spanCount == 0) {
                    PaddingValues(start = 16.dp)
                } else if (it % spanCount == spanCount - 1) {
                    PaddingValues(end = 16.dp)
                } else {
                    PaddingValues()
                }
                IPTVChannelItem(channel, itemPaddingValues) {}
            }
        }
    }
}

@Composable
fun IPTVChannelItem(
    item: IPTVChannel,
    paddingValues: PaddingValues,
    onClick: (IPTVChannel) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize()
            .padding(paddingValues)
            .clip(MaterialTheme.shapes.medium)
            .background(
                MaterialTheme.colorScheme
                    .surfaceTint
                    .copy(alpha = 0.1f)
            )
            .clickable {
                onClick(item)
            }
    ) {
        val imageRequest = ImageRequest.Builder(LocalPlatformContext.current)
            .data(item.logoChannel)
            .memoryCacheKey(item.logoChannel)
            .build()
        AsyncImage(
            model = imageRequest,
            placeholder = ColorPainter(
                MaterialTheme.colorScheme
                    .surfaceTint
                    .copy(0.1f)
            ),
            contentDescription = item.channelId,
            modifier = Modifier.fillMaxWidth()
                .height(120.dp)
                .clip(MaterialTheme.shapes.medium)
                .blur(25.dp)
                .alpha(0.5f),
            contentScale = ContentScale.Crop,
            error = painterResource(Res.drawable.ic_launcher_playstore)
        )
        Column(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = imageRequest,
                placeholder = ColorPainter(
                    MaterialTheme.colorScheme
                        .surfaceTint
                        .copy(0.1f)
                ),
                contentDescription = item.channelId,
                modifier = Modifier.fillMaxWidth()
                    .height(120.dp)
                    .clip(MaterialTheme.shapes.medium),
                error = painterResource(Res.drawable.ic_launcher_playstore)
            )
            Text(
                item.tvChannelName,
                style = MaterialTheme.typography
                    .titleMedium
                    .copy(fontSize = 14.sp),
                modifier = Modifier.fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 12.dp)
            )
        }
    }

}

@Composable
fun IPTVMainEmptyContent(
    nav: NavHostController,
    iptvViewModel: IPTVViewModel,
    state: IPTVUIState,
    paddingValues: PaddingValues,
    showBottomSheet: MutableState<Boolean>,
    iptvSourceConfig: MutableState<IPTVSourceConfig?>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(paddingValues.calculateTopPadding()))
        //              ===Big Image===
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .wrapContentHeight()
        ) {
            Image(
                painter = painterResource(Res.drawable.tv_entertainment),
                stringResource(Res.string.empty_iptv_source_description),
                modifier = Modifier.clip(RoundedCornerShape(8.dp))
            )
            Column(
                modifier = Modifier.fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.BottomStart)
            ) {
                Text(
                    stringResource(Res.string.discover_iptv_title),
                    style = MaterialTheme.typography.titleMedium
                        .copy(
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                )
                Text(
                    stringResource(Res.string.discover_iptv_description),
                    style = MaterialTheme.typography.bodySmall
                        .copy(
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                )
            }
        }


        Spacer(modifier = Modifier.height(24.dp))
        Text(
            stringResource(Res.string.what_is_iptv_title),
            style = MaterialTheme.typography.titleMedium
                .copy(
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.onSurface
                ),
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = 16.dp)
        )
        Text(
            stringResource(Res.string.what_is_iptv_description),
            style = MaterialTheme.typography.bodyMedium
                .copy(
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                ),
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))
        IconButtonPositive(
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = 16.dp),
            stringResource(Res.string.add_iptv_btn_title),
            onClick = {
                showBottomSheetAddIPTV(iptvSourceConfig, null, showBottomSheet)
            },
            containerColor = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            stringResource(Res.string.popular_iptv_title),
            style = MaterialTheme.typography.titleMedium
                .copy(
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.onSurface
                ),
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme
                        .surfaceTint
                        .copy(alpha = 0.1f)
                ).padding(
                    vertical = 12.dp,
                    horizontal = 16.dp
                ),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        iptvViewModel.popularListIPTV.forEachIndexed { index, item ->
            Text(
                item,
                modifier = Modifier.fillMaxWidth()
                    .clickable {
                        showBottomSheetAddIPTV(iptvSourceConfig, item, showBottomSheet)
                    }
                    .padding(vertical = 16.dp, horizontal = 16.dp),
                style = MaterialTheme.typography.bodyMedium
                    .copy(
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                fontWeight = FontWeight.Normal,
                fontStyle = FontStyle.Italic,
            )
            if (index != iptvViewModel.popularListIPTV.size - 1) {
                HorizontalDivider(
                    thickness = 1.dp,
                    color = Color(0x14000000),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        Spacer(modifier = Modifier.height(paddingValues.calculateBottomPadding() * 3))
    }
}

private fun showBottomSheetAddIPTV(
    iptvSourceConfig: MutableState<IPTVSourceConfig?>,
    item: String?,
    showBottomSheet: MutableState<Boolean>
) {
    iptvSourceConfig.value = item?.let {
        IPTVSourceConfig(
            sourceUrl = item,
            sourceName = item.getHost(),
            type = IPTVSourceConfig.Type.TV_CHANNEL
        )
    }
    showBottomSheet.value = true
}
