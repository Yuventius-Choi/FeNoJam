package com.keygul.fe_no_jam.service

import com.keygul.fe_no_jam.model.Holiday
import com.keygul.fe_no_jam.repos.HolidayRepos
import org.springframework.stereotype.Service

@Service
class HolidayService (
    private val holidayRepos: HolidayRepos
) {
    suspend fun insertHolidays(
        holidays: List<Holiday>
    ): List<Holiday> {
        var result = true
        for (holiday in holidays) {
            result = result && holidayRepos.insert(holiday) as Boolean
        }

        if (result) {
            println("Success!")
        } else {
            println("Failed!")
        }

        return holidayRepos.selectAll()
    }
}
