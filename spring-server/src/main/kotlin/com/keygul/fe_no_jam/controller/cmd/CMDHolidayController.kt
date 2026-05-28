package com.keygul.fe_no_jam.controller.cmd

import com.keygul.fe_no_jam.model.Holiday
import com.keygul.fe_no_jam.model.response.toHolidayList
import com.keygul.fe_no_jam.service.ApiService
import com.keygul.fe_no_jam.service.HolidayService
import com.keygul.fe_no_jam.utils.BaseResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/cmd/holiday")
class CMDHolidayController (
    private val apiService: ApiService,
    private val holidayService: HolidayService
) {

    /**
     * XML 혐오증 걸려서 일단 보류
     */
//    @GetMapping
//    suspend fun getHolidays(
//        @RequestParam pageNo: Int,
//        @RequestParam numOfRows: Int,
//        @RequestParam year: String,
//        @RequestParam(required = false) month: String?
//        ): BaseResponse<List<Holiday>> {
//        val response = apiService.getHolidays(
//            year = year,
//            month = month,
//            pageNo = pageNo,
//            numOfRows = numOfRows
//        )
//        val result = holidayService.insertHolidays(response.toHolidayList())
//        return BaseResponse.ok(result)
//    }
}
