package com.example.chelaspokerdice.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PlayerSummary(
    val userId: Int,
    val balance: Int,
    val currentHand: Hand? = null,
    val hasPlayed: Boolean = false,
    val isHost: Boolean = false,
): Parcelable
