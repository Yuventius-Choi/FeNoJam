package com.keygul.fe_no_jam.controller.apis

import com.keygul.fe_no_jam.exceptions.CSVException
import com.keygul.fe_no_jam.model.Weather
import com.keygul.fe_no_jam.model.WeatherStn
import com.keygul.fe_no_jam.service.WeatherService
import com.keygul.fe_no_jam.utils.BaseResponse
import com.keygul.fe_no_jam.utils.BaseResponseCode
import jakarta.websocket.server.PathParam
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.io.File

@RestController
@RequestMapping("/apis/weather")
class WeatherController (
    private val weatherService: WeatherService
) {

    @GetMapping("/stn")
    suspend fun getWeatherStns(): BaseResponse<List<WeatherStn>> {
        val result = weatherService.getWeatherStns()
        return BaseResponse.ok(result)
    }

    @PostMapping("/upload/csv")
    suspend fun saveWeatherByCSV(
        @RequestPart("file") file: MultipartFile
    ): BaseResponse<List<Weather>> {
        return try {
            val result = weatherService.updateWeatherByCSV(file)
            BaseResponse.ok(result)
        } catch (e: CSVException) {
            e.printStackTrace()
            BaseResponse.error(BaseResponseCode.INVALID_FILE, e.localizedMessage)
        } catch (e: Exception) {
            e.printStackTrace()
            BaseResponse.error(BaseResponseCode.INTERNAL_SERVER_ERROR, e.localizedMessage)
        }
    }

    @PostMapping("/upload/csvs")
    suspend fun saveWeatherByCSVs(
        @RequestParam("files") files: List<MultipartFile>
    ): BaseResponse<Boolean> {
        return try {
            val result = weatherService.updateWeatherByCSVs(files)
            BaseResponse.ok(result)
        } catch (e: CSVException) {
            e.printStackTrace()
            BaseResponse.error(BaseResponseCode.INVALID_FILE, e.localizedMessage)
        } catch (e: Exception) {
            e.printStackTrace()
            BaseResponse.error(BaseResponseCode.INTERNAL_SERVER_ERROR, e.localizedMessage)
        }
    }

    @GetMapping("/stn/{id}")
    suspend fun getWeathersByStnId(
        @PathVariable("id") stnId: Int
    ): BaseResponse<List<Weather>> {
        val result = weatherService.getWeathersByStnId(stnId)
        return BaseResponse.ok(result)
    }
}
