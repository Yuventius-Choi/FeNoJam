package com.keygul.fe_no_jam.utils.exts

import com.keygul.fe_no_jam.exceptions.CSVException
import org.springframework.web.multipart.MultipartFile
import java.io.InputStreamReader

fun <T> MultipartFile.parseCSV(
    mapper: (List<String>) -> T
): List<T> {
    return try {
        InputStreamReader(this.inputStream, Charsets.UTF_8)
            .useLines { lines ->
            lines
                .drop(1) // -> Header 제거
                .map { line ->
                    val col = line.split(",", ignoreCase = false, limit = 100)
                    mapper(col)
                }
                .toList()
        }
    } catch (e: Exception) {
        throw CSVException("CSV 파싱 중 오류가 발생했습니다.", e)
    }
}
