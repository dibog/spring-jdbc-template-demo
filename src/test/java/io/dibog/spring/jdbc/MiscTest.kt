package io.dibog.spring.jdbc

import assertk.assertThat
import assertk.assertions.isEqualTo
import io.bogdoll.spring.EntityRowMapper
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

@TestInstance(PER_CLASS)
class MiscTest : BaseDatabase() {

    init {
        jdbc.update("""
            INSERT INTO TEST_TABLE (NAME) VALUES
                ('A'),('B'),('C'),('D'),('E'),('F')
        """.trimIndent())
    }

    @Test
    fun `query for entities`() {

        // tag::query-for-entitiy-in[]
        val list = listOf("A","B","Z")

        val named = NamedParameterJdbcTemplate(jdbc)
        val result = named.query("SELECT ID, NAME FROM TEST_TABLE WHERE NAME in (:ids)",
                mapOf("ids" to list),
                EntityRowMapper)
        // end::query-for-entitiy-in[]

        assertThat(result.map { it.name }).isEqualTo(listOf("A","B"))
    }

    @Test
    fun `query for attribute`() {

        // tag::query-for-attribute-in[]
        val list = listOf("A","B","Z")

        val named = NamedParameterJdbcTemplate(jdbc)
        val result = named.queryForList("SELECT NAME FROM TEST_TABLE WHERE NAME in (:ids)",
                mapOf("ids" to list),
                String::class.java)
        // end::query-for-attribute-in[]

        assertThat(result).isEqualTo(listOf("A","B"))
    }

}