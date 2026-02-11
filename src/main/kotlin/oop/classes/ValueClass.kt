package org.example.oop.classes

/*
* A value class is a special kind of class introduced to optimize performance by avoiding
unnecessary object allocation. Value classes wrap a single value but are inlined at runtime,
meaning they are treated as the underlying value rather than a full object. This makes them
lightweight and efficient while still providing type safety.
* */
@JvmInline
value class Password(val value:String)

fun authenticate(password: Password){
    println("Authenticating with password: ${password.value}")
}

/*In this example, the Password value class wraps a String. At runtime, this class will typically
be represented as a raw String, avoiding additional memory allocation for a Password object.
1. Type Safe: Value classes provide a clear and type-safe way to differentiate between
different usages of the same primitive type, like distinguishing between UserId and
OrderId that might both be Int.

2. Performance Optimization: They are compiled into the underlying type (when
possible) and avoid object allocation, reducing memory overhead.

3. Improved Semantics: Code becomes more expressive by explicitly defining what a
value represents.
*/

// Lets have a look why we need value class

fun sendEmail(email: Email){
    println("Email has been sent on $email")
}
fun sendMessage(phone: Phone){
    println("A message has been sent on $phone")
}
// Two value classes
@JvmInline
value class Email(val value:String)

@JvmInline
value class Phone(val value:String)


fun main(){
    authenticate(Password("1234"))

    // Example
   // sendEmail("123456777")  // wrong but compiler allows

    // To avoid this we can use value class to restrict only email can have email

    val email = Email("abc@gmail.com")
    val phone = Phone("09876554444")
    sendEmail(email)
    sendMessage(phone)
}
