package io.bogdoll.spring

import assertk.assertThat
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import io.dibog.spring.jdbc.BaseDatabase
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
class InsertingEntitiesWithGeneratedIDsTest : BaseDatabase() {

    @DisplayName("and inserting exactly one new entry")
    @Nested
    inner class InsertingOneNewEntity {

        @Test
        fun `using the plain jdbc way`() {
            // tag::inserting-single-entity-jdbc[]
            val id = jdbc.execute(ConnectionCallback<Number>(){ conn ->                 // <1>
                val ps = conn.prepareStatement(                                         // <2>
                        SQL_INSERT_ENTITY,
                        Statement.RETURN_GENERATED_KEYS )
                ps.setString(1, "Z")                                                    // <3>
                val nb = ps.executeUpdate()                                             // <4>

                assert(nb==1) {
                    "Expected to have inserted one entity, but it were $nb"
                }

                ps.singleGeneratedKey<Number>("ID")                                     // <5>
            })
            // end::inserting-single-entity-jdbc[]

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

            assert(nb==1) {
                "Expected to have inserted one entity, but it were $nb"
            }

            val id = keyHolder.key

            assertThat(id).isNull()
        }

        @Test
        fun `using the spring jdbc way correctly`() {
            // tag::inserting-single-entity-spring[]
            val keyHolder = GeneratedKeyHolder()                                        // <1>
            val nb = jdbc.update(
                    PreparedStatementCreator { conn->                                   // <2>
                        conn.prepareStatement(
                                SQL_INSERT_ENTITY,
                                Statement.RETURN_GENERATED_KEYS                         // <3>
                        ).apply {
                            setString(1, "Z")                                           // <4>
                        }
                    },
                    keyHolder                                                           // <5>
            )

            assert(nb==1) {
                "Expected to have inserted one entity, but it were $nb"
            }
            val id = keyHolder.key                                                      // <6>
            // end::inserting-single-entity-spring[]

            assertThat(id).isNotNull().isInstanceOf(Number::class)
        }
    }

    @DisplayName("and inserting many new entries")
    @Nested
    inner class InsertingManyNewEntities {

        @Test
        fun `using the plain jdbc way`() {
            // tag::inserting-multiple-entity-plain[]
            val id = jdbc.execute(
                    ConnectionCallback<List<Number?>>(){ conn ->                        // <1>
                val ps = conn.prepareStatement(
                        SQL_INSERT_ENTITY, Statement.RETURN_GENERATED_KEYS )            // <2>

                ps.setString(1, "Z")                                                    // <3>
                ps.addBatch()

                ps.setString(1, "Y")                                                    // <3>
                ps.addBatch()

                val nb = ps.executeBatch()                                              // <4>
                assert(nb.sumBy { it }==2) {
                    "Expected to have inserted two entities, but it were $nb"
                }

                ps.generatedKeys<Number>("ID")                                          // <5>
            })
            // end::inserting-multiple-entity-plain[]

            assertThat(id).isNotNull()
            assertThat( id.all { it!=null } )
        }

        @Test
        fun `using the spring jdbc way with an extension method`() {
            // tag::inserting-multiple-entity-spring[]
            val keyHolder = GeneratedKeyHolder()                                        // <1>
            val names = listOf("Z","Y")
            val inserts = jdbc.batchInsert(                                             // <2>
                    SQL_INSERT_ENTITY,
                    object: BatchPreparedStatementSetter{                               // <3>
                        private val iter = names.iterator()
                        override fun getBatchSize() = names.size
                        override fun setValues(ps: PreparedStatement, i: Int) {
                            val name = iter.next()
                            ps.setString(1, name)
                        }
                    },
                    keyHolder                                                           // <4>
            )

            assertThat(inserts).isNotNull()
            assertThat(inserts.sumBy { it }==2)

            val keys = keyHolder.extract<Number>("ID")                                  // <5>
            // end::inserting-multiple-entity-spring[]

            assertThat(inserts.all { it!=null })
        }
    }
}