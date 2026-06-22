package com.keygul.fe_no_jam.controller.apis

import com.keygul.fe_no_jam.exceptions.CSVException
import com.keygul.fe_no_jam.model.Holiday
import com.keygul.fe_no_jam.service.HolidayService
import com.keygul.fe_no_jam.utils.BaseResponse
import com.keygul.fe_no_jam.utils.BaseResponseCode
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/apis/holiday")
class HolidayController (
    private val holidayService: HolidayService
) {
    @PostMapping("/upload/csv")
    suspend fun saveHolidays(
        @RequestParam("files") files: List<MultipartFile>
    ): BaseResponse<List<Holiday>> = try {
        val result = holidayService.updateHolidaysByCSV(files)
        BaseResponse.ok(result)
    }  catch (e: CSVException) {
        e.printStackTrace()
        BaseResponse.error(BaseResponseCode.INVALID_FILE, e.localizedMessage)
    } catch (e: Exception) {
        e.printStackTrace()
        BaseResponse.error(BaseResponseCode.INTERNAL_SERVER_ERROR, e.localizedMessage)
    }

    @GetMapping()
    suspend fun getHolidays(
        @RequestParam year: Int
    ): BaseResponse<List<Holiday>> = BaseResponse.ok(
        holidayService.getHolidaysByYear(year)
    )
}
