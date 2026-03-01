package org.example.collections

/*
*   Mutable collections allow modifications such as adding, removing, or updating elements.
    These collections extend the corresponding read-only interfaces and provide additional
    methods for mutability.
* */

fun mutableCollections() {

    // List - mutableListOf()
    println("-------List---------")
    val mutableList = mutableListOf<String>()
    // Add elements in the list
    mutableList.add("First")
    mutableList.add("Second")
    // Add a whole list
    val readOnlyList = listOf<String>("A", "B")
    mutableList.addAll(readOnlyList)
    mutableList.sort()
    mutableList.forEach { s ->
        if ((s == "A") or (s == "B")) println("`$s` belongs to list readOnlyList")
        else println("`$s` belongs to mutable list")
    }


    // Map - mutableMapOf()
    println("-------Map---------")
    val mutableMap = mutableMapOf<String, String>()
    mutableMap.put("A", "First")
    mutableMap.put("B", "Second")
    // Add a whole read-only map in the existing map
    val readOnlyMap = mapOf<String, String>("C" to "Third", "D" to "Forth")
    mutableMap.putAll(readOnlyMap)
    mutableMap.forEach { (key, value) ->
        if (key.contentEquals("A") or key.contentEquals("B")) println("`$key` belongs to mutable map with value: `$value`")
        else println("`$key` belongs to read-only map with value:`$value`")
    }


    // Set - mutableSetOf()
    println("-------Set---------")
    val mutableSet = mutableSetOf<String>("A", "V", "A", "B")
    mutableSet.add("C")
    mutableSet.add("D")
    // Add a whole read-only set in the mutable set
    val readOnlySet = setOf<String>("E", "F", "G")
    mutableSet.addAll(readOnlySet)
    mutableSet
        .stream()
        .sorted()
        .distinct()
        .forEach { s -> print("$s ") }
}

fun main() {
    mutableCollections()
}
