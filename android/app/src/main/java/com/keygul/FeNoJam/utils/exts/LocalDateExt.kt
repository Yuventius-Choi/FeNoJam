package com.keygul.FeNoJam.utils.exts

import com.keygul.FeNoJam.utils.Patterns
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * [LocalDate]을 [pattern]으로 포메팅합니다.
 * @param pattern 문자열 패턴
 * @return 포멧 문자열
 */
fun LocalDate.format(pattern: String = Patterns.MM_DD_KR): String =
    this.format(DateTimeFormatter.ofPattern(pattern))