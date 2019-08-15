package io.dibog.spring.jdbc

import mu.KotlinLogging
import org.springframework.jdbc.core.BatchPreparedStatementSetter
import org.springframework.jdbc.core.InterruptibleBatchPreparedStatementSetter
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.ParameterDisposer
import org.springframework.jdbc.core.PreparedStatementCallback
import org.springframework.jdbc.core.PreparedStatementCreator
import org.springframework.jdbc.core.SqlProvider
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.jdbc.support.JdbcUtils
import org.springframework.util.Assert
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.SQLException
import java.sql.Statement
import java.util.*

private var logger = KotlinLogging.logger {  }

/** BatchInsert which also makes the generated keys available. Don't use it if the insert does not require to get the generated keys back.
 * @param sql the sql statement
 * @param pss lambda for feeding the prepared statements with data
 * @param keyHolder will contain the collected generated keys after the call to this method is finished
 * @return contains the number of affected rows per insert of the batch insert
 */
fun JdbcTemplate.batchInsert(sql: String, pss: BatchPreparedStatementSetter, keyHolder: GeneratedKeyHolder): IntArray {
    logger.debug {"Executing SQL batch update [$sql]" }

    val keyList = keyHolder.keyList
    keyList.clear()

    val result = execute<IntArray>(GeneratedKeysPreparedStatementCreator(sql), PreparedStatementCallback<IntArray> { ps ->
        try {
            val batchSize = pss.batchSize
            val ipss = pss as? InterruptibleBatchPreparedStatementSetter

            if (JdbcUtils.supportsBatchUpdates(ps.connection)) {
                for (i in 0 until batchSize) {
                    pss.setValues(ps, i)
                    if (ipss != null && ipss.isBatchExhausted(i)) {
                        break
                    }
                    ps.addBatch()
                }
                val result =  ps.executeBatch()
                ps.generatedKeys.let { rs ->
                    while(rs.next()) {
                        keyList.add( rs.toResultMap())
                    }
                }
                return@PreparedStatementCallback result
            } else {
                val rowsAffected = ArrayList<Int>()
                for (i in 0 until batchSize) {
                    pss.setValues(ps, i)
                    if (ipss != null && ipss.isBatchExhausted(i)) {
                        break
                    }
                    rowsAffected.add(ps.executeUpdate())
                    ps.generatedKeys.let { rs ->
                        if(rs.next() ) {
                            keyList.add( rs.toResultMap() )
                        }
                        val resultMap = rs.toResultMap()
                        val columnCount = rs.metaData
                        rs.next()
                        keyList.add(rs.toResultMap())
                    }
                }
                val rowsAffectedArray = IntArray(rowsAffected.size)
                for (i in rowsAffectedArray.indices) {
                    rowsAffectedArray[i] = rowsAffected[i]
                }
                return@PreparedStatementCallback rowsAffectedArray
            }
        } finally {
            if (pss is ParameterDisposer) {
                (pss as ParameterDisposer).cleanupParameters()
            }
        }
    })

    Assert.state(result != null, "No result array")
    return result
}

// Using special ps creator to inform the jdbc driver to return generated keys
private class GeneratedKeysPreparedStatementCreator(private val sql: String) : PreparedStatementCreator, SqlProvider {
    @Throws(SQLException::class)
    override fun createPreparedStatement(con: Connection): PreparedStatement {
        return con.prepareStatement(this.sql, Statement.RETURN_GENERATED_KEYS)
    }

    override fun getSql(): String {
        return this.sql
    }
}
