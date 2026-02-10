package com.example.chelaspokerdice.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Hand(
    val dices: List<Dice>,
    val handRank: String? = null,
    val points: Int = 0,
): Parcelable
