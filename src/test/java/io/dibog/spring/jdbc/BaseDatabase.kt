package io.dibog.spring.jdbc

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.springframework.jdbc.core.JdbcTemplate

open class BaseDatabase {
    protected val jdbc: JdbcTemplate = JdbcTemplate(
            HikariDataSource(
                    HikariConfig().apply {
                        jdbcUrl = "jdbc:hsqldb:mem:${this@BaseDatabase.javaClass.simpleName};SHUTDOWN"
                        username = "user"
                        password = "password"
                    }
            )
    )

    init {
        jdbc.update("""
            create table TEST_TABLE (
                ID INTEGER GENERATED BY DEFAULT AS IDENTITY (START WITH 1) PRIMARY KEY,
                NAME VARCHAR(20) NOT NULL 
            )""".trim())
    }
}