package oop.classes

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking


// A class is a blueprint or template for creating objects (instances) that share:
// properties (data, like name, age, color)
// functions (methods) (behavior, like walk(), drive(), displayInfo())
// So in simple words:
//A class defines what an object is and what it can do.

class ClassName {
    companion object{
        const val AA = ""
        fun display(){
            println("First loaded")
        }
    }
}
// Data class:
// Data classes in Kotlin are primarily used to hold data.
// For each data class, the compiler automatically generates additional member functions that allow you to print an instance to readable output, compare instances, copy instances, and more.
// Data classes are marked with data:
// data class User(val name: String, val age: Int)
/* The compiler automatically derives the following members from all properties declared in the primary constructor:
- equals()/hashCode() pair.
- toString() of the form "User(name=John, age=42)".
- componentN() functions corresponding to the properties in their order of declaration.
- copy() function (see below).
To ensure consistency and meaningful behavior of the generated code, data classes have to fulfill the following requirements:

- The primary constructor must have at least one parameter.

- All primary constructor parameters must be marked as val or var.

- Data classes can't be abstract, open, sealed, or inner.

*/

data class User(val name: String, val age: Int, val subjects: MutableList<String>? = null)

/*
* A sealed class is a special kind of class in Kotlin that:
- restricts inheritance to a fixed set of subclasses,
- all of which must be declared in the same file.
- You can think of it as:
- “An abstract class with a closed, known set of subclasses.”
This is great for representing limited, well-defined types, like states, results, or responses.*/

sealed class Result {
    class Success(val data: String) : Result()
    class Error(val errorMessage: String) : Result()
    data object Loading : Result()
}


fun handleResult(result: Result) {
    when (result) {
        is Result.Success -> {
            println("Successful data")
        }

        is Result.Error -> {
            println("Error while getting the result")
        }

        is Result.Loading -> {
            println("Loading...")
        }

    }
}

// Generic classes
class Box<T>(t: T) {
    val value = t
}

interface Comparable<in T> {
    operator fun compareTo(v: T): Int
}

fun demo(x: Comparable<Number>) {
    x.compareTo(1.0) // 1.0 has type Double, which is a subtype of Number
    // Thus, you can assign x to a variable of type Comparable<Double>
    val y: Comparable<Double> = x // OK!
}
/*
*
fuFunction<*, String> means Function<in Nothing, String>.
Function<Int, *> means Function<Int, out Any?>.
Function<*, *> means Function<in Nothing, out Any?>
*/
// Classes are not only declarations that can have type parameters.
// Functions can too, Type parameters are placed before the name of function

fun <T> genericFun(item: T): List<T> {
    val list = mutableListOf<T>()
    list.add(item)
    return list
}


fun main() {
    val clas = ClassName.display()
    val originalUser = User("Jhon", age = 10)
    // The copy() function creates a shallow copy of the instance.
    val olderUser = originalUser.copy(name = "New user")

    println(olderUser)

    // Example to show the copy function as shallow copy
    // Insert elements in the subject list and then change in copy you will see the changes also

    val userOriginalCopy = User("Oreal", age = 20, subjects = mutableListOf("A", "B"))
    println(userOriginalCopy)
    val userShallowCopy = userOriginalCopy.copy()

    userShallowCopy.subjects?.add("C")

    println("Original user with subjects:: $userOriginalCopy")
    println("Shallow user with new subjects $userShallowCopy")
    /*
    * Original user with subjects:: User(name=Oreal, age=20, subjects=[A, B, C])
      Shallow user with new subjects User(name=Oreal, age=20, subjects=[A, B, C])
      * */
    // Data class destructuring
    val (name, age) = userOriginalCopy
    println("Destructuring of data class object:: $name , $age")

    // Destructuring of data class object:: Oreal , 20

    // Sealed class

    var result: Result = Result.Loading
    handleResult(result)
    runBlocking {
        delay(3000)
        result = Result.Success("Success")
        handleResult(result)
    }

    // Generics
    val b: Box<Int> = Box(1)
    println(b.value)

    val list = listOf<Int>(1, 2, 3, 4)
    list.forEach {
        genericFun(it).also { v ->
            println("Values:: $v")
        }
    }
    arrayOf("A", "B", "C").forEach { str -> genericFun(str).filter { s -> s!="A" }.also { v -> println("Values:: $v") } }


}
