package com.keygul.fe_no_jam.repos

import com.keygul.fe_no_jam.model.Weather
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.query
import org.springframework.jdbc.core.queryForList
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import kotlin.collections.emptyList

@Repository
class WeatherRepos {
    private lateinit var jdbcTemplate: JdbcTemplate

    private val weatherRowMapper = RowMapper { rs, rowNum ->
        Weather(
            id = rs.getLong("id"),
            date = rs.getString("datetime"),
            stnId = rs.getInt("stn_id"),
            ws = rs.getFloat("ws"),
            ta = rs.getFloat("ta"),
            hm = rs.getFloat("hm"),
            rn = rs.getFloat("rn"),
            sdTot = rs.getFloat("sd_tot"),
        )
    }

    constructor(jdbcTemplate: JdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate
        createTable()
    }

    private fun createTable() {
        val sql: String = """
            CREATE TABLE IF NOT EXISTS weather (
            id BIGINT PRIMARY KEY,
            datetime VARCHAR(500),
            stn_id INT NOT NULL,
            ws FLOAT DEFAULT 0.0,
            ta FLOAT DEFAULT 0.0,
            hm FLOAT DEFAULT 0.0,
            rn FLOAT DEFAULT 0.0,
            sd_tot FLOAT DEFAULT 0.0,
            
            INDEX idx_weather_stn_id (stn_id),
            
            CONSTRAINT fk_weather_stn
                FOREIGN KEY (stn_id)
                REFERENCES weather_stn (id)
            )
        """.trimIndent()
        jdbcTemplate.execute(sql)
    }

    fun selectAll(): List<Weather> {
        val sql: String = "SELECT * FROM weather"
        return jdbcTemplate.query(sql, weatherRowMapper)
    }

    fun selectByStnId(stnId: Int): List<Weather> {
        val sql: String = "SELECT * FROM weather WHERE stn_id = ?"
        return jdbcTemplate.query(sql, weatherRowMapper, stnId)
    }

    fun updateAll(weathers: List<Weather>): List<Weather> {
        val sql: String = """
            INSERT INTO weather (id, datetime, stn_id, ws, ta, hm, rn, sd_tot)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
            ws = VALUES(ws),
            ta = VALUES(ta),
            hm = VALUES(hm),
            rn = VALUES(rn),
            sd_tot = VALUES(sd_tot)
        """.trimIndent()
        jdbcTemplate.batchUpdate(sql, weathers, weathers.size) {
                ps, weather ->
            ps.setLong(1, weather.id)
            ps.setString(2, weather.date)
            ps.setInt(3, weather.stnId)
            ps.setFloat(4, weather.ws)
            ps.setFloat(5, weather.ta)
            ps.setFloat(6, weather.hm)
            ps.setFloat(7, weather.rn)
            ps.setFloat(8, weather.sdTot)
        }

        return weathers
    }
}
