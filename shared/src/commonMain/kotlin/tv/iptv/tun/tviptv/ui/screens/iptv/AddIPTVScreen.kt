package tv.iptv.tun.tviptv.ui.screens.iptv

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActionScope
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TextFieldDefaults.indicatorLine
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PlatformImeOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import io.ktor.http.Url
import tv.iptv.tun.tviptv.ui.customview.IconButtonNegative
import tv.iptv.tun.tviptv.ui.customview.IconButtonPositive

@Composable
fun AddIPTVScreen(
    nav: NavHostController = rememberNavController()
) {
    val textFieldValue = mutableStateOf("")
    val textFieldValue2 = mutableStateOf("")
    Scaffold(modifier = Modifier.fillMaxSize(),
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    nav.popBackStack()
                }) {
                    Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Back")
                }
                Text(
                    text = "Add IPTV Channel",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                IconButton(onClick = {

                }) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings")
                }
            }
        }
    ) {
        AddIPTVContent(
            modifier = Modifier.padding(
                top = it.calculateTopPadding(),
                bottom = it.calculateBottomPadding()
            ),
            channelNameValue = textFieldValue,
            channelUrlValue = textFieldValue2,
            onAddIPTChannel = { name, url ->

            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddIPTVBottomSheet() {
    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(
        onDismissRequest = {},
        sheetState = sheetState
    ) {
        AddIPTVContent()
    }
}

@Composable
internal fun AddIPTVContent(
    modifier: Modifier = Modifier,
    channelNameValue: MutableState<String> = mutableStateOf(""),
    channelUrlValue: MutableState<String> = mutableStateOf(""),
    onAddIPTChannel: (url: String, name: String) -> Unit = { _, _ -> },
    onCancel: () -> Unit = {}
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    Column(
        modifier = Modifier.fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = 16.dp)
            .then(modifier)
    ) {
        TextInputText(
            channelUrlValue,
            "IPTV Url",
            "Enter Url",
            imeAction = ImeAction.Done,
            keyboardType = KeyboardType.Uri,
            onImeAction = {
                keyboardController?.hide()
                onAddIPTChannel(
                    channelNameValue.value,
                    channelUrlValue.value
                )
            },
            onValueChange = { value ->
                channelNameValue.value = kotlin.runCatching {
                    val url = Url(value)
                    Url(value).pathSegments.lastOrNull {
                        it.isNotBlank()
                    }?.takeIf {
                        it.isNotEmpty()
                    } ?: url.host
                }.onFailure {
                    error(it)
                }.getOrDefault("")
            }
        )
        TextInputText(
            channelNameValue,
            "IPTV Source Name",
            "Enter Name",
            onImeAction = {
                keyboardController?.hide()
            },
            imeAction = ImeAction.Done
        )
        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButtonNegative(
                title = "Cancel",
                icon = Icons.Rounded.Cancel,
                onClick = {
                    onCancel()
                },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(16.dp))
            IconButtonPositive(
                title = "Add IPTV",
                icon = Icons.Rounded.Add,
                onClick = {
                    onAddIPTChannel(
                        channelNameValue.value,
                        channelUrlValue.value
                    )
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextInputText(
    textFieldValue: MutableState<String> = mutableStateOf(""),
    title: String,
    placeHolder: String,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit = {},
    imeAction: ImeAction = ImeAction.Next,
    keyboardType: KeyboardType = KeyboardType.Text,
    onImeAction: KeyboardActionScope.() -> Unit = {},
    focusRequester: FocusRequester? = null
) {
    Column(
        modifier = Modifier.padding(vertical = 8.dp)
            .then(modifier)
    ) {
        Text(
            title,
            modifier = Modifier.padding(vertical = 8.dp),
            style = MaterialTheme.typography
                .titleMedium,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
        TextField(
            textFieldValue.value,
            onValueChange = {
                textFieldValue.value = it
                onValueChange.invoke(it)
            },
            modifier = Modifier.fillMaxWidth()
                .indicatorLine(
                    false,
                    false,
                    remember { MutableInteractionSource() },
                    TextFieldDefaults.colors(),
                    0.dp,
                    0.dp
                ).then(
                    if (focusRequester != null) {
                        Modifier.focusRequester(focusRequester)
                    } else {
                        Modifier
                    }
                ),
            singleLine = true,
            colors = TextFieldDefaults.colors()
                .copy(
                    focusedIndicatorColor = Color(0x00),
                    unfocusedIndicatorColor = Color(0x00),
                    unfocusedPlaceholderColor = Color(0xFF7E8A8C),
                    focusedPlaceholderColor = Color(0xFF7E8A8C)
                ),
            placeholder = {
                Text(placeHolder)
            },
            shape = RoundedCornerShape(4.dp),
            keyboardActions = KeyboardActions(onImeAction),
            keyboardOptions = KeyboardOptions(
                autoCorrect = false,
                imeAction = imeAction,
                keyboardType = keyboardType,
                platformImeOptions = PlatformImeOptions(),
                capitalization = KeyboardCapitalization.Sentences
            ),
        )
    }
}
