package com.keygul.fe_no_jam.model

import kotlinx.serialization.Serializable

@Serializable
data class Holiday(
    val id: Long,
    val locdate: String,
    val seq: Int,
    val dateKind: Int,
    val isHoliday: Boolean,
    val dateName: String
)
