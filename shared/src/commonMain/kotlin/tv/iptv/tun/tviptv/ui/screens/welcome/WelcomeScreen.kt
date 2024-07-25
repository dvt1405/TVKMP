package tv.iptv.tun.tviptv.ui.screens.welcome

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import tv.iptv.tun.tviptv.ui.customview.RoundedButton
import tv.iptv.tun.tviptv.ui.navigations.NavDestinations
import tv__iptv.shared.generated.resources.Res
import tv__iptv.shared.generated.resources.compose_multiplatform
import tv__iptv.shared.generated.resources.welcome_screen_btn_title

@Composable
fun WelcomeScreen(
    nav: NavHostController = rememberNavController(),
    welcomeViewModel: WelcomeViewModel = viewModel { WelcomeViewModel() }
) {
    val ui = welcomeViewModel.uiState.collectAsState()
    val isBegin = ui.value != UIWelcomeData.Begin
    Scaffold {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (isBegin) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                )
        ) {
            if (ui.value != UIWelcomeData.Begin) {
                CircularProgressIndicator(
                    modifier = Modifier.size(50.dp)
                        .align(Alignment.Center),
                    strokeWidth = 4.dp,
                    strokeCap = StrokeCap.Round,
                    color = MaterialTheme.colorScheme.secondary
                )
            } else {
                Column(
                    modifier = Modifier.align(Alignment.Center)
                        .padding(horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(Res.drawable.compose_multiplatform),
                        "AppLogo",
                        modifier = Modifier
                            .padding(top = it.calculateTopPadding())
                            .size(250.dp)
                    )
                    Spacer(Modifier.height(20.dp))
                    Text(
                        "Welcome to TV Live app",
                        style = MaterialTheme.typography.headlineMedium
                            .copy(
                                color = MaterialTheme.colorScheme
                                    .onPrimary
                            ),
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Trải nghiệm ngay TV live với hàng trăm kênh truyền hình đa dạng từ các quốc gia trên thế giới từ các nguồn IPTV",
                        style = MaterialTheme.typography.bodyMedium
                            .copy(
                                color = MaterialTheme.typography.bodyMedium
                                    .color
                                    .copy(alpha = 0.8f)
                            ),
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(20.dp))
                    RoundedButton(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(it.calculateBottomPadding()),
                        title = stringResource(Res.string.welcome_screen_btn_title),
                        onClick = {
                            welcomeViewModel.goToOnboard()
                        },
                        fontSize = 16.sp
                    )

                }
            }
        }
    }
}