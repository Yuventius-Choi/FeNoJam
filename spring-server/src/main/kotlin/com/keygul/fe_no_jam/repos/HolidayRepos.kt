package com.keygul.fe_no_jam.repos

import com.keygul.fe_no_jam.model.Holiday
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
            id VARCHAR(100) PRIMARY KEY,
            date VARCHAR(100),
            seq INT,
            date_kind VARCHAR(100),
            is_holiday BOOLEAN,
            date_name VARCHAR(100))
        """.trimIndent()
        jdbcTemplate.execute(sql)
    }

    fun insert(holiday: Holiday): Int {
        val sql: String = """
            INSERT INTO holiday (
            id,
            date,
            seq,
            date_kind,
            is_holiday,
            date_name)
            VALUES (
            ${holiday.date + holiday.seq},
            ${holiday.date},
            ${holiday.seq},
            ${holiday.dateKind},
            ${holiday.isHoliday},
            ${holiday.dateName})
        """.trimIndent()
        return jdbcTemplate.update(sql)
    }

    fun selectAll(): List<Holiday> {
        val sql: String = "SELECT * FROM holiday"
        return jdbcTemplate.query(
            sql,
            DataClassRowMapper(Holiday::class.java)
        )
    }

    fun updateByAPI(holiday: Holiday): Int {
        val sql: String = """
            UPDATE holiday
            SET datekind = ${holiday.dateKind},
            isholiday = ${holiday.isHoliday},
            datename = ${holiday.dateName}
            WHERE id = ${holiday.date + holiday.seq}
        """.trimIndent()

        return jdbcTemplate.update(sql)
    }
}
