package com.example.chelaspokerdice.views.lobbyActivity.components

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chelaspokerdice.R
import com.example.chelaspokerdice.views.lobbyActivity.LobbyActivityViewModel

@Composable
fun WaitingForAnteScreen(
    modifier: Modifier,
    vm: LobbyActivityViewModel,
    onAntePlace: (Int) -> Unit,
) {
    val user = vm.user.collectAsState().value
    val game = vm.game

    val playerBalance = game?.gameState?.playerList
        ?.find { it.userId == user?.id }?.balance ?: 0

    val userHasNotPlacedBet = game?.gameState?.playersPaidAnte?.contains(user?.id)?.not() ?: true
    var betAmount by rememberSaveable { mutableStateOf("") }
    var isError by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A2E))
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.place_your_ante),
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF16213E)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.your_balance),
                    color = Color(0xFF6C63FF),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = stringResource(R.string.available), color = Color(0xFFB0B0B0), fontSize = 14.sp)
                    Text(
                        text = "$playerBalance",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF16213E)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.ante_amount),
                    color = Color(0xFF6C63FF),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )

                OutlinedTextField(
                    value = betAmount,
                    onValueChange = { newValue ->
                        betAmount = newValue
                        val amount = newValue.toIntOrNull()
                        isError = amount == null || amount <= 0 || amount > playerBalance
                    },
                    label = { Text(stringResource(R.string.enter_amount)) },
                    isError = isError,
                    supportingText = {
                        if (isError) {
                            Text(stringResource(R.string.enter_a_valid_amount) + "(1 - $playerBalance)", color = Color(0xFFFF6B6B))
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = userHasNotPlacedBet,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF6C63FF),
                        unfocusedBorderColor = Color(0xFF3A3A5C),
                        focusedLabelColor = Color(0xFF6C63FF),
                        unfocusedLabelColor = Color(0xFFB0B0B0),
                        cursorColor = Color(0xFF6C63FF),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        disabledTextColor = Color(0xFFB0B0B0),
                        disabledBorderColor = Color(0xFF3A3A5C),
                        disabledLabelColor = Color(0xFFB0B0B0)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (!userHasNotPlacedBet) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF6C63FF).copy(alpha = 0.2f)
                )
            ) {
                Text(
                    text = stringResource(R.string.ante_placed),
                    color = Color(0xFF6C63FF),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            Text(
                text = stringResource(R.string.place_your_ante_to_continue),
                color = Color(0xFFB0B0B0),
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { onAntePlace(betAmount.toInt()) },
            enabled = !isError && betAmount.isNotEmpty() && userHasNotPlacedBet,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF6C63FF),
                disabledContainerColor = Color(0xFF3A3A5C)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = stringResource(R.string.place_your_ante),
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
}
