package org.example.collections

// Read only collections can't be modified once it has been created
/*
* They provide methods to access and
    query data but do not include methods for modification. Kotlin ensures that read-only
    collections maintain data integrity.
    The primary read-only collection types are:
        • List: Represents an ordered collection of elements. Elements can be accessed by their
index.
* */

fun readOnlyCollections() {
    // List - Represents an ordered collection of elements. Elements can be accessed by their index.
    val readOnlyList = listOf("ABC", "kotlin", "developer")
    println("-------List---------")
    println(readOnlyList[0]) // Output: ABC
    readOnlyList.forEach { s ->
        println(s.uppercase())
    }
    // Map - Represents a collection of key-value pairs. Each key is unique.
    println("-------Map---------")
    val readOnlyMap = mapOf("A" to "ACG", "B" to "Java","C" to "Developer")
    readOnlyMap.forEach { (key, value) ->
        println("$key -> $value")
    }
    // Set - Represents a collection of unique elements. It does not allow duplicate values.
    val readOnlySet = setOf("A","A","b","B")
    println("-------Set---------")
    readOnlySet.forEach { s ->
        println(s)
    } // A,b,B

}


fun main() {
    readOnlyCollections()
}
