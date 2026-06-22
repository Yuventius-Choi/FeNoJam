package com.keygul.fe_no_jam.model

data class Weather(
    val id: Long,
    val date: String,
    val stnId: Int,
    val ws: Float,
    val ta: Float,
    val hm: Float,
    val rn: Float,
    val sdTot: Float
)
