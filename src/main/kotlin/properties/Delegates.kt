package org.example.properties

import kotlin.properties.Delegates

/*
* The delegated properties39 allow the delegation of property access to another object, which
  can handle the getter and setter logic. This is done using the by keyword, which connects
  the property to a delegate that defines how the property will be stored and retrieved.
* */

// 1. Lazy Initialization (lazy)
/*
* The lazy delegate allows for initializing a property only when it’s first accessed,
    avoiding the need to initialize it at object creation. In the code below, "Computed!" is
    printed, and the value "Hello, Kotlin!" is assigned only when lazyValue is accessed
    for the first time.
* */

val lazyName : String by lazy {
     println("Name Computed")
    "Test 12"
}

// 2. Observable Properties (Delegates.observable)
/*
* This delegate allows monitoring
  property changes by triggering a callback when the value changes. In the example
  below, "Value changed from Initial value to New value" is printed whenever
  observableValue changes:
  * */
var obName:String by Delegates.observable("Default"){ property, oldValue, newValue ->
    println("Value changed from $oldValue to $newValue")
}

// 3. Vetoable Properties (Delegates.vetoable)
/* The vetoable delegate is similar to observable but allows vetoing changes based on a
condition. In the example below, the new value is accepted only if it is greater than
the old value:
* */
var age: Int by Delegates.vetoable(0){ property, oldValue, newValue ->
        if (newValue>oldValue)
            println("new is greater than old")
            true
    false
}
// 4. Storing Properties in a Map (Map Delegate)
class User(val map : Map<String,Any>){
    val name:String by map
    val age:Int by map
}
fun a(){
    val ss by lazy {
        10
    }
    println(ss)
}
fun main() {
    lazyName // Name Computed
    println(lazyName) // Test 12

    // Observable
    println(obName) // only printed: Default
    obName = "New name" // Value changed from Default to New name

    // veto
    println(age) // 0
    age = 0 // No execution
    age = 1 // new is greater than old

    // map
    val us = User(mapOf("name" to "Test","age" to 12))
    println(us.name)
    println(us.age)

    a()





}
