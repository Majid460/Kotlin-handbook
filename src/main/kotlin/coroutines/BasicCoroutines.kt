package coroutines

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.seconds

/**
 * Introductory coroutine examples used by the app and by the concept README.
 *
 * This file focuses on:
 * - `suspend` functions as the building block for asynchronous work
 * - `withContext` for switching dispatchers and returning a value
 * - `coroutineScope` for structured concurrency
 * - `supervisorScope` when sibling failures must not cancel each other
 *
 * Typical flow:
 * 1. Write small suspending functions such as `generateNumbers()` or `greet()`.
 * 2. Call them from a coroutine scope such as `lifecycleScope`, `viewModelScope`,
 *    `coroutineScope`, or a service-owned scope.
 * 3. Select a dispatcher that matches the work type.
 */
object BasicCoroutines {

    /**
     * Example of a suspending function that emits values over time.
     *
     * A suspending function does not create a coroutine by itself. It must be
     * called from another coroutine or another suspending function.
     */
    suspend fun generateNumbers() {
        println("Numbers:")
        for (number in 1..5) {
            println("Number: $number")
            delay(500)
        }
    }

    /**
     * Simulates a lightweight asynchronous task.
     */
    suspend fun greet() {
        delay(1.seconds)
        println("greet() ran on ${Thread.currentThread().name}")
    }
}

/**
 * Demonstrates the most common coroutine scope functions in one place.
 *
 * Use this from a parent scope such as `lifecycleScope.launch { coroutines() }`.
 */
suspend fun coroutines() {
    val combinedResult = withContext(Dispatchers.Default) {
        launch { BasicCoroutines.generateNumbers() }
        launch { BasicCoroutines.greet() }
        "Completed work on ${Thread.currentThread().name}"
    }
    println(combinedResult)

    coroutineScope {
        launch {
            delay(2.seconds)
            println("Child coroutine 1 completed")
        }
        launch {
            delay(1.seconds)
            println("Child coroutine 2 completed")
        }
    }

    println("coroutineScope completed after all children")
}

/**
 * `supervisorScope` keeps sibling jobs independent.
 *
 * `fetchUser()` fails here, but `fetchPosts()` still runs because the failure
 * is handled locally instead of cancelling the whole scope.
 */
suspend fun supervisorScopeFetchAll() = supervisorScope {
    launch {
        runCatching { fetchUser() }
            .onFailure { println("fetchUser failed: ${it.message}") }
    }
    launch {
        fetchPosts()
    }
    println("supervisorScope launched both child coroutines")
}

/**
 * `suspend` lambda functions in function param
 * */
suspend fun fetchUserAndPosts(onSuccess: suspend (String) -> Unit) {
    val combinedResult = withContext(Dispatchers.Default) {
        delay(1000L)
        launch { fetchUser() }
        launch { fetchPosts() }
        "Completed work on ${Thread.currentThread().name}"
    }
    println("Not waiting")
    onSuccess(combinedResult)
}
/**
 * The two launches ran in parallel with each other —
 * ```kotlin
 *    launch { fetchUser() }
 *    launch { fetchPosts() }
 *    ```
 * The withContext returns immediately, without waiting for the response because launch is fire and forget
 * They became orphaned coroutines running after withContext already returned.
 * this is a structured concurrency violation
 * */

suspend fun fetchUser() {
    delay(500)
    println("Fetching user")
    throw RuntimeException("User fetch failed")
}

suspend fun fetchPosts() {
    delay(1.seconds)
    println("Fetching posts succeeded")
}

// Suspend workflow and concurrency
suspend fun suspendWorkflow() {
    println("I am started")
    coroutineScope {
        val data = getData()
        println("I am doing other work while getData suspended")
        println("Data from suspend -> $data")
    }
    println("I am done")
}
suspend fun getData():String{
    delay(4000)
    return "Test Suspend"
}

/**
 * Demonstrates four parallel execution patterns using Kotlin coroutines.
 *
 * ### Pattern 1 — launch inside withContext (WRONG)
 * `withContext` does not wait for internal `launch` blocks.
 * Children become orphaned coroutines — nobody tracks or cancels them.
 * ```kotlin
 * withContext(Dispatchers.Default) {
 *     launch { fetchUser() }     // parallel but orphaned ✗
 *     launch { fetchPosts() }    // parallel but orphaned ✗
 * }
 * // returns before launches finish ✗
 * ```
 *
 * ### Pattern 2 — coroutineScope + launch (CORRECT)
 * Use `coroutineScope` when you don't need return values but want to wait for all children.
 * ```kotlin
 * withContext(Dispatchers.Default) {
 *     coroutineScope {
 *         launch { fetchUser() }    // parallel ✓
 *         launch { fetchPosts() }   // parallel ✓
 *     }                             // waits for both ✓
 * }
 * ```
 *
 * ### Pattern 3 — async + await (CORRECT)
 * Use `async` when you need return values from parallel work.
 * ```kotlin
 * withContext(Dispatchers.Default) {
 *     coroutineScope {
 *         val u = async { fetchUser() }         // parallel ✓
 *         val p = async { fetchPosts() }        // parallel ✓
 *         println("${u.await()} ${p.await()}")  // waits for both ✓
 *     }
 * }
 * ```
 *
 * ### Pattern 4 — sequential (no parallel)
 * Each call waits for the previous to complete.
 * ```kotlin
 * withContext(Dispatchers.Default) {
 *     fetchUser()     // waits 500ms
 *     fetchPosts()    // starts only after fetchUser is done
 * }
 * // total time = 500 + 1000 = 1500ms
 * ```
 *
 * ### Timeline comparison
 *
 * **Pattern 1 — launch inside withContext (WRONG):**
 * - t=0ms    — withContext starts
 * - t=0ms    — `launch { fetchUser() }` fires
 * - t=0ms    — `launch { fetchPosts() }` fires
 * - t=0ms    — withContext returns immediately (does not wait)
 * - t=500ms  — fetchUser runs (orphaned — nobody tracking)
 * - t=1000ms — fetchPosts runs (orphaned — nobody tracking)
 *
 * **Pattern 2 — coroutineScope + launch (CORRECT):**
 * - t=0ms    — withContext + coroutineScope start
 * - t=0ms    — `launch { fetchUser() }` fires
 * - t=0ms    — `launch { fetchPosts() }` fires
 * - t=500ms  — fetchUser done
 * - t=1000ms — fetchPosts done
 * - t=1000ms — coroutineScope completes — withContext returns
 *
 * **Pattern 4 — sequential:**
 * - t=0ms    — fetchUser starts
 * - t=500ms  — fetchUser done
 * - t=500ms  — fetchPosts starts
 * - t=1500ms — fetchPosts done — withContext returns
 * - 500ms slower than parallel ✗
 *
 * @see coroutineScope
 * @see withContext
 * @see async
 */
suspend fun parallelExecutionPatterns() {
    // pattern implementations here
}
/**
 * Small blocking demo helper for docs and quick experiments.
 *
 * This is intentionally not named `main()`. Android application modules are
 * not plain JVM entry-point modules, so Android Studio creates an invalid run
 * configuration if a top-level `main()` is placed under `src/main/java`.
 */

//fun main() {
//    runBlocking {
//        suspendWorkflow()
////        launch {
////            BasicCoroutines.generateNumbers()
////        }
////        try {
////            fetchUserAndPosts { println(it) }
////        } catch (e: Exception) {
////            println("Caught expected exception in main: ${e.message}")
////        }
//    }
//}
fun main() = runBlocking {

    // ── Test 1: withContext ───────────────────────────────────────────────
    println("=== withContext Test ===")

    withContext(Dispatchers.Default) {
        println("1. withContext block starts")

        launch {
            println("3. launch inside withContext starts")
            delay(500)
            println("5. launch inside withContext done")
        }

        println("2. after launch — withContext carries on")
        delay(200)
        println("4. withContext after delay")
    }
    println("6. after withContext — but launches were ORPHANED")

    println()

    // ── Test 2: coroutineScope ────────────────────────────────────────────
    println("=== coroutineScope Test ===")

    coroutineScope {
        println("1. coroutineScope block starts")

        launch {
            println("3. launch inside coroutineScope starts")
            delay(500)
            println("5. launch inside coroutineScope done")
        }

        println("2. after launch — coroutineScope carries on")
        delay(200)
        println("4. coroutineScope after delay")
        // coroutineScope WAITS here for launch to finish
    }
    println("6. after coroutineScope — launch was properly waited for")
}
/**
 * Both show the same launch fire-and-return behaviour. The only difference is `coroutineScope` waits for launch to finish — `withContext` does not.
 * */
@Suppress
fun Unit.I():Unit {}

// Output:
// === withContext Test ===
// 1. withContext block starts
// 2. after launch — withContext carries on    ← launch returned immediately ✓
// 3. launch inside withContext starts         ← scheduler gave it a turn
// 4. withContext after delay                  ← took turns at delay
// 5. launch inside withContext done
// 6. after withContext — but launches were ORPHANED ← did not wait ✗

// === coroutineScope Test ===
// 1. coroutineScope block starts
// 2. after launch — coroutineScope carries on ← launch returned immediately ✓
// 3. launch inside coroutineScope starts      ← scheduler gave it a turn
// 4. coroutineScope after delay               ← took turns at delay
// 5. launch inside coroutineScope done
// 6. after coroutineScope — launch was properly waited for ✓
