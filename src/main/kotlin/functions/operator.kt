package functions

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import oop.classes.User

// The invoke() operator allows you to call an object like a function.
// Without operator, you’d always need to write .invoke().

// Example 1 - using contains
class MySet(private val data: Set<Int>) {
    operator fun contains(value: Int) = data.contains(value)
}

// Example 2- invoke

internal class GreeterOper {
    operator fun invoke(name: String) = "Hello, $name!"
}

// Example with Use case
interface BaseUseCase<in P, out T> {
    suspend operator fun invoke(p: P): T
}

class GetUserUseCase() : BaseUseCase<Int, User> {
    override suspend fun invoke(p: Int): User {
        return withContext(Dispatchers.IO) {
            delay(2000)
            User("Test User", 20)
        }
    }
}

fun main() {
    val s = MySet(setOf(1, 2, 3))
    println(2 in s)   // true
    println(5 in s)   // false

    val greeter = GreeterOper()
    println(greeter("Test"))   // Hello, Test
    println(greeter.invoke("Test")) // Same result

    // User
    runBlocking {
        val user = GetUserUseCase()
        println(user(1))
    }

}
