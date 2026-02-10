package com.example.chelaspokerdice.views.lobbyListActivity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.ExperimentalMaterial3Api
import com.example.chelaspokerdice.DependencyContainer
import com.example.chelaspokerdice.commons.viewModelInit
import com.example.chelaspokerdice.views.BaseActivity
import com.example.chelaspokerdice.views.createLobbyActivity.CreateLobbyActivity
import com.example.chelaspokerdice.views.lobbyActivity.LobbyActivity

class LobbyListActivity : BaseActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val vm by viewModels<LobbyListViewModel> {
            viewModelInit {
                LobbyListViewModel(
                    (application as DependencyContainer).lobbyService,
                    (application as DependencyContainer).userService
                )
            }
        }

        enableEdgeToEdge()
        setContent {
            LobbyListScreen(
                viewModel = vm,
                onCreateLobby = {
                    navigate<CreateLobbyActivity>()
                },
                onLobbySelection = { lobby ->
                    // Join lobby if not joined yet else just open lobby activity
                    navigate<LobbyActivity> { intent ->
                        vm.joinGame(lobby.id)
                        intent.putExtra("game", lobby)
                    }
                })
        }

    }
}

// Scrollable list guide
// https://medium.com/@mehtabhumika/efficient-scrollable-list-in-jetpack-compose-27619900be9e

// Top Bar
// https://developer.android.com/develop/ui/compose/components/app-bars?hl=pt-br