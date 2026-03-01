package org.example.dsl

import kotlin.apply

/*
    * A DSL (Domain-Specific Language) (a part of Type-safe builders54) is a programming style
    that allows you to create custom, fluent APIs tailored to a specific problem domain, making
    the code more readable and expressive. Kotlin’s features, such as extension functions,
    lambdas with receivers, and default parameter values, make it an ideal language for creating
    DSLs. DSLs in Kotlin are often used to create declarative syntax for configuration, UI layouts,
    object creation, and more.
    How Kotlin DSL Works
    Kotlin DSLs use several language features to make syntax expressive:
    • Extension Functions: Enable you to add new functions to existing classes without
      modifying them, allowing for a more fluent syntax.
    • Lambdas with Receivers: Allow you to access members of a receiver object within a
      lambda, making it easy to build a configuration block.
    • Named and Default Arguments: Provide readable and flexible ways to define
      parameters, which is useful in DSL contexts.
    You can implement the Builder Pattern using a data class and DSL to create a more intuitive
    and expressive API for constructing complex objects.
* */

data class User(
    var name: String = "",
    var age: Int = 0,
    var phone: Int = 0,
    var address: String = ""
)

fun user(block: User.() -> Unit): User {
    return User().apply(block)
}

// Using val + copy
data class Person(
    var name: String = "",
    var age: Int = 0,
    var phone: Int = 0,
    var address: String = ""
)

fun main() {

    val user = user {
        name = "Test"
        age = 22
        phone = 222222222
        address = "123 Main st"
    }
    println(user)
    //User(name=Test, age=22, phone=222222222, address=123 Main st)


    val baseP = Person()
    val p1 = baseP.copy(
        name = "Test",
        age = 22,
        phone = 222222222,
        address = "123 Main st"
    )
    println(p1)
    // Person(name=Test, age=22, phone=222222222, address=123 Main st)

}
