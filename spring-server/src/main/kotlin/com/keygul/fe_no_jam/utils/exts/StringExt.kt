package com.keygul.fe_no_jam.utils.exts

import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Throws(ParseException::class)
fun String.makeIDwithDatetimeString(
    prefix: String,
    pattern: String = "yyyy-MM-dd HH:mm:ss"
): Long {
    val parsed = LocalDateTime.parse(
        this,
        DateTimeFormatter.ofPattern(pattern)
    )
    val formatted = parsed.format(
        DateTimeFormatter.ofPattern(("yyyyMMddHH"))
    )
    return "$prefix$formatted".toLong()
}

fun String.toFloatOrZero(): Float = this.ifBlank { "0" }.toFloat()
fun String.toIntOrZero(): Int = this.ifBlank { "0" }.toInt()
