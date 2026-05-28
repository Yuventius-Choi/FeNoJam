package com.keygul.fe_no_jam.repos

import com.keygul.fe_no_jam.model.Holiday
import org.springframework.expression.common.ExpressionUtils.toLong
import org.springframework.jdbc.core.DataClassRowMapper
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class HolidayRepos {
    private lateinit var jdbcTemplate: JdbcTemplate

    constructor(jdbcTemplate: JdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate
        createTable()
    }

    fun createTable() {
        val sql: String = """
            CREATE TABLE IF NOT EXISTS holiday (
            id BIGINT PRIMARY KEY,
            locdate VARCHAR(100),
            seq INT,
            date_kind INT,
            is_holiday BOOLEAN,
            date_name VARCHAR(100))
        """.trimIndent()
        jdbcTemplate.execute(sql)
    }

    fun updateAll(holidays: List<Holiday>): List<Holiday> {
        val sql: String = """
            INSERT INTO holiday (id, locdate, seq, date_kind, is_holiday, date_name)
            VALUES (?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
            date_kind = VALUES(date_kind),
            is_holiday = VALUES(is_holiday),
            date_name = VALUES(date_name)
        """
        jdbcTemplate.batchUpdate(sql, holidays, holidays.size) { ps, holiday ->
            ps.setLong(1, holiday.id)
            ps.setString(2, holiday.locdate)
            ps.setInt(3, holiday.seq)
            ps.setInt(4, holiday.dateKind)
            ps.setBoolean(5, holiday.isHoliday)
            ps.setString(6, holiday.dateName)
        }
        return holidays
    }

    fun selectAll(): List<Holiday> {
        val sql: String = "SELECT * FROM holiday"
        return jdbcTemplate.query(
            sql,
            DataClassRowMapper(Holiday::class.java)
        )
    }

    fun selectByYear(year: Int): List<Holiday> {
        val sql: String = "SELECT * FROM holiday WHERE locdate LIKE '$year%'"
        return jdbcTemplate.query(
            sql,
            DataClassRowMapper(Holiday::class.java)
        )
    }
}
