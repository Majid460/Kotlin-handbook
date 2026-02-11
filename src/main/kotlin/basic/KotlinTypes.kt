package org.example.basic

import kotlin.reflect.KClass

// Integer Types
val one = 1 //int
val threeBillion = 3000000000 // long
val oneLong = 1L //long
val oneByte: Byte = 1

// Floating Types

val oneFloat = 1F
val oneDouble = 1.0

// String
val str: String = "A string"
val ch: Char = 'A'

// Literal constants for numbers
const val oneMil = 1_000_000
const val creditCardNumber = 5343_3443_3455_1232L
const val hexBytes = 0xFF_EC_DE_5E
const val bytes = 0b11010010_01101001_10010100_10010010
const val bigFractional = 1234567.7182818283

// Type conversion - Explicit conversion
const val byte: Byte = 1
const val inInt: Int = byte.toInt()

// Boolean
val myTrue: Boolean = true
val myFalse: Boolean = false
val boolNull: Boolean? = null

// String Interpolation
const val productName = "carrot"
const val requestedData =
    $$"""{
      "currency": "$",
      "enteredAmount": "42.45 $",
      "serviceField": "none",
      "product": "$$productName"
    }
    """

//{
//    "currency": "$",
//    "enteredAmount": "42.45 $$",
//    "$$serviceField": "none",
//    "product": "carrot"
//}

// Arrays
// 1. Create an array
val arr = arrayOf(1,2,3)
val nullArr : Array<Int?> = arrayOfNulls(3)
var exampleArray = emptyArray<String>()

// 2D array
val simple2DArr = Array(2){ Array<Int?>(2) {0} }
// Access Elements
val element = simple2DArr[0][0]


fun main() {
    println(oneMil)
    println(creditCardNumber)
    println(hexBytes)
    println(bytes)
    println(bigFractional)

    println(inInt)

    println(myTrue || myFalse)
    // true
    println(myTrue && myFalse)
    // false
    println(!myTrue)
    // false
    println(boolNull)
    // null
    println(requestedData)
    println(arr.joinToString())
    println(nullArr.joinToString())
    println(exampleArray.joinToString())
    println(simple2DArr.contentDeepToString())
    println(element.toString()) // 0

}
