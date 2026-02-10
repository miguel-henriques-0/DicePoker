package com.example.chelaspokerdice.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Dice(
    val face: String,
    val points: Int,
): Parcelable
