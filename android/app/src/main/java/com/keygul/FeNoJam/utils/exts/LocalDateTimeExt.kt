package com.keygul.FeNoJam.utils.exts

import com.keygul.FeNoJam.utils.Patterns
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * [LocalDateTime]을 [pattern]으로 포메팅합니다.
 * @param pattern 문자열 패턴
 * @return 포멧 문자열
 */
fun LocalDateTime.format(pattern: String = Patterns.YY_MM_DD): String =
    this.format(DateTimeFormatter.ofPattern(pattern))