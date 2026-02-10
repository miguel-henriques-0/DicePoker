package com.example.chelaspokerdice.views.createLobbyActivity.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chelaspokerdice.R
import com.example.chelaspokerdice.views.createLobbyActivity.CreateLobbyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateLobby(
    vm: CreateLobbyViewModel,
    modifier: Modifier = Modifier,
    onCreateLobby: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A2E))
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF16213E)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                OutlinedTextField(
                    value = vm.lobbyName,
                    onValueChange = {
                        vm.lobbyName = it
                        vm.lobbyNameError = it.isEmpty() || it.isBlank()
                    },
                    singleLine = true,
                    isError = vm.lobbyNameError,
                    supportingText = {
                        if (vm.lobbyNameError) {
                            Text(
                                stringResource(R.string.lobby_creation_name_input_error),
                                color = Color(0xFFFF6B6B)
                            )
                        }
                    },
                    label = {
                        Text(
                            stringResource(R.string.lobby_creation_name_input_label),
                            color = Color(0xFFB0B0C3)
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF6C63FF),
                        unfocusedBorderColor = Color(0xFF3A3A5C),
                        errorBorderColor = Color(0xFFFF6B6B),
                        cursorColor = Color(0xFF6C63FF),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = Color(0xFF6C63FF)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = vm.description,
                    onValueChange = {
                        vm.description = it
                        vm.descriptionError = it.isEmpty() || it.isBlank()
                    },
                    isError = vm.descriptionError,
                    minLines = 3,
                    maxLines = 5,
                    supportingText = {
                        if (vm.descriptionError) {
                            Text(
                                stringResource(R.string.lobby_creation_description_input_error),
                                color = Color(0xFFFF6B6B)
                            )
                        }
                    },
                    label = {
                        Text(
                            stringResource(R.string.game_description_input_label),
                            color = Color(0xFFB0B0C3)
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF6C63FF),
                        unfocusedBorderColor = Color(0xFF3A3A5C),
                        errorBorderColor = Color(0xFFFF6B6B),
                        cursorColor = Color(0xFF6C63FF),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = Color(0xFF6C63FF)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF16213E)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    text = stringResource(R.string.game_settings),
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                StyledDropdownComponent(
                    label = stringResource(R.string.number_of_players_input_label),
                    expanded = vm.expandedP,
                    optionList = vm.numberOfPlayers,
                    selectedOption = vm.selectedPlayer,
                    onExpandedChange = {
                        vm.expandedP = it
                    },
                    onClick = {
                        vm.selectedPlayer = it
                        vm.expandedP = false
                    }
                )

                StyledDropdownComponent(
                    label = stringResource(R.string.number_of_rounds_input_label),
                    expanded = vm.expandedR,
                    optionList = vm.roundsOptions,
                    selectedOption = vm.selectedRounds,
                    onExpandedChange = {
                        vm.expandedR = it
                    },
                    onClick = {
                        vm.selectedRounds = it
                        vm.expandedR = false
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                val validName = vm.lobbyName.isNotEmpty() && vm.lobbyName.isNotBlank()
                val validDescription = vm.description.isNotEmpty() && vm.description.isNotBlank()

                vm.lobbyNameError = !validName
                vm.descriptionError = !validDescription

                if (validName && validDescription) {
                    Log.d(
                        "CreateLobby",
                        "creating game with name ${vm.lobbyName}, description ${vm.description}, " +
                                "number of players ${vm.selectedPlayer} and number of rounds ${vm.selectedRounds}"
                    )
                    onCreateLobby()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF6C63FF)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                stringResource(R.string.create_lobby_button),
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StyledDropdownComponent(
    label: String,
    expanded: Boolean,
    optionList: List<String>,
    selectedOption: String,
    onExpandedChange: (Boolean) -> Unit,
    onClick: (String) -> Unit
) {
    androidx.compose.material3.ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = onExpandedChange
    ) {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            readOnly = true,
            label = {
                Text(
                    text = label,
                    color = Color(0xFFB0B0C3)
                )
            },
            trailingIcon = {
                androidx.compose.material3.ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF6C63FF),
                unfocusedBorderColor = Color(0xFF3A3A5C),
                cursorColor = Color(0xFF6C63FF),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedLabelColor = Color(0xFF6C63FF),
                focusedTrailingIconColor = Color.White,
                unfocusedTrailingIconColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
            modifier = Modifier.background(Color(0xFF16213E))
        ) {
            optionList.forEach { option ->
                androidx.compose.material3.DropdownMenuItem(
                    text = {
                        Text(
                            text = option,
                            color = Color.White
                        )
                    },
                    onClick = { onClick(option) },
                    colors = androidx.compose.material3.MenuDefaults.itemColors(
                        textColor = Color.White
                    )
                )
            }
        }
    }
}
