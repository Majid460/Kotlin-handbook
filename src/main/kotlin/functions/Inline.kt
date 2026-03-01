package org.example.functions

/*The inline keyword is used to optimize higher-order functions by reducing the runtime
overhead associated with function calls, particularly when lambda expressions are involved.
When a function is marked as inline, the Kotlin compiler substitutes the function body
directly into the places where the function is called. This substitution eliminates the need to
allocate memory for function objects and avoids the overhead of lambda invocation.
*/

private inline fun inlineFun(lambda: () -> Unit) {
    println("COMPUTING started")
    lambda() // The code from the lambda will be copied here
    println("Computing ended")
}

// Non-inlined
/*
* Without inline, every time
   you pass a lambda to a function, the compiler must create a Function object to represent that
   lambda. This involves:
    1. Object Allocation: A new object is created on the heap to hold the lambda’s code.
    2. Memory Overhead: This object consumes memory.
    3. Virtual Method Call: Invoking the lambda requires a virtual method call (invoke()),
  which is slightly slower than a direct method call.*/
private fun nonInlined(lambda: () -> Unit) {
    println("COMPUTING started")
    lambda() // This is a virtual call to a Function object
    println("Computing ended")
}


//  Non-local returns
private inline fun findFirst(numbers: List<Int>, predicate: (Int) -> Boolean): Int? {
    for (number in numbers) {
        if (predicate(number)) {
            return number
        }
    }
    println("End")

    return null
}

// Reified use with inline -> to retain the generic type at run time and avoid eraser in the JVM
private inline fun <reified T> isInstance(value: Any): Boolean {
    return value is T
}

inline fun <reified T> List<Any>.filterByType(): List<T> {
    return this.filterIsInstance<T>()
}


fun main() {
    inlineFun { println("Performing heavy computing") }
    nonInlined { println("Performing heavy computing") }

    // Non-local returns -> feature of inline word
    val first = findFirst(listOf(1, 2, 3, 4, 5, 6, 7)) {
        it % 2 == 0
    }
    println("First Even number is:$first")

    // Reified
    println(isInstance<String>("This is string")) // true
    println(isInstance<Int>("This is Int")) // false
    // without reified it is not possible to check the generic type of function at run time

    val mixedList = listOf(1, "Class A", 2.5, "Programming")
    val stringList = mixedList.filterByType<String>()

    println(stringList) // Outputs: [Class A, Programming]
}

/*
* how it is compiled
* // For nonInlinedAction, a Function object is created
 println("Before action")
 Function0 { println("Executing non-inlined action") }.invoke()
 println("After action")

 // For inlinedAction, the code is copied directly
 println("Before action")
 println("Executing inlined action") // No object, no virtual call
 println("After action")
*
* This elimination of object creation and virtual calls is the core performance benefit.
* */

// Non-local returns -> Returns from the outer function by using inline
//  but
/*  In a regular (non-inlined) lambda, you can only use return to exit from the lambda itself
    (return@label). You cannot use a plain return to exit the enclosing function.
* */
