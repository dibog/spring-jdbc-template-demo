package io.bogdoll.spring

import assertk.assertThat
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import io.dibog.spring.jdbc.BaseDatabaseTest
import io.dibog.spring.jdbc.batchInsert
import io.dibog.spring.jdbc.extract
import io.dibog.spring.jdbc.generatedKeys
import io.dibog.spring.jdbc.singleGeneratedKey
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.springframework.jdbc.core.BatchPreparedStatementSetter
import org.springframework.jdbc.core.ConnectionCallback
import org.springframework.jdbc.core.PreparedStatementCreator
import org.springframework.jdbc.support.GeneratedKeyHolder
import java.sql.PreparedStatement
import java.sql.Statement

private const val SQL_INSERT_ENTITY = "INSERT INTO TEST_TABLE (NAME) VALUES (?)"

@DisplayName("Using Spring JdbcTemplate for inserting entities into table with generated IDs")
@TestInstance(PER_CLASS)
class InsertingEntitiesWithGeneratedIDsTest : BaseDatabaseTest() {

    @DisplayName("and inserting exactly one new entry")
    @Nested
    inner class InsertingOneNewEntity {

        @Test
        fun `using the plain jdbc way`() {
            val id = jdbc.execute(ConnectionCallback<Number>(){ conn ->
                val ps = conn.prepareStatement( SQL_INSERT_ENTITY, Statement.RETURN_GENERATED_KEYS )
                ps.setString(1, "Z")
                val nb = ps.executeUpdate()
                assert(nb==1) { "Expected to have inserted one entity, but it were $nb" }
                ps.singleGeneratedKey<Number>("ID")
            })

            assertThat(id).isNotNull().isInstanceOf(Number::class)
        }

        @Test
        fun `using the spring jdbc way wrongly`() {
            val keyHolder = GeneratedKeyHolder()
            val nb = jdbc.update(
                    PreparedStatementCreator { conn->
                        conn.prepareStatement(SQL_INSERT_ENTITY).apply {
                            setString(1, "Z")
                        }
                    },
                    keyHolder
            )

            assert(nb==1) { "Expected to have inserted one entity, but it were $nb" }
            val id = keyHolder.key

            assertThat(id).isNull()
        }

        @Test
        fun `using the spring jdbc way correctly`() {
            val keyHolder = GeneratedKeyHolder()
            val nb = jdbc.update(
                    PreparedStatementCreator { conn->
                        conn.prepareStatement(SQL_INSERT_ENTITY, Statement.RETURN_GENERATED_KEYS).apply {
                            setString(1, "Z")
                        }
                    },
                    keyHolder
            )

            assert(nb==1) { "Expected to have inserted one entity, but it were $nb" }
            val id = keyHolder.key

            assertThat(id).isNotNull().isInstanceOf(Number::class)
        }
    }

    @DisplayName("and inserting many new entries")
    @Nested
    inner class InsertingManyNewEntities {

        @Test
        fun `using the plain jdbc way`() {
            val id = jdbc.execute(ConnectionCallback<List<Number?>>(){ conn ->
                val ps = conn.prepareStatement( SQL_INSERT_ENTITY, Statement.RETURN_GENERATED_KEYS )

                ps.setString(1, "Z")
                ps.addBatch()

                ps.setString(1, "Y")
                ps.addBatch()

                val nb = ps.executeBatch()
                assert(nb.sumBy { it }==2) { "Expected to have inserted two entities, but it were $nb" }
                ps.generatedKeys<Number>("ID")
            })

            assertThat(id).isNotNull()
            assertThat( id.all { it!=null } )
        }

        @Test
        fun `using the spring jdbc way with an extension method`() {
            val keyHolder = GeneratedKeyHolder()
            val names = listOf("Z","Y")
            val inserts = jdbc.batchInsert(
                    SQL_INSERT_ENTITY,
                    object: BatchPreparedStatementSetter{
                        private val iter = names.iterator()
                        override fun getBatchSize() = names.size
                        override fun setValues(ps: PreparedStatement, i: Int) {
                            val name = iter.next()
                            ps.setString(1, name)
                        }
                    },
                    keyHolder
            )

            assertThat(inserts).isNotNull()
            assertThat(inserts.sumBy { it }==2)

            val keys = keyHolder.extract<Number>("ID")
            assertThat(inserts.all { it!=null })
        }
    }

}