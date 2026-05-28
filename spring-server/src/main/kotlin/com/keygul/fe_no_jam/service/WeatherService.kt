package com.keygul.fe_no_jam.service

import com.keygul.fe_no_jam.exceptions.CSVException
import com.keygul.fe_no_jam.model.Weather
import com.keygul.fe_no_jam.model.WeatherStn
import com.keygul.fe_no_jam.repos.WeatherRepos
import com.keygul.fe_no_jam.repos.WeatherStnRepos
import com.keygul.fe_no_jam.utils.exts.makeIDwithDatetimeString
import com.keygul.fe_no_jam.utils.exts.parseCSV
import com.keygul.fe_no_jam.utils.exts.toFloatOrZero
import com.keygul.fe_no_jam.utils.exts.toIntOrZero
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import kotlin.jvm.Throws

@Service
class WeatherService (
    private val weatherStnRepos: WeatherStnRepos,
    private val weatherRepos: WeatherRepos
) {
    suspend fun getWeatherStns(): List<WeatherStn> {
        return weatherStnRepos.selectAll()
    }

    @Throws(CSVException::class)
    suspend fun updateWeatherByCSV(
        multipartFile: MultipartFile
    ): List<Weather> {
        val datas = multipartFile.parseCSV { row ->
            val date = row[0]
            val stnId = row[1].toInt()

            Weather(
                id = date.makeIDwithDatetimeString("$stnId"),
                stnId = stnId,
                date = date,
                ws = row[2].toFloatOrZero(),
                ta = row[3].toFloatOrZero(),
                hm = row[4].toFloatOrZero(),
                rn = row[5].toFloatOrZero(),
                sdTot = row[6].toFloatOrZero()
            )
        }
        return weatherRepos.updateAll(datas)
    }

    @Throws(CSVException::class)
    suspend fun updateWeatherByCSVs(
        files: List<MultipartFile>
    ): Boolean {
        val weathers = mutableListOf<Weather>()

        files.forEach { file ->
            val datas = file.parseCSV { row ->
                val date = row[0]
                val stnId = row[1].toInt()

                Weather(
                    id = date.makeIDwithDatetimeString("$stnId"),
                    stnId = stnId,
                    date = date,
                    ws = row[2].toFloatOrZero(),
                    ta = row[3].toFloatOrZero(),
                    hm = row[4].toFloatOrZero(),
                    rn = row[5].toFloatOrZero(),
                    sdTot = row[6].toFloatOrZero()
                )
            }

            weathers.addAll(datas)
        }

        weatherRepos.updateAll(weathers)
        return true
    }

    suspend fun getWeathersByStnId(stnId: Int): List<Weather> {
        return weatherRepos.selectByStnId(stnId)
    }
}
