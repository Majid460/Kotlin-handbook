package coroutines.flows

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

// Simulates fetching news for a topic with varying delays
fun fetchNewsForTopic(topic: String): Flow<String> = flow {
    val delayTime = if (topic == "Tech") 100L else 300L
    println("Fetching news for $topic...")
    delay(delayTime)
    emit("Article 1 for $topic")
    delay(delayTime)
    emit("Article 2 for $topic")
}

suspend fun flatMapMerge() {
    val topics = flowOf("Tech", "Sports")

    println("--- Using flatMapMerge ---")
    // It works as concurrently and launches multiple coroutines in parallel
    topics.flatMapMerge { topic ->
        fetchNewsForTopic(topic)
    }.collect { article ->
        println("Displaying: $article")
    }
}

// Channel Flow
fun getHighPriorityAlerts(): Flow<String> = flow {
    delay(500)
    emit("Alert: System critical!")
    delay(1000)
    emit("Alert: User logged out!")
}

fun getStandardNotifications(): Flow<String> = flow {
    delay(300)
    emit("Notification: Update available")
    delay(600)
    emit("Notification: New message")
}
/**
 * One of the most common use cases for `channelFlow` is to collect data from multiple sources
 * concurrently and merge them into a single stream. The `ProducerScope’s` ability to launch
 * new coroutines is the key enabler here.
 *
 * Imagine a dashboard that needs to display a live feed of both “high-priority alerts” and
 * “standard notifications.” These come from two different `Flows`, and we want to display them
 * in a single list as they arrive.*/
suspend fun channelFlowSimulation() {
    val combinedFeed: Flow<String> = channelFlow {
        // Launch a new coroutine to collect alerts.
        launch {
            getHighPriorityAlerts().collect { alert ->
                send("Priority Feed: $alert")
            }
        }

        // Launch another new coroutine to collect notifications.
        launch {
            getStandardNotifications().collect { notification ->
                send("Standard Feed: $notification")
            }
        }
    }

    // The consumer will receive events as they are produced by either coroutine.
    combinedFeed.collect { event ->
        println(event)
    }
}

fun main() = runBlocking {
 //   flatMapMerge()
    channelFlowSimulation()
}