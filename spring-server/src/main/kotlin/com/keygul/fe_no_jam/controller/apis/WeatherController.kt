package com.keygul.fe_no_jam.controller.apis

import com.keygul.fe_no_jam.model.WeatherStn
import com.keygul.fe_no_jam.service.WeatherService
import com.keygul.fe_no_jam.utils.BaseResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/apis/weather")
class WeatherController (
    private val weatherService: WeatherService
) {
    init {
        CoroutineScope(Dispatchers.IO).launch {
            getWeatherStns()
        }
    }

    @GetMapping("/stn")
    suspend fun getWeatherStns(): BaseResponse<List<WeatherStn>> {
        val result = weatherService.getWeatherStns()
        return BaseResponse.ok(result)
    }

    @PostMapping("/upload")
    fun upload() {

    }
}
