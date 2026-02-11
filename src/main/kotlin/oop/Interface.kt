package org.example.oop

/*
* Interfaces in Kotlin can contain declarations of abstract methods,
* as well as method implementations. What makes them different from abstract Classes.kt
* is that interfaces cannot store state. They can have properties, but these need to
* be abstract or provide accessor implementations.*/

interface MyInterface { // Optional Body
    fun hello()
    fun foo() {
        // optional body
    }
}

// Implementing interfaces
// A class or object can implement one or more interfaces:
class A : MyInterface {
    // Interface is a contract and we need to implement its functions
    override fun hello() {
        println("Welcome in A class")
    }


}
// Properties in interfaces
/*
* You can declare properties in interfaces.
* A property declared in an interface can either be abstract or provide implementations for accessors.
*  Properties declared in interfaces can't have backing fields, and therefore accessors declared in interfaces can't reference them:
*/

interface MyInterfaceT {
    val prop: Int  // abstract property

    val propertyWithImpl: String
        get() = "foo"

    fun foo() {
        print(prop)
    }
}

class Child : MyInterfaceT {
    override val prop: Int
        get() = 29
    override val propertyWithImpl: String
        get() = super.propertyWithImpl

    override fun foo() {
        super.foo()
    }
}

// Resolving overriding conflicts
interface AA {
    fun foo() {
        print("A")
    }

    fun bar()
}

interface B {
    fun foo() {
        print("B")
    }

    fun bar() {
        print("bar")
    }
}

class C : AA {
    override fun bar() {
        print("bar")
    }
}

class D : AA, B {
    override fun foo() {
        super<AA>.foo()
        super<B>.foo()
    }

    override fun bar() {
        super<B>.bar()
    }
}
//.   ------------------- Functional Interface -----------------------
/*
* An interface with only one abstract member function is called a functional interface,
* or a Single Abstract Method (SAM) interface.
*
*
* To declare a functional interface in Kotlin, use the fun modifier.
*  */

fun interface KRunnable {
    fun invoke()
}

// Implementation of SAM function
val runnable = object : KRunnable {
    override fun invoke() {
        println("Implementing the runnable function")
    }
}

// Implementation using Lambda
val run = KRunnable {
    println("Implementing the runnable using lambda")
}

// Second example
fun interface IntPredicate {
    fun accept(i: Int): Boolean
}

val isEven = IntPredicate { it % 2 == 0 }

fun main() {
    println("IS Even or not:: ${isEven.accept(4)}")
    // IS Even or not:: true
}
