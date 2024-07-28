package tv.iptv.tun.tviptv.ui.screens.welcome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import tv.iptv.tun.tviptv.storage.IKeyValueStorage
import tv.iptv.tun.tviptv.storage.KeyValueStorageForTesting
import tv.iptv.tun.tviptv.storage.isFirstOpenApp
import tv.iptv.tun.tviptv.storage.isOnboardingSuccess
import tv.iptv.tun.tviptv.storage.isPrivacyAccepted
import tv.iptv.tun.tviptv.storage.setFistOpened
import tv.iptv.tun.tviptv.storage.setOnBoardingSuccess
import tv.iptv.tun.tviptv.storage.setPrivacyAccepted

class WelcomeViewModel(
    private val keyValueStorage: IKeyValueStorage = KeyValueStorageForTesting()
) : ViewModel() {
    private val _uiState: MutableStateFlow<UIWelcomeData> by lazy {
        MutableStateFlow(UIWelcomeData.None)
    }

    val uiState: StateFlow<UIWelcomeData>
        get() = _uiState

    init {
        when {
            keyValueStorage.isFirstOpenApp() -> delayUIValueIfNeeded(UIWelcomeData.Begin)
//            !keyValueStorage.isOnboardingSuccess() -> delayUIValueIfNeeded(UIWelcomeData.Onboard)
            !keyValueStorage.isPrivacyAccepted() -> delayUIValueIfNeeded(UIWelcomeData.Privacy)
            else -> delayUIValueIfNeeded(UIWelcomeData.Done)
        }
    }

    private fun delayUIValueIfNeeded(value: UIWelcomeData) {
        if (value == UIWelcomeData.Begin) {
            _uiState.value = value
        } else {
            viewModelScope.launch(Dispatchers.Main) {
                _uiState.value = value
            }
        }
    }

    fun markDone() {
        keyValueStorage.setPrivacyAccepted(true)
        _uiState.value = UIWelcomeData.Done
    }

    fun goToPrivacy() {
        keyValueStorage.setOnBoardingSuccess(true)
        _uiState.value = UIWelcomeData.Privacy
    }

    fun goToOnboard() {
        keyValueStorage.setFistOpened(false)
        _uiState.value = UIWelcomeData.Onboard
    }
}

sealed interface UIWelcomeData {
    data object None : UIWelcomeData
    data object Done : UIWelcomeData
    data object Begin : UIWelcomeData
    data object Onboard : UIWelcomeData
    data object Privacy : UIWelcomeData
}