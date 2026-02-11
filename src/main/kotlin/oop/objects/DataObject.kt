package org.example.oop.objects

// data object is a special kind of singleton that behaves like a data class with no properties.
/*It is mainly used for:

Sealed hierarchies

State machines

UI states

Events

Pattern matching (when)
* */
data object DataObject {
    const val NAME = "test"
    const val AGE = 25
}

fun main() {
        println(DataObject.toString())
}
