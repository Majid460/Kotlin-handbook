package org.example.functions

/*
* The higher-order function45 is a function that either takes another function as a parameter,
    returns a function, or both. This concept allows functions to be more flexible and expressive,
    enabling functional programming paradigms in Kotlin.
* */
// Higher Order
fun higherOrderFunction(input: Int, operation: (Int) -> Int): Int {
    return operation(input)
}

// higher order function can also return function
fun returnFun(input: Int): (String, Int) -> String {
   return when(input){
        1 -> { s: String, i: Int -> "$s + $i" }
        2 -> {s: String, i: Int -> "$s - $i"}
        else -> {_, _ -> "Null"}
    }
}

fun double(value: Int): Int {
    return value * 2
}

fun main() {
    val v = higherOrderFunction(5, ::double)
    println("Value of higherOrder:: $v")

    // Fun
   val fu =  ::returnFun
    val res = fu(1)
    println(res.invoke("S",1))


}
