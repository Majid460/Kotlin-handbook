package coroutines


import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.BUFFERED
import kotlinx.coroutines.channels.Channel.Factory.CONFLATED
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.channels.toList
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration.Companion.seconds

/*
* Channels
* - Writing code with a shared mutable state is quite difficult and error-prone (like in the solution using callbacks).
* - A simpler way is to share information by communication rather than by using a common mutable state. Coroutines can communicate with each other through channels.
* - Channels are communication primitives that allow data to be passed between coroutines.
* - One coroutine can send some information to a channel, while another can receive that information from it:
* Channels work with producer and consumer pattern
* Producer: Produces the value and send to channel (Can be one or more)
* Consumer: Consume the value from channel (Can be one or more)
* Multiple channel handling:
* - When many coroutines receive information from same channel, each element handled only once by one consumer,  Once an element is handled, it is immediately removed from the channel.
* - Channels are like a queue or collections where elements are added on one end and removed from other, but channels are suspendable with send and receive.
* - Channel is represented by three different interfaces: SendChannel, ReceiveChannel, and Channel, with the latter extending the first two.
* - You usually create a channel and give it to producers as a SendChannel instance so that only they can send information to the channel. You give a channel to consumers as a ReceiveChannel instance so that only they can receive from it. Both send and receive methods are declared as suspend:
* Types of channels:
* 1. Unlimited Channels (An unlimited channel is the closest analog to a queue: producers can send elements to this channel and it will keep growing indefinitely. The send() call will never be suspended. If the program runs out of memory, you'll get an OutOfMemoryException. The difference between an unlimited channel and a queue is that when a consumer tries to receive from an empty channel, it becomes suspended until some new elements are sent.)
* 2. Buffered Channels (The size of a buffered channel is constrained by the specified number. Producers can send elements to this channel until the size limit is reached. All of the elements are internally stored. When the channel is full, the next `send` call on it is suspended until more free space becomes available.)
* 3. Rendezvous Channels (The "Rendezvous" channel is a channel without a buffer, the same as a buffered channel with zero size. One of the functions (send() or receive()) is always suspended until the other is called.)
* 4. Conflated Channel (A new element sent to the conflated channel will overwrite the previously sent element, so the receiver will always get only the latest element. The send() call is never suspended.)
* */

suspend fun createAChannel() {
    val confChannel = Channel<String>(CONFLATED)

    // ConfChannel
    coroutineScope {
        launch {
            confChannel.send("Conflated Channel Send 1")
            println("Conflated Channels: Sent the value in conflated channel 1")

        }
        launch {
            confChannel.send("Conflated Channel Send 2")
            println("Conflated Channels: Sent the value in conflated channel 2")

        }
        launch {
            delay(1.seconds)
            repeat(3) {
                val x = confChannel.receive()
                println("Conflated Channels: Received the value in conflated channel: $x")
            }
        }

    }

}
suspend fun bufferedChannel() {

    val channel = Channel<String>(2)

    coroutineScope {

        repeat(4) { i ->
            launch {
                println("Trying send ${i+1}")
                channel.send("Send ${i+1}")
                println("Sent ${i+1}")
            }
        }

        launch {
            delay(5.seconds)

            for(x in channel) {
                println("Received $x")
            }
        }
        delay(1.seconds)

        channel.close()
    }
}
suspend fun unlimitChannel(){
    val unlimitChannel = Channel<String>(UNLIMITED)

    withContext(Dispatchers.Default){
        // Unlimited Channel
        //Send
        launch {
            val x1 = "Unlimited Channel Send 1"
            println("Sending $x1 from ::${Thread.currentThread().name} ")
            unlimitChannel.send("")
        }
        launch {
            val x2 = "Unlimited Channel Send 2"
            println("Sending $x2 from ::${Thread.currentThread().name} ")
            unlimitChannel.send(x2)
        }

        // Receive
        launch {
            delay(1.seconds)
            repeat(unlimitChannel.toList().size) {
                val x = unlimitChannel.receive()
                println("Receiving unlimit from ::${Thread.currentThread().name} value is:: $x ")
            }
        }
    }

}
suspend fun randezousChannel(){
    val rendezvousChannel = Channel<Int>() // Rendezvous
    // Randezvous Channel
    // Sending
    withContext(Dispatchers.Default){
        launch {
            println("Sending Rand from ::${Thread.currentThread().name}")
            println("sending 1")
            rendezvousChannel.send(1)          // suspends HERE until someone receives
            println("sending 2")     // only runs after 1 is received
            rendezvousChannel.send(2)
            rendezvousChannel.close()
        }
        // Receiving
        launch {
            println("Receiving Rand from ::${Thread.currentThread().name}")
            delay(1000)
            val value = rendezvousChannel.receive()   // producer resumes now
            println("got: $value")
            for (value in rendezvousChannel) {
                println("got in loop: $value")
            }
        }
    }
}
var acquired = 0
class Resource {
    init { acquired++ } // Acquire the resource
    fun close() { acquired-- } // Release the resource
}
// Real life Message passing example of channels
data class Message(val from: String, val content: String)

suspend fun passMessages() {
    val channel = Channel<Message>(BUFFERED)
    // Produce server simulation
    coroutineScope {
        launch {
            listOf(
                Message("Alice", "Hi!"), Message("Bob", "Hello!"),
                Message("Alice", "How are you?")
            ).forEach { i ->
                delay(500)
                channel.send(i)
            }
            channel.close()
        }
        launch {
            for (message in channel){
                println("Display in chat: ${message.from}: ${message.content}")
            }
        }
        repeat(10_000) { // Launch 10K coroutines
            launch {
                val resource = withTimeout(60) { // Timeout of 60 ms
                    delay(50) // Delay for 50 ms
                    Resource() // Acquire a resource and return it from withTimeout block
                }
                resource.close() // Release the resource
            }
        }
    }
    println(acquired)
}

fun main() {
    runBlocking {
       // randezousChannel()
        bufferedChannel()
    }

}