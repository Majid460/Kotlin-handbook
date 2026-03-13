package org.example.basic

// Kotlin solves java null pointer exception by introducing the null safety

// 1. Nullable and Non-Nullable
fun nonNull(){
    var s = "REs"
    // s = null //Null cannot be a value of a non-null type 'String'.
    s = "SDS"
    println(s)
}
fun nullAble(){
    var nullAb : String? = "Asd"
    nullAb = null
    println(nullAb)

    //How to use type safe (Safe call Elvis operator)
    val nonNull = nullAb?:"aa"
    println(nonNull)
    // Using dot operator we can safely access the nested values
    // By using kotlin scope function let
    nullAb?.let {
        println(it)
    }
}
fun main() {
    nonNull()
    nullAble()
}