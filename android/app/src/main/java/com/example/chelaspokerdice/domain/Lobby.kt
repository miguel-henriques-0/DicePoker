package com.example.chelaspokerdice.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Lobby(
    val name: String,
    val description: String,
    val maxPlayers: Int,
    val rounds: Int,
    val host: User
): Parcelable