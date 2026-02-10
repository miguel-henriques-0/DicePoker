package com.example.chelaspokerdice.views.homeActivity

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chelaspokerdice.domain.User
import com.example.chelaspokerdice.services.PlayerServiceInterface
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    private val service: PlayerServiceInterface
): ViewModel()
{
    var user: StateFlow<User?> = service.currentUser.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
        null
    )

    var inviteCode: String? by mutableStateOf("")

    fun logout(){
        viewModelScope.launch {
            service.logout()
        }
    }

    fun refreshInviteCode() {
        viewModelScope.launch {
            inviteCode = service.getInviteCode()
        }
    }
}