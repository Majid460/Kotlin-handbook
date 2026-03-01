package org.example.functions


// Lambda Expression:
/*
* A lambda expression50 is an anonymous function that you can treat as a value, allowing
    it to be passed as an argument, returned from a function, or stored in a variable. Lambda
    expressions are a cornerstone of functional programming in Kotlin, enabling concise and
    expressive code for tasks such as filtering, mapping, and event handling.
    * */

val doubleN = { it: Int -> it * 2 }


fun main() {
    val numbers = listOf(1, 2, 3, 4, 5)
    val doubledNumbers = numbers.map { it * 2 }
    println(doubledNumbers) // Output: [2, 4, 6, 8, 10]
    println(doubleN(2))
}
