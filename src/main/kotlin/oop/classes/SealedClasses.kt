package org.example.oop.classes

/*
* Sealed classes are used to model complex hierarchies or states where each subtype may
have unique properties and behavior. They are ideal for representing scenarios with varying
data structures or polymorphic states, ensuring exhaustive type safety in when expressions.
*/
sealed class Payment {
    object Cash : Payment()
    data class CreditCard(val cardNumber: String) : Payment()
    data class PayPal(val email: String) : Payment()
}

fun processPayment(payment: Payment) {
    when (payment) {
        is Payment.Cash -> println("Paying with cash")
        is Payment.CreditCard -> println("Paying with card: ${payment.cardNumber}")
        is Payment.PayPal -> println("Paying via PayPal: ${payment.email}")
    }
}

sealed interface Response<T>

data class Success<T>(val data: T) : Response<T>
data class Error(val message: String) : Response<Nothing>

/*
* Enum classes, on the other hand, are suited for representing a fixed set of constants,
such as predefined categories or enumerations, and are typically simpler in structure and
functionality.
* */
enum class Direction(val degrees: Int) {
    NORTH(0),
    EAST(90),
    South(180),
    WEST(270);

    fun describe() = "Direction $name with $degrees degrees"
}
data class InData(val name:String)
data class OutData(val res: Int)

fun printDirection(direction: Direction) = println(direction.describe())
fun main() {
    val dir = listOf(Direction.NORTH, Direction.EAST, Direction.South, Direction.WEST)
    dir.forEach { direction -> printDirection(direction) }

    // Interface
    val res: Response<InData> = Success(InData("Majid"))
    println(res)
}
