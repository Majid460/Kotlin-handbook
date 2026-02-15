package org.example.properties

// A backing field is an implicit storage mechanism that Kotlin generates for a property when
// you define custom getters or setters and use the field keyword. It allows the property
// to hold its value while enabling custom logic for access or modification.

// 1. Backing field
var name: String = "Default"
    get() = field.uppercase()
    set(value) {
        field = value.trim()
    }
// Backing properties are explicitly defined variables used to store the actual value of a property.
// Unlike backing fields, backing properties are manually created and give you complete control
// over the property’s internal representation.

// 2. Backing Property

private val _list = mutableListOf(1,2,3)

val listP : List<Int>
    get() = _list

fun setList(a:Int){
    _list.add(a)
}


fun main(){
    name = "Test t"
    println(name)

    setList(4)
    println(listP)
}
