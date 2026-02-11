package org.example.oop

import kotlin.properties.Delegates


class DelegateProperties {
    var name : String by Delegates.observable("unknown"){
        property, oldValue, newValue -> println("$oldValue -> $newValue")
    }
    val lazyValue: String by lazy {
        println("computed!")
        "Hello"
    }


}

fun main() {
    val user = DelegateProperties()
    user.name = "first"
    user.name = "second"

    println(user.lazyValue)
    println(user.lazyValue)
}
