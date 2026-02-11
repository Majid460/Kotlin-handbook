package org.example.oop

/*
* Classes, objects, interfaces, constructors, and functions, as well as properties and their setters,
* can have visibility modifiers. Getters always have the same visibility as their properties.

There are four visibility modifiers in Kotlin:
* private, protected, internal, and public.
* The default visibility is public.

On this page, you'll learn how the modifiers apply to different types of declaring scopes.
*
*
* If you don't use a visibility modifier, public is used by default, which means that your declarations will be visible everywhere.

* If you mark a declaration as private, it will only be visible inside the file that contains the declaration.

* If you mark it as internal, it will be visible everywhere in the same module.

* The protected modifier is not available for top-level declarations.*/

// file name: VisibilityModifier.kt
// package org.example.oop

private fun foo() { } // visible inside example.kt

public var bar: Int = 5 // property is visible everywhere
    private set         // setter is visible only in example.kt

internal val baz = 6    // visible inside the same module

/*
* Class members
For members declared inside a class:

* private means that the member is visible inside this class only (including all its members).

* protected means that the member has the same visibility as one marked as private, but that it is also visible in subclasses.

* internal means that any client inside this module who sees the declaring class sees its internal members.

* public means that any client who sees the declaring class sees its public members.

In Kotlin, an outer class does not see private members of its inner Classes.kt.*/
open class Outer {
    private val a = 1
    protected open val b = 2
    internal open val c = 3
    val d = 4  // public by default

    protected class Nested {
        public val e: Int = 5
    }
}

class Subclass : Outer() {
    // a is not visible
    // b, c and d are visible
    // Nested and e are visible

    override val b = 5   // 'b' is protected
    override val c = 7   // 'c' is internal
}

class Unrelated(o: Outer) {
    // o.a, o.b are not visible
    // o.c and o.d are visible (same module)
    // Outer.Nested is not visible, and Nested::e is not visible either
}
