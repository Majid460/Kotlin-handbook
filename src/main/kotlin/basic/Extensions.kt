package org.example.basic

/*
An Extensions52 is a way to add new functionality to existing classes without modifying
their code directly. Kotlin allows you to “extend” a class with new functions or properties
using extension functions and extension properties. This is especially useful for enhancing
classes from third-party libraries or the standard library where you don’t have access to
the source code.
* */

data class User (val name:String)

val String.Empty: String
    get() = ""

val String.Companion.Empty: String
    get() = ""

fun main() {
    val user = User(name = String.Empty)
    "Hello".Empty
}
