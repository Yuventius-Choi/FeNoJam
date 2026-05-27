package com.keygul.fe_no_jam.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Holiday(
    val id: String? = null,
    val date: String,
    val seq: Int,
    val dateKind: String,
    val isHoliday: Boolean,
    val dateName: String
)
