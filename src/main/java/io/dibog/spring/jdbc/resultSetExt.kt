package io.dibog.spring.jdbc

import java.sql.ResultSet

/** Applies [function] on to each line of the result set and returns this list.
 * @param R the return type for each result set line
 * @param function the function which will be applied to each result row
 * @return a list will each function result applied to each result row
 */
fun <R> ResultSet.flatMap(function: (ResultSet)->R ): List<R> {
    val result = mutableListOf<R>()
    while(next()) {
        result.add( function(this) )
    }
    return result
}

/** Converts the result set to a map, where the column name is the key and the column value is the value.
 * @return a map containing the values of oen ResultSet row
 */
fun ResultSet.toResultMap(): Map<String,Any> {
    val result = mutableMapOf<String,Any>()
    for(i in 1 until metaData.columnCount) {
        result[ metaData.getColumnName(i) ] = getObject( i )
    }
    return result
}
