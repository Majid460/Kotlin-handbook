package org.example.basic

import kotlin.io.path.Path

// Loops

// 1. For loop
fun main() {
    println("Closed-ended range:")
    // Closed range means the last element in range is included
    for (i in 1..6) {
        print("$i ")
        if(i==6) println()
    }
    // Closed-ended range:
    // 1 2 3 4 5 6
    println("Open ended range:")
    // Open-ended range means the last element is not included in the range
    for (i in 1..<6){
        print("$i ")
        if(i==5) println()
    }
    // 1 2 3 4 5

    println("\nReverse order in steps of 2:")
    for (i in 6 downTo 0 step 2){
        print("$i ")
        if(i==0) println()
    }
    // Loop over arrays
    val arr = arrayOf(1,2,3,4,5)
    print("The elements of array: ")
    for (i in arr.indices){
        print("${arr[i]} ")
    }
    println()
    println("Without indices::")
    for(i in arr){
        print("$i ")
    }
    // Foreach
    println()
    println("With foreach loop:")
    arr.forEach { i -> print("$i ")}

    // Foreach indexed
    println()
    println("With foreach indexed loop:")
    arr.forEachIndexed { index,i -> println("$i at index $index ")}

    // Labels, breaks, return, continue
    println("Labels......")
    loop@ for (i in 1..5){
        // Break terminates the nearest enclosing loop.
        if(i == 3) break@loop
        else print("$i ")
    }
    // 1 2
    println("After breaking the loop")
    println("Continue statement....")
    loop@ for (i in 1..5){
        // continue proceeds to the next step of the nearest enclosing loop skip the current iteration
        if(i == 3) continue@loop
        else print("$i ")
    }
    // 1 2 4 5
    println()
    println("Return..... ")
    // A qualified return allows you to return from an outer function.
    fun foo() {
        // Return with explicit return tag
        listOf(1, 2, 3, 4, 5).forEach lit@{
            if (it == 3) return@lit // local return to the caller of the lambda - the forEach loop
            print("$it ")
        }
        print(" done with explicit label")

        // Return with implicit tag
        println()
        println("Return by the implicit tag")
        listOf(1,2,3,4,5).forEach {
            if (it == 3) return@forEach
            print("$it ")
        }
    }
    foo()

}
