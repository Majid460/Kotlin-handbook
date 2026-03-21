package oop

import kotlin.properties.Delegates


/** ### Delegation means:
 * An object passes a task to another object that knows how to do it better.
 * Delegation helps:
 * 1. avoid huge classes
 * 2. reuse logic
 * 3. reduce code duplication
 * 4. follow composition over inheritance
 */


interface Engine {
    fun start()
}

class PetrolEngine : Engine {
    override fun start() {
        println("Engine started")
    }
}
// Example: Without by keyword
class Car(val engine: Engine) : Engine {
    override fun start() {
        println("Car starting...")
        engine.start()
    }
}
//Example with by
 class CarWithDelegation(val engine: Engine) : Engine by engine

class DelegateProperties {
    var name : String by Delegates.observable("unknown"){
        property, oldValue, newValue -> println("$oldValue -> $newValue")
    }
    val lazyValue: String by lazy {
        println("computed!")
        "Hello"
    }


}

// Delegation in

fun main() {
    val user = DelegateProperties()
    user.name = "first"
    user.name = "second"

    println(user.lazyValue)
    println(user.lazyValue)

    // Without Delegation
    val petrolEngine = PetrolEngine()
    val carOne = Car(petrolEngine)
    carOne.start()

    // With Delegation
    val carTwo = CarWithDelegation(petrolEngine)
    carTwo.start()

}
