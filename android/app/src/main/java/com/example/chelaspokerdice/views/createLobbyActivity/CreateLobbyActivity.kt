package com.example.chelaspokerdice.views.createLobbyActivity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.chelaspokerdice.DependencyContainer
import com.example.chelaspokerdice.commons.viewModelInit
import com.example.chelaspokerdice.ui.theme.ChelasPokerDiceTheme
import com.example.chelaspokerdice.views.BaseActivity
import com.example.chelaspokerdice.views.lobbyActivity.LobbyActivity

class CreateLobbyActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val vm by viewModels<CreateLobbyViewModel> {
            viewModelInit {
                CreateLobbyViewModel(
                    (application as DependencyContainer).lobbyService,
                    (application as DependencyContainer).userService
                )
            }
        }

        enableEdgeToEdge()
        setContent {
            ChelasPokerDiceTheme {
                Scaffold { innerPadding ->
                    CreateLobbyScreen(
                        viewModel = vm,
                        modifier = Modifier.padding(innerPadding),
                        onCreateLobby = { game ->
                            navigate<LobbyActivity> {
                                it.putExtra("game", game)
                            }
                            // End the current activity so the user can't go back to it
                            finish()
                        }
                    )
                }
            }

        }
    }

}




// https://developer.android.com/develop/ui/compose/text/user-input?textfield=state-based
// https://www.geeksforgeeks.org/kotlin/drop-down-menu-in-android-using-jetpack-compose/
// https://stackoverflow.com/questions/68573228/how-to-show-error-message-in-outlinedtextfield-in-jetpack-compose