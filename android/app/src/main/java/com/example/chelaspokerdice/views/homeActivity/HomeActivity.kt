package com.example.chelaspokerdice.views.homeActivity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.chelaspokerdice.DependencyContainer
import com.example.chelaspokerdice.commons.viewModelInit
import com.example.chelaspokerdice.views.statisticsActivity.StatisticsActivity
import com.example.chelaspokerdice.ui.theme.ChelasPokerDiceTheme
import com.example.chelaspokerdice.views.BaseActivity
import com.example.chelaspokerdice.views.aboutActivity.AboutActivity
import com.example.chelaspokerdice.views.homeActivity.components.HomeScreenComponent
import com.example.chelaspokerdice.views.lobbyListActivity.LobbyListActivity
import com.example.chelaspokerdice.views.loginActivity.LoginActivity
import kotlin.getValue

class HomeActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        val vm by viewModels<HomeViewModel> {
            viewModelInit {
                HomeViewModel((application as DependencyContainer).userService)
            }
        }

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChelasPokerDiceTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    HomeScreenComponent(
                        modifier = Modifier.padding(innerPadding),
                        vm = vm,
                        onAbout = { navigate<AboutActivity>() },
                        onStatistics = { navigate<StatisticsActivity>() },
                        onLobbyList = {  navigate<LobbyListActivity>() }, // Navigate with data since we need to pass the user
                        onLogout = {
                            vm.logout()
                            navigate<LoginActivity>()
                        },
                        onRefreshInviteCode = {
                            vm.refreshInviteCode()
                        }
                    )
                }
            }
        }
    }
}
