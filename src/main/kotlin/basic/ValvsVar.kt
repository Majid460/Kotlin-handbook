package org.example.basic

/*
* Val - read-only reference
* Var - Mutable reference
* val in Kotlin makes the reference read-only, meaning the variable cannot be reassigned. However, it does not guarantee immutability because the object's internal state may still change if the object itself is mutable.
* val list = MutableList
    list  ----->  [1,2,3]

Allowed:
    list.add(4)

Not allowed:
    list = new MutableList
*
* */
// Example why val is not immutable
fun whyValNotImmutable(){
    val list = mutableListOf(1,2,3)

    list.add(4)     // allowed - The content of the list is changed, but the reference remains constant
    list.remove(1) // Allowed

    // list = mutableListOf(5,6,7) // cannot reassign new reference
    println(list)
}
// True Immutable means
fun trueImmutable(){
    val list = listOf(1,2,3)
    // list.add(4) // not possible (Can't change state of object)
    // list = listOf(4) // Can't assign new reference
}

// Data class Val concept

fun dataClassVal(){
    data class User(val name : String)
    val user = User("Test")
   // user.name = "A" // can't change it
    // COPY performs a shallow copy
    val newUser = user.copy(name = "A") // Creates a copy of the user with new value and return a new object
    println(newUser)

}

/*
* There are three different things involved here:
1-  Variable
2-  Reference
3-  Object
*
* 1. Variable

    The variable is the name you use in code to access something.
    In this line:
        val user = User("Test")
        user is the variable name.
    So:
        Variable → user
    It is simply a label in your program.

* 2. The object is the actual instance created in memory (heap).

    This part creates the object:
    User("Majid")
    This allocates memory like:

    Object in memory
    ----------------
    User
    name = "Majid"
    So:
    Object → User(name="Majid")

* 3. Reference:
* The reference is the memory address pointing to the object.
The variable stores this reference.
Visualize memory like this:
Stack (variable)        Heap (object)

user  -----------→  User object
                    name = "Majid"

* Flow ->
* Kotlin creates a object in heap like User("Test)
* A variable is created "user" in stack
* Heap returned a reference of stored value
* Variable "user" stores that reference.
* user = 0xABC123
* user → User(name="Majid")
* */
fun main() {
    whyValNotImmutable()
    dataClassVal()
}