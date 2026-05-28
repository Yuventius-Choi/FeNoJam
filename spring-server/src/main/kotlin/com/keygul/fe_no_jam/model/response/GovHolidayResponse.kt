package com.keygul.fe_no_jam.model.response

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement
import com.keygul.fe_no_jam.model.Holiday

@JacksonXmlRootElement(localName = "response")
data class HolidayResponse(
    val header: Header,
    val body: Body
)

data class Header(
    val resultCode: String,
    val resultMsg: String
)

data class Body(
    val items: Items,
    val numOfRows: Int,
    val pageNo: Int,
    val totalCount: Int
)

data class Items(
    val item: List<GovHoliday> = emptyList()
)

data class GovHoliday(
    val dateKind: String,
    val dateName: String,
    val isHoliday: String,
    @JacksonXmlProperty(localName = "locdate")
    val date: String,
    val seq: Int
)

fun HolidayResponse.toHolidayList(): List<Holiday> {
    return body.items.item.map {
        Holiday(
            date = it.date,
            seq = it.seq,
            dateKind = it.dateKind,
            isHoliday = it.isHoliday == "Y",
            dateName = it.dateName
        )
    }
}
