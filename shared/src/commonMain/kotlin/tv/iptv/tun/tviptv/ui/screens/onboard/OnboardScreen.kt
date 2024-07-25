package tv.iptv.tun.tviptv.ui.screens.onboard

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.util.lerp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import tv.iptv.tun.tviptv.ui.screens.welcome.WelcomeViewModel
import tv__iptv.shared.generated.resources.Res
import tv__iptv.shared.generated.resources.compose_multiplatform
import kotlin.math.absoluteValue

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardScreen(
    nav: NavHostController,
    welcomeViewModel: WelcomeViewModel = viewModel { WelcomeViewModel() }
) {
    val pagerState = rememberPagerState(0) {
        3
    }
    val scope = rememberCoroutineScope()
    HorizontalPager(
        modifier = Modifier.fillMaxSize()
            .background(Color.White),
        state = pagerState
    ) { currentPage ->
        OnboardScreenChild(
            modifier = Modifier.graphicsLayer {
                // Calculate the absolute offset for the current page from the
                // scroll position. We use the absolute value which allows us to mirror
                // any effects for both directions
                val pageOffset = ((pagerState.currentPage - currentPage) + pagerState
                    .currentPageOffsetFraction).absoluteValue

                // We animate the alpha, between 50% and 100%
                alpha = lerp(
                    start = 0.5f,
                    stop = 1f,
                    fraction = 1f - pageOffset.coerceIn(0f, 1f)
                )
            },
            currentPage, pagerState.pageCount
        ) {
            if (currentPage < pagerState.pageCount - 1) {
                scope.launch {
                    pagerState.animateScrollToPage((currentPage + 1) % pagerState.pageCount)
                }
            } else {
                welcomeViewModel.goToPrivacy()
            }
        }
    }
}

@Composable
fun OnboardScreenChild(
    modifier: Modifier = Modifier,
    pageNum: Int,
    pageCount: Int,
    onNext: () -> Unit
) {
    Column(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(Res.drawable.compose_multiplatform),
            "Onboard $pageNum"
        )
        ElevatedButton(onClick = onNext) {
            if (pageNum == pageCount - 1) {
                Text("Finish")
            } else {
                Text("Continue")
            }
        }
    }

}