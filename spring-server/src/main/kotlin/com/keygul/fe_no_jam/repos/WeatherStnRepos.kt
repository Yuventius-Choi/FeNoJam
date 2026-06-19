package com.keygul.fe_no_jam.repos

import com.keygul.fe_no_jam.model.WeatherStn
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class WeatherStnRepos {
    private lateinit var jdbcTemplate: JdbcTemplate

    private val stnData = listOf(
        WeatherStn(93, "북춘천"),
        WeatherStn(90, "속초"),
        WeatherStn(95, "철원"),
        WeatherStn(100, "대관령"),
        WeatherStn(101, "춘천"),
        WeatherStn(104, "북강릉"),
        WeatherStn(105, "강릉"),
        WeatherStn(106, "동행"),
        WeatherStn(114, "원주"),
        WeatherStn(121, "영월"),
        WeatherStn(211, "인제"),
        WeatherStn(212, "홍천"),
        WeatherStn(214, "삼척", isDisabled = true),
        WeatherStn(217, "정선군"),
        WeatherStn(216, "태백")
    )

    constructor(jdbcTemplate: JdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate
        createTable()
        initializeValue()
    }

    suspend fun selectAll(): List<WeatherStn> {
        val sql: String = "SELECT * FROM weather_stn"
        return jdbcTemplate.query(sql) { rs, _ ->
            WeatherStn(
                id = rs.getInt("id"),
                name = rs.getString("name"),
                isDisabled = rs.getBoolean("is_disabled")
            )
        }
    }

    private fun createTable() {
        val sql: String = """
            CREATE TABLE IF NOT EXISTS weather_stn (
            id INT PRIMARY KEY,
            name VARCHAR(500) NOT NULL,
            is_disabled BOOLEAN NOT NULL DEFAULT FALSE
            )
        """.trimIndent()
        jdbcTemplate.execute(sql)
    }

    /**
     * 새로 추가하거나 기존값을 업데이트 합니다.
     */
    private fun initializeValue() {
        val sql: String = """
                INSERT INTO weather_stn (id, name, is_disabled)
                VALUES (?, ?, ?)
                ON DUPLICATE KEY UPDATE
                name = VALUES(name),
                is_disabled = VALUES(is_disabled)
            """.trimIndent()
        jdbcTemplate.batchUpdate(sql, stnData, stnData.size) {
            ps, stn ->
            ps.setInt(1, stn.id)
            ps.setString(2, stn.name)
            ps.setBoolean(3, stn.isDisabled)
        }
    }
}
