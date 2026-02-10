package com.example.chelaspokerdice.views.components

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.chelaspokerdice.R

@Composable
fun LoadingComponent(
    modifier: Modifier
) {
    Log.d("LoadingComponent", "LOADING")
    Box(
        modifier = modifier
            .fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
            ,
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(100.dp)
                ,
                color = MaterialTheme.colorScheme.secondary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Text(
                text = stringResource(R.string.login_in_text),
                modifier = Modifier.padding(22.dp)
            )
        }
    }
}


@Preview
@Composable
fun PreviewLoading() {
    LoadingComponent(
        modifier = Modifier
    )
}

// https://developer.android.com/develop/ui/compose/components/progress