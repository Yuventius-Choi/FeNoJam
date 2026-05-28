package com.keygul.fe_no_jam.service

import com.keygul.fe_no_jam.model.WeatherStn
import com.keygul.fe_no_jam.repos.WeatherStnRepos
import org.springframework.stereotype.Service

@Service
class WeatherService (
    private val weatherStnRepos: WeatherStnRepos
) {
    suspend fun getWeatherStns(): List<WeatherStn> {
        return weatherStnRepos.selectAll()
    }
}
