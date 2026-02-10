package com.example.chelaspokerdice.views.lobbyActivity.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.example.chelaspokerdice.R

@Composable
fun TurnTimedOutScreen(
    onContinue: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E2431))
    ) {
        Text(stringResource(R.string.timed_out), color = Color.Yellow)
        Button(
            onClick = { onContinue() }
        ) {
            Text(stringResource(R.string.continue_label))
        }
    }
}