package com.example.chelaspokerdice.views.loginActivity

import android.util.Log
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

class LoginViewModel(
    private val service: PlayerServiceInterface
): ViewModel() {
    var usernameInput by mutableStateOf("")
    var passwordInput by mutableStateOf("")
    var emptyUsername by mutableStateOf(false)
    var emptyPassword by mutableStateOf(false)

    var emptyInviteCode by mutableStateOf(false)

    var selectedUsernameInput by mutableStateOf(false)
    var selectedPasswordInput by mutableStateOf(false
    )
    var isErrorUsername by mutableStateOf(false)
    var isErrorPassword by mutableStateOf(false)

    var isErrorInviteCode by mutableStateOf(false)

    var registerError by mutableStateOf(false)

    var isRegistering by mutableStateOf(false)

    var selectedInviteCodeInput by mutableStateOf(false)

    var inviteCodeInput by mutableStateOf("")

    var user: StateFlow<User?> = service.currentUser.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
        null
    )

    var isLoading by mutableStateOf(false)

    var noUser by mutableStateOf(false)

    fun login(username: String, password: String) {
        Log.d("Getuser", "Getting user")
        viewModelScope.launch {
            try {
                isLoading = true
                val user = service.login(username, password)
                if (user == null) {
                    noUser = true
                }
            } catch (e: Exception) {
                Log.e("LoginViewModel", "An exception occurred while fetching user:" +
                        "username -> $username -" +
                        "password -> $password - exception -> $e"
                )
            }
            finally {
                isLoading = false
            }
        }
    }

    fun register(username: String, password: String, inviteCode: String) {
        viewModelScope.launch {
            try {
                val user = service.register(username, password, inviteCode)
                if (user == null) {
                    registerError = true
                }
            } catch (e: Exception) {
                Log.e("LoginViewModel", "An exception occurred while registering user:" +
                        "username -> $username -" +
                        "password -> $password -" +
                        "inviteCode -> $inviteCode -" +
                        "exception -> $e"
                )
            }
        }
    }
}