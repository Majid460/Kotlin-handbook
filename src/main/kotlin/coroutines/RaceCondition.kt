import kotlinx.coroutines.*
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.atomic.AtomicInteger

var balance = 1000
val mutex = Mutex()

suspend fun simpleExample() = coroutineScope {

    val jobs = List(1000) {
        launch(Dispatchers.Default) {
            deposit(100)
            withDraw(50)
        }
    }

    jobs.joinAll()

    println("Final balance = $balance")
}
// The ways to overcome race condition
// 1. Using Mutex : Now only one coroutine modifies balance at a time.

suspend fun deposit(amount: Int) {
    val current = balance
    delay(1) // simulate context switch
    mutex.withLock {
        balance = current + amount
    }

}

suspend fun withDraw(amount: Int) {
    val current = balance
    delay(1)
    mutex.withLock {
        balance = current - amount
    }
}
// 2. Use atomic types so operations happen atomically (indivisible).
val atomicBalance = AtomicInteger(1000)
fun deposit1(amount: Int) {
    atomicBalance.addAndGet(amount)
}

fun withdraw1(amount: Int) {
    atomicBalance.addAndGet(-amount)
}

// 3. 2. synchronized (Classic Java Lock)
//You can protect the critical section using synchronization.

var syncBalance = 1000

@Synchronized
fun deposit2(amount: Int) {
    syncBalance += amount
}

@Synchronized
fun withdraw2(amount: Int) {
    syncBalance -= amount
}
// 4. Actor Model (Recommended for Coroutines)
//  Actors process messages one at a time.

sealed class BalanceMsg
data class Deposit(val amount: Int): BalanceMsg()
data class Withdraw(val amount: Int): BalanceMsg()

fun CoroutineScope.balanceActor() = actor<BalanceMsg> {

    var balance = 1000

    for (msg in channel) {
        when (msg) {
            is Deposit -> balance += msg.amount
            is Withdraw -> balance -= msg.amount
        }
        println(balance)
    }
}
// Actor ensures messages are processed sequentially.

// 5.Immutable State (Best for UI State)
//
//Instead of modifying shared data, create new state objects.
data class UiState(val balance: Int)

var state = UiState(1000)

fun deposit4(amount: Int) {
    state = state.copy(balance = state.balance + amount)
}
fun main() = runBlocking {
    simpleExample()
    val actor = balanceActor()

    actor.send(Deposit(100))
    actor.send(Withdraw(50))

}