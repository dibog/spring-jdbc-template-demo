package io.dibog.spring.jdbc

import org.springframework.jdbc.support.GeneratedKeyHolder
import kotlin.reflect.KClass

/** Extracting a column with a given name from the [GeneratedKeyHolder].
 * The content of the list can be null if the column was not present.
 *
 * @param R the expected type of the column (if the actual type is different from R the value will be null)
 * @param name the name of the column you want to extract
 * @return a list containing the column
 *
 */
inline fun <reified R: Any> GeneratedKeyHolder.extract(name: String): List<R?> {
    return extract(name, R::class)
}

/** Extracting a column with a given name from the [GeneratedKeyHolder].
 * The content of the list can be null if the column was not present.
 *
 * @param name the name of the column you want to extract
 * @param resultType the expected type of the column (if the actual type is different from R the value will be null)
 * @return a list containing the column
 *
 */
fun <R: Any> GeneratedKeyHolder.extract(name: String, resultType: KClass<R>): List<R?> {
    return keyList.map { it[name] as? R }
}
