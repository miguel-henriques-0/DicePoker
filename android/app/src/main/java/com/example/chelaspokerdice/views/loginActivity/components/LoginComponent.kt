package com.example.chelaspokerdice.views.loginActivity.components

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.chelaspokerdice.views.loginActivity.LoginViewModel
import com.example.chelaspokerdice.R
import com.example.chelaspokerdice.views.components.LoadingComponent

@Composable
fun LoginComponent(
    vm: LoginViewModel,
    modifier: Modifier,
    onLogin: () -> Unit,
) {
    val user = vm.user.collectAsState()
    if (user.value != null) {
        Log.d("NULL  ", user.value.toString())
        onLogin()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (!vm.isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Text(
                        text = stringResource(R.string.login_page_title),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp, horizontal = 16.dp)
                    )
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedTextField(
                            value = vm.usernameInput,
                            onValueChange = {
                                vm.usernameInput = it
                                vm.selectedUsernameInput = true
                                vm.emptyUsername = it.isEmpty() || it.isBlank()
                                vm.isErrorUsername = vm.emptyUsername && vm.selectedUsernameInput
                            },
                            label = { Text(stringResource(R.string.username_label)) },
                            singleLine = true,
                            isError = vm.isErrorUsername,
                            supportingText = {
                                if (vm.isErrorUsername) {
                                    vm.selectedUsernameInput = false
                                    Text(
                                        text = stringResource(R.string.login_empty_input),
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                focusedLabelColor = MaterialTheme.colorScheme.primary,
                                cursorColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = vm.passwordInput,
                            onValueChange = {
                                vm.passwordInput = it
                                vm.selectedPasswordInput = true
                                vm.emptyPassword = it.isEmpty() || it.isBlank()
                                vm.isErrorPassword = vm.emptyPassword && vm.selectedPasswordInput
                            },
                            label = { Text(stringResource(R.string.password_label)) },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            isError = vm.isErrorPassword,
                            supportingText = {
                                if (vm.isErrorPassword) {
                                    vm.selectedPasswordInput = false
                                    Text(
                                        text = stringResource(R.string.login_empty_input),
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                focusedLabelColor = MaterialTheme.colorScheme.primary,
                                cursorColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (vm.isRegistering) {
                            OutlinedTextField(
                                value = vm.inviteCodeInput,
                                onValueChange = {
                                    vm.inviteCodeInput = it
                                    vm.selectedInviteCodeInput = true
                                    vm.emptyInviteCode = it.isEmpty() || it.isBlank()
                                    vm.isErrorInviteCode = vm.emptyInviteCode && vm.selectedInviteCodeInput
                                },
                                label = { Text(stringResource(R.string.invite_code_label)) },
                                singleLine = true,
                                isError = vm.isErrorInviteCode,
                                supportingText = {
                                    if (vm.isErrorInviteCode) {
                                        vm.selectedInviteCodeInput = false
                                        Text(
                                            text = stringResource(R.string.login_empty_input),
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                                    cursorColor = MaterialTheme.colorScheme.primary
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Toggle Register Button
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(4.dp)
                        ) {
                            ToggleRegister(vm, Modifier)
                        }
                    }
                }
            }
        } else {
            LoadingComponent(modifier)
        }

        if (vm.noUser) {
            val context = LocalContext.current
            Toast.makeText(context, stringResource(R.string.no_user_found), Toast.LENGTH_LONG).show()
            vm.noUser = false
        }

        if(vm.registerError){
            val context = LocalContext.current
            Toast.makeText(context, stringResource(R.string.error_registering_user), Toast.LENGTH_LONG).show()
            vm.registerError = false
        }
    }
}
