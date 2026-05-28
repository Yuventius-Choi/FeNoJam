package com.keygul.fe_no_jam.service

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.keygul.fe_no_jam.model.Holiday
import com.keygul.fe_no_jam.model.response.HolidayResponse
import com.keygul.fe_no_jam.utils.Apis
import com.keygul.fe_no_jam.utils.Envs
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.reactive.function.client.bodyToMono
import org.springframework.web.reactive.function.server.RequestPredicates.queryParam
import tools.jackson.module.kotlin.KotlinModule

@Service
class ApiService (
    private val webClient: WebClient
) {
    suspend fun getHolidays(
        year: String = "2026",
        month: String? = null,
        pageNo: Int = 1,
        numOfRows: Int = 100
    ): HolidayResponse {

        val xml = webClient.get()
            .uri { builder ->
                builder
                    .scheme("https")
                    .host("apis.data.go.kr")
                    .path("/B090041/openapi/service/SpcdeInfoService/getHoliDeInfo")
                    .queryParam("ServiceKey", System.getenv(Envs.GOV_DECODED))
                    .queryParam("pageNo", pageNo)
                    .queryParam("numOfRows", numOfRows)
                    .queryParam("solYear", year)

                month?.let {
                    builder.queryParam("solMonth", it)
                }

                builder.build()
            }
            .accept(MediaType.APPLICATION_XML)
            .retrieve()
            .bodyToMono<String>()
            .awaitSingle()

        val xmlMapper = XmlMapper().apply {
            registerKotlinModule()
        }

        return xmlMapper.readValue(xml, HolidayResponse::class.java)
    }
}
