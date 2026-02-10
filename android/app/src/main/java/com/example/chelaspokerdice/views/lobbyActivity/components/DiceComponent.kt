package com.example.chelaspokerdice.views.lobbyActivity.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun Dice(
    face: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(12.dp)
    val borderModifier = if (isSelected) {
        Modifier.border(3.dp, Color(0xFFFFAA00), shape)
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .size(60.dp)
            .clip(shape)
            .then(borderModifier)
            .background(Color(0xFF2D2D2D), shape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = selectFace(face),
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

fun selectFace(face: String): String {
    return when (face) {
        "ACE" -> "A"
        "KING" -> "K"
        "QUEEN" -> "Q"
        "JACK" -> "J"
        "TEN" -> "10"
        "NINE" -> "9"
        else -> "?"
    }
}