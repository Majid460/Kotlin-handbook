package org.example.coroutines

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlin.system.measureTimeMillis

// To represent the stream of values that are being computed asynchronously, we can use a Flow<Int> type just like we would use a Sequence<Int> type for synchronously computed values:

fun simple(): Flow<Int> = flow {
    for (i in 1..3) {
        delay(1000)
        emit(i)
    }
}

// Flows transformation
suspend fun anyFunction(request: Int): String {
    delay(1000)
    return "response: $request"
}

// Flow with
suspend fun flowTransformation() {
    (1..3).asFlow().map { value -> anyFunction(value) }.collect { response ->
        println(response)
    }
}

// Flow with filter and map
data class User(val name: String, val active: Boolean)
data class UiUserDto(val name: String, val active: Boolean, val age: Int)

val users = listOf(
    User("Majid", true), User("Ali", false),
    User("Sara", true),
    User("Ayesha", false)
)

class UserImpl {
    // Get users from server
    fun getUsers(): Flow<User> = flow {
        users.forEach { it ->
            delay(100)
            emit(it)
        }
    }

    init {
        runBlocking {
//            mainManger()
//            singleHandler()
//            convertToUiDTO()
//            applyBuffer()
//            conflation()
//            usingCollectLatest()
//            zipFlows()
//            combineFlows()
//            flattenFlowImpl()
//            flattenFlowsWithMap()
//            flatMapMerge()
//            flatMapLatest()
//            testSimpleException()
            catchWithCoroutine()
            checkFlowCompleted()
        }
    }

    suspend fun mainManger() {
        withContext(Dispatchers.Default) {
            getUsers().collect { value ->
                val filterActive = async { getActive(value) }
                val transformName = async { transformName(value) }
                awaitAll(filterActive, transformName)
            }
        }
    }

    fun getActive(user: User) {
        if (user.active) println("User: ${user.name} is Active")
    }

    fun transformName(user: User) {
        println(user.name.uppercase())
    }

    suspend fun singleHandler() {
        getUsers().filter { it -> it.active }.map { user -> user.name.uppercase() }
            .collect { value -> println("Active user: $value") }
    }

    suspend fun convertToUiDTO() {
        getUsers().map { value -> UiUserDto(value.name, value.active, 20) }
            .collect { value -> print("New User:$value") }
        getUsers().collect { value ->
            val data = UiUserDto(value.name, value.active, 20)
            println(data)
        }
    }

    // Buffer operator
    suspend fun applyBuffer() {
        val time = measureTimeMillis {
            getUsers().buffer().collect { user ->
                println("User :: $user")
            }
        }
        println("Time taken to run the function: $time")

    }

    // Conflate: It processes the only most recent values and drops the previous values

    suspend fun conflation() {
        getUsers().conflate().collect { value ->
            delay(1300)
            println("The value using conflate:: $value")
        }
    }

    // Explanation:
    // When a new emission arrives, collectLatest cancels the previous collect block immediately.
    // That means your ongoing delay(1300) inside the collector will be cancelled before it completes.
    // It works the same way as conflation, but the difference is it stop the processing on previous value and starts for new
    suspend fun usingCollectLatest() {
        getUsers().collectLatest { value ->
            delay(1300)
            println("The value using collect latest:: $value")
        }
    }

    // Combining the flows using zip
    // ZIP:
    // Waits for both flows to have a value ready.
    // Emits pairs sequentially: (first from A, first from B), (second from A, second from B)
    // Stops when either flow completes.
    suspend fun zipFlows() {
        val nums = (1..3).asFlow()
        val str = flowOf("one", "two", "three")
        nums.zip(str) { a, b ->
            "$a -> $b"
        }.collect { s ->
            println("The combine value using zip:: $s")
        }
    }
    /*Combine:
    Emits immediately when either flow emits a new value.
    Always use the latest value from each flow.
    Keep emitting until both flows complete.*/

    suspend fun combineFlows() {
        val nums = (1..3).asFlow().onEach { delay(300) }
        val str = flowOf("one", "two", "three").onEach { delay(400) }
        val startTime = System.currentTimeMillis()
        nums.combine(str) { a, b ->
            "$a -> $b"
        }.collect { s ->
            println("$s at ${System.currentTimeMillis() - startTime} ms from start")
        }
    }

    fun flattenFlows(v: Int): Flow<String> = flow {
        emit("$v : First")
        delay(500)
        emit("$v : Second")
    }

    suspend fun flattenFlowImpl() {
        (1..3).asFlow().map { flattenFlows(it) }.collect {
            println("Before Flatten the flows:: $it")
        }
    }

    // To flatten the upper flow -  It flats the flows one by one as it emits and then they are flattened
    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun flattenFlowsWithMap() {
        (1..3).asFlow().onEach { delay(100) }.flatMapConcat { value -> flattenFlows(value) }.collect {
            println("Before Flatten the flows:: $it")
        }
    }

    // To flatten the flows concurrently, we can use the flatMapMerge
    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun flatMapMerge() {
        println("\n Flatten the map with flatMapMerge")
        val startTime = System.currentTimeMillis() // remember the start time
        (1..3).asFlow().onEach { delay(100) } // a number every 100 ms
            .flatMapMerge { flattenFlows(it) }
            .collect { value -> // collect and print
                println("$value at ${System.currentTimeMillis() - startTime} ms from start")
            }
    }
    // flatMapLatest
    // In a similar way to the collectLatest operator, that was described in the section "Processing the latest value", there is the corresponding "Latest" flattening mode where the collection of the previous flow is cancelled as soon as new flow is emitted. It is implemented by the flatMapLatest operator.

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun flatMapLatest() {
        println("\n Flatten the map with flatMapLatest")
        val startTime = System.currentTimeMillis() // remember the start time
        (1..3).asFlow().onEach { delay(100) } // a number every 100 ms
            .flatMapLatest { flattenFlows(it) }
            .collect { value -> // collect and print
                println("$value at ${System.currentTimeMillis() - startTime} ms from start")
            }
    }

    // Exception Handling with flows
    fun simpleException(): Flow<Int> = flow {
        for (i in 1..3) {
            println("Emitting $i")
            emit(i) // emit next value
        }
    }

    // With try catch
    suspend fun testSimpleException() {
        println("Test the exception handling ")
        try {
            simpleException().collect { value ->
                println(value)
                check(value <= 1) { "Collected $value" }
            }
        } catch (e: Throwable) {
            println("Caught $e")
        }
    }

    fun simple(): Flow<String> =
        flow {
            for (i in 1..3) {
                println("Emitting $i")
                emit(i) // emit next value
            }
        }
            .map { value ->
                check(value <= 1) { "Crashed on $value" }
                "string $value"
            }

    // With Coroutine Catch
    suspend fun catchWithCoroutine() {
        simple().catch { e ->
            emit("Caught $e") // emit on exception
        }.collect { value ->
            println("Catch with Coroutine Catch: $value")
        }
    }
    // Check the flow completed or not
    // The onCompletion operator, unlike catch, does not handle the exception. As we can see from the above example code, the exception still flows downstream. It will be delivered to further onCompletion operators and can be handled with a catch operator.
    suspend fun checkFlowCompleted(){
        simple().onCompletion { cause -> if (cause!= null) println("Flow completed exceptionally") }
            .catch { cause -> println("Caught exception") }
            .collect { value -> println(value) }
    }

}
