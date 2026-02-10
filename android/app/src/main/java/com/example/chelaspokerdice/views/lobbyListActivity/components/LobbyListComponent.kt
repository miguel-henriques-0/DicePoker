package com.example.chelaspokerdice.views.lobbyListActivity.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chelaspokerdice.R
import com.example.chelaspokerdice.domain.Game

@Composable
fun LobbyList(
    lobbyList: List<Game> = emptyList(),
    lastId: Int?,
    onLobbySelect: (Int) -> Unit,
    onScrollEnd: () -> Unit,
    onRefresh: () -> Unit
) {
    val scrollState = rememberScrollState()
    lastId == null

    if ((scrollState.value == scrollState.maxValue
                && scrollState.maxValue > 0
                && lastId != null)
    ) {
        Log.d("LobbyList", "Scrolled to the end, lastId: $lastId")
        onScrollEnd()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .background(Color(0xFF1A1A2E))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (lobbyList.isEmpty()) {
            Text(
                text = stringResource(R.string.no_lobbies_available),
                color = Color.Gray,
                fontSize = 16.sp,
                modifier = Modifier
                    .padding(32.dp)
                    .align(Alignment.CenterHorizontally)
            )
        }
        lobbyList.forEachIndexed { index, lobby ->
            LobbyCard(
                lobby = lobby,
                onClick = { onLobbySelect(index) }
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = onRefresh,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF6C63FF)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = stringResource(R.string.refresh_lobbies),
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun LobbyCard(
    lobby: Game,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF16213E)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = lobby.name,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Players: 1/${lobby.maxPlayers}",
                    color = Color(0xFFB0B0B0),
                    fontSize = 14.sp
                )
            }
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF6C63FF).copy(alpha = 0.2f)
                )
            ) {
                Text(
                    text = stringResource(R.string.join),
                    color = Color(0xFF6C63FF),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}
