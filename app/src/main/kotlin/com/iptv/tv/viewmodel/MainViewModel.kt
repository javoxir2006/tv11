package com.iptv.tv.viewmodel

import androidx.lifecycle.ViewModel
import com.iptv.tv.data.model.Channel
import com.iptv.tv.data.repository.ChannelRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class UiState(
    val channels: List<Channel> = emptyList(),
    val selectedIndex: Int = 0,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

class MainViewModel : ViewModel() {

    private val repository = ChannelRepository()

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    val selectedChannel: Channel?
        get() = _uiState.value.channels.getOrNull(_uiState.value.selectedIndex)

    init {
        loadChannels()
    }

    private fun loadChannels() {
        val channels = repository.getChannels()
        _uiState.update {
            it.copy(channels = channels, isLoading = false)
        }
    }

    fun selectChannel(index: Int) {
        _uiState.update { it.copy(selectedIndex = index) }
    }

    fun selectPreviousChannel() {
        val current = _uiState.value.selectedIndex
        if (current > 0) selectChannel(current - 1)
    }

    fun selectNextChannel() {
        val current = _uiState.value.selectedIndex
        val max = _uiState.value.channels.lastIndex
        if (current < max) selectChannel(current + 1)
    }
}
