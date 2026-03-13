package org.example.functions


// Functions are backbone of kotlin functional programming
// Functions
// 1. No param fun
fun basicFun() {
}

// With single param
fun withSingleParam(param: Int) {
}

// With multiple params
fun withMultipleParam(param1: Int, param2: String) {
}

// With return type
fun withReturnType(param: Int): Int {
    return param
}

// With lists type
fun withList(param1: List<Int>) {
    param1.forEachIndexed { i, v -> println("$i $v") }
}

// With Lambda
fun withLambda(param: (Int) -> Unit): (Int) -> Unit {
    param(1)
    return param
}

// With default values
fun withDefault(
    param1: Int = 1,
    param2: String = "Test",
    param3: Boolean = true,
    param4: () -> Unit = {},
    param5: (Int) -> Unit = { println(it) },
    param6: (x: Int) -> Unit = { println(it) },
    // param6: @Composable () -> Unit = {} // for composable
) {
    println(param1)
    println(param2)
    println(param3)
    param4()
    param5(4)
    param6(5)
}
fun main() {
    withDefault(param6 = { println(it) })
}