package com.example.chelaspokerdice.views.loginActivity.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.chelaspokerdice.R
import com.example.chelaspokerdice.views.loginActivity.LoginViewModel

@Composable
fun ToggleRegister(
    vm: LoginViewModel,
    modifier: Modifier
    )
{
    Column (modifier = modifier){

        Button(
            modifier =
                Modifier
                    .align(Alignment.CenterHorizontally),
            onClick = {
                if(vm.isRegistering){
                    vm.register(vm.usernameInput, vm.passwordInput, vm.inviteCodeInput)
                }
                else {
                    vm.login(vm.usernameInput, vm.passwordInput)
                }
            },
            enabled = vm.selectedPasswordInput && vm.selectedUsernameInput
        ) {
            Text(
                text = if(vm.isRegistering) stringResource(R.string.register) else stringResource(R.string.login_label)
            )
        }
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
            ,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = if(vm.isRegistering) stringResource(R.string.already_have_account) else stringResource(R.string.no_account),
                modifier = Modifier
            )
            Text(
                text = if(vm.isRegistering) stringResource(R.string.login_label) else stringResource(R.string.register),
                color = Color.Blue,
                modifier =
                    modifier
                        .padding(2.dp)
                        .clickable {
                            vm.usernameInput = ""
                            vm.passwordInput = ""
                            vm.inviteCodeInput = ""
                            vm.isRegistering = !vm.isRegistering
                        }
            )
        }
    }
}