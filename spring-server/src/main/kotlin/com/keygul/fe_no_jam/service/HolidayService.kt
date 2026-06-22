package com.keygul.fe_no_jam.service

import com.keygul.fe_no_jam.model.Holiday
import com.keygul.fe_no_jam.repos.HolidayRepos
import com.keygul.fe_no_jam.utils.exts.parseCSV
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class HolidayService (
    private val holidayRepos: HolidayRepos
) {
    suspend fun updateHolidaysByCSV(
        files: List<MultipartFile>
    ): List<Holiday> {
        val holidays = mutableListOf<Holiday>()

        files.forEach { file ->
            val datas = file.parseCSV { row ->
                val locdate = row[3]
                val seq = row[4].toInt()
                val id = (locdate + seq).toLong()
                Holiday(
                    id = id,
                    dateKind = row[0].toInt(),
                    dateName = row[1],
                    isHoliday = row[2] == "Y",
                    locdate = locdate,
                    seq = seq,
                )
            }

            holidays.addAll(datas)
        }

        val result = holidayRepos.updateAll(holidays)
        return result
    }

    suspend fun getHolidaysByYear(year: Int): List<Holiday> {
        return holidayRepos.selectByYear(year)
    }
}
