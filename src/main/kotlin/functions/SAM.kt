package org.example.functions

// What are functional (SAM) interfaces?
/*
* Functional interfaces51, also known as Single Abstract Method (SAM) interfaces, are interfaces
that have exactly one abstract method. These interfaces are designed to represent
a single operation or function, making them an essential feature for enabling functional
programming constructs and simplifying the use of lambda expressions.*/

fun interface Greeter{
    fun greet(name:String):String
}
/*
* In this example, the Greeter functional interface defines a single method greet. A lambda
expression is used to provide the implementation directly when creating an instance of
Greeter.*/

fun interface ActionHandler{
    fun handleAction(action:String)
}
fun performAction(actionHandler: ActionHandler){
    actionHandler.handleAction("Press")
}


fun main() {
    val gre = Greeter{n -> "Hello, $n!"}
    println(gre.greet("Test"))

    performAction { s -> println("Action $s has been performed") }
}
