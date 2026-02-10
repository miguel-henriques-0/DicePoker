package com.example.chelaspokerdice.views.loginActivity

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.chelaspokerdice.DependencyContainer
import com.example.chelaspokerdice.commons.viewModelInit
import com.example.chelaspokerdice.ui.theme.ChelasPokerDiceTheme
import com.example.chelaspokerdice.views.BaseActivity
import com.example.chelaspokerdice.views.loginActivity.components.LoginComponent
import com.example.chelaspokerdice.views.homeActivity.HomeActivity
import kotlin.getValue

class LoginActivity : BaseActivity() {
    val vm by viewModels<LoginViewModel> {
        viewModelInit {
            LoginViewModel((application as DependencyContainer).userService)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChelasPokerDiceTheme {
                Scaffold(modifier = Modifier.Companion.fillMaxSize()) { innerPadding ->
                    LoginComponent(
                        vm = vm,
                        modifier = Modifier.padding(innerPadding),
                        onLogin = {
                            Log.d("LoginActivity", "Value for username is: ${vm.usernameInput}")
                            Log.d("LoginActivity", "Value for password is: ${vm.passwordInput}")
                            navigate<HomeActivity> {}
                            finish()
                        }
                    )
                }
            }
        }
    }
}