package io.bogdoll.spring

import assertk.all
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isFailure
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import io.dibog.spring.jdbc.BaseDatabase
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.RowMapper
import java.sql.ResultSet

const val SQL_QUERY_SINGLE_ENTITY = "SELECT ID, NAME FROM TEST_TABLE WHERE NAME=?"
const val SQL_QUERY_MANY_ENTITIES = "SELECT ID, NAME FROM TEST_TABLE"

const val SQL_QUERY_ATTRIBUTE_OF_SINGLE_ENTITY = "SELECT NAME FROM TEST_TABLE WHERE NAME=?"
const val SQL_QUERY_ATTRIBUTE_OF_MANY_ENTITIES = "SELECT NAME FROM TEST_TABLE"


data class NameEntity(val id: Int, val name: String)
object EntityRowMapper : RowMapper<NameEntity> {
    override fun mapRow(rs: ResultSet, rowNum: Int): NameEntity {
        return NameEntity(
                rs.getInt("ID"),
                rs.getString("NAME")
        )
    }
}

@DisplayName("Using Spring JdbcTemplate for querying")
@TestInstance(PER_CLASS)
class QueryTest : BaseDatabase() {

    init {
        jdbc.update("""
            INSERT INTO TEST_TABLE (NAME) VALUES
                ('A'),('B'),('C'),('D'),('E'),('F')
        """.trimIndent())
    }

    @DisplayName("a single entity")
    @Nested
    inner class QuerySingleEntity {
        @Test
        fun `which should definitely exist (and it does)`() {
            // tag::query-single-entity[]
            val name = jdbc.queryForObject(                                             // <1>
                    SQL_QUERY_SINGLE_ENTITY,                                            // <2>
                    arrayOf("A"),                                                       // <3>
                    EntityRowMapper                                                     // <4>
            )
            // end::query-single-entity[]

            assertThat(name).isNotNull()
            assertThat(name.name).isEqualTo("A")
        }

        @Test
        fun `which should definitely exist (and it does not)`() {
            assertThat {
                jdbc.queryForObject(SQL_QUERY_SINGLE_ENTITY, arrayOf("Z"), EntityRowMapper)
            }.isFailure()
                    .isInstanceOf(EmptyResultDataAccessException::class)
        }

        @Test
        fun `where you are not sure if it exists (and it does)`() {
            val result = jdbc.query(SQL_QUERY_SINGLE_ENTITY, arrayOf("A"), EntityRowMapper)

            assertThat(result).hasSize(1)
            assertThat(result[0].name).isEqualTo("A")
        }

        @Test
        fun `where you are not sure if it exists (and it does not)`() {
            val result = jdbc.query(SQL_QUERY_SINGLE_ENTITY, arrayOf("Z"), EntityRowMapper)

            assertThat(result).hasSize(0)
        }
    }

    @DisplayName("many entities")
    @Nested
    inner class QueryManyEntities {
        @Test
        fun doAndTestIt() {
            // tag::query-multiple-entity[]
            val result = jdbc.query(                                                    // <1>
                    SQL_QUERY_MANY_ENTITIES,                                            // <2>
                    EntityRowMapper                                                     // <3>
            )
            // end::query-multiple-entity[]
            assertThat(result).hasSize(6)
        }
    }

    @DisplayName("attributes of entities")
    @Nested
    inner class QueryAttributes {
        @Test
        fun `select a single attribute`() {
            // tag::query-single-attribute[]
            val result = jdbc.queryForList(                                             // <1>
                    SQL_QUERY_ATTRIBUTE_OF_MANY_ENTITIES,                               // <2>
                    String::class.java                                                  // <3>
            )
            // end::query-single-attribute[]
            assertThat(result).all {
                hasSize(6)
                isEqualTo(listOf("A","B","C","D","E","F"))
            }
        }

        @Test
        fun `select all attributes`() {
            // tag::query-multiple-attribute[]
            val result = jdbc.queryForList(                                             // <1>
                    SQL_QUERY_MANY_ENTITIES                                             // <2>
            )
            // end::query-multiple-attribute[]
            assertThat(result).all {
                hasSize(6)
                isEqualTo(listOf(
                        mapOf("ID" to 1, "NAME" to "A"),
                        mapOf("ID" to 2, "NAME" to "B"),
                        mapOf("ID" to 3, "NAME" to "C"),
                        mapOf("ID" to 4, "NAME" to "D"),
                        mapOf("ID" to 5, "NAME" to "E"),
                        mapOf("ID" to 6, "NAME" to "F")
                ))
            }
        }
    }
}
