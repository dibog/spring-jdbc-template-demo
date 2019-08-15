package io.dibog.spring.jdbc

import java.sql.PreparedStatement
import kotlin.reflect.KClass

/** Returns a single generated key from a prepare statement.
 * @param R the type of the single generated key
 * @param name the name of generated key
 * @return the generated key value or null
 */
inline fun <reified R: Any> PreparedStatement.singleGeneratedKey(name: String): R? {
    return singleGeneratedKey(name, R::class)
}

/** Returns a single generated key from a prepare statement.
 * @param R the type of the single generated key
 * @param name the name of generated key
 * @param resultType the type of the single generated key
 * @return the generated key value or null
 */
fun <R: Any> PreparedStatement.singleGeneratedKey(name: String, resultType: KClass<R>): R? {
    val rs = generatedKeys ?: return null
    return if(rs.next()) {
        val result = rs.getObject(name) as R?
        if(rs.wasNull()) { null } else result
    }
    else {
        null as R?
    }
}

/** Returns a single generated key from a prepare statement.
 * @param R the type of the single generated key
 * @param index the index where the generated key can be found
 * @return the generated key value or null
 */
inline fun <reified R: Any> PreparedStatement.singleGeneratedKey(index: Int): R? {
    return singleGeneratedKey(index, R::class)
}

/** Returns a single generated key from a prepare statement.
 * @param R the type of the single generated key
 * @param index the index where the generated key can be found
 * @param resultType the type of the single generated key
 * @return the generated key value or null
 */
fun <R: Any> PreparedStatement.singleGeneratedKey(index: Int, resultType: KClass<R>): R? {
    val rs = generatedKeys ?: return null
    return if(rs.next()) {
        val result = rs.getObject(index) as R?
        if(rs.wasNull()) { null } else result
    }
    else {
        null as R?
    }
}

/** Returns a list of generated keys from a prepare statement.
 * @param R the type of the generated keys
 * @param name the name of the generated key
 * @return a list of generated key value or null
 */
inline fun <reified R: Any> PreparedStatement.generatedKeys(name: String): List<R?> {
    return generatedKeys(name, R::class)
}

/** Returns a list of generated keys from a prepare statement.
 * @param R the type of the generated keys
 * @param name the name of generated key
 * @param resultType the type of the generated keys
 * @return a list of generated key value or null
 */
fun <R: Any> PreparedStatement.generatedKeys(name: String, resultType: KClass<R>): List<R?> {
    val rs = generatedKeys ?: return listOf<R>()
    return rs.flatMap { rs ->
        val result = rs.getObject(name) as R
        if(rs.wasNull()) null else result
    }
}

/** Returns a list of generated keys from a prepare statement.
 * @param R the type of the generated keys
 * @param index the index where the generated key can be found
 * @return a list of generated key value or null
 */
inline fun <reified R: Any> PreparedStatement.generatedKeys(index: Int): List<R?> {
    return generatedKeys(index, R::class)
}

/** Returns a list of generated keys from a prepare statement.
 * @param R the type of the generated keys
 * @param index the index where the generated key can be found
 * @param resultType the type of the generated keys
 * @return a list of generated key value or null
 */
fun <R: Any> PreparedStatement.generatedKeys(index: Int, resultType: KClass<R>): List<R?> {
    val rs = generatedKeys ?: return listOf<R>()
    return rs.flatMap { rs ->
        val result = rs.getObject(index) as R
        if(rs.wasNull()) null else result
    }
}
