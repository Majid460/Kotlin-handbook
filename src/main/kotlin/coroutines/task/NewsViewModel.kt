package coroutines.task

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope
import kotlin.system.measureTimeMillis


data class DashboardData(
    val topHeadlines: List<String>,
    val userProfile: String,
    val weatherUpdate: String
)

// ── Simulated ViewModel ───────────────────────────────────────────────────
/**
 * ## Task
 * 1. Fetch three things in PARALLEL:
 *    - fetchTopHeadlines()  — takes 800ms  — returns List<String>
 *    - fetchUserProfile()   — takes 500ms  — returns String
 *    - fetchWeatherUpdate() — takes 300ms  — returns String
 *
 * 2. If fetchTopHeadlines() fails:
 *    - other two should CONTINUE running
 *    - headlines should fallback to emptyList()
 *
 * 3. If fetchUserProfile() fails:
 *    - other two should CONTINUE running
 *    - profile should fallback to "Guest"
 *
 * 4. Print the result only after ALL three are done
 *
 * 5. Total time must be close to 800ms — NOT 1600ms
 *    (prove you understand parallel vs sequential)
 *
 * 6. All work must happen on Dispatchers.IO
 *
 * 7. Result must be posted back on Main thread
 *
 * 8. If ViewModel is cleared mid-fetch — everything cancels cleanly
 *
 * */
class NewsViewModel {

    // SupervisorJob — children independent, same as viewModelScope
    // No Dispatchers.Main in pure Kotlin — use Default for output
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    fun loadDashboard() {
        scope.launch {
            val time = measureTimeMillis {
                supervisorScope {
                    val topHeadLinesDeferred = async { fetchTopHeadlines() }
                    val userProfileDeferred = async { fetchUserProfile() }
                    val weatherDetailsDeferred = async { fetchWeatherUpdate() }
                    // runCatching per await — clean and functional
                    val topHeadlines = runCatching { topHeadLinesDeferred.await() }
                        .onFailure { println("Headlines failed: ${it.message}") }
                        .getOrDefault(emptyList())

                    val userProfile = runCatching { userProfileDeferred.await() }
                        .onFailure { println("Profile failed: ${it.message}") }
                        .getOrDefault("Guest")

                    val weatherDetails = runCatching { weatherDetailsDeferred.await() }
                        .onFailure { println("Weather failed: ${it.message}") }
                        .getOrDefault("Unavailable")

                    val data = DashboardData(
                        topHeadlines,
                        userProfile,
                        weatherDetails
                    )
                    println("Data = $data")
                }
            }
            println("Total time: $time ms")
        }
    }

    // simulates onCleared() — cancels all coroutines
    fun clear() {
        scope.cancel()
        println("ViewModel cleared — all coroutines cancelled")
    }
}

// ── Fake suspend functions ────────────────────────────────────────────────

// make fetchTopHeadlines throw
suspend fun fetchTopHeadlines(): List<String> {
    delay(800)
    throw RuntimeException("API limit reached")
}

suspend fun fetchUserProfile(): String {
    delay(500)
    throw RuntimeException("Auth expired")
}

suspend fun fetchWeatherUpdate(): String {
    delay(300)
    return "Dublin, 12°C"
}
//suspend fun fetchTopHeadlines(): List<String> {
//    delay(800)
//    return listOf("Headline 1", "Headline 2")
//}
//
//suspend fun fetchUserProfile(): String {
//    delay(500)
//    return "Majid Shahbaz"
//}
//
//suspend fun fetchWeatherUpdate(): String {
//    delay(300)
//    return "Dublin, 12°C"
//}

// ── Entry point ───────────────────────────────────────────────────────────

fun main() = runBlocking {
    val viewModel = NewsViewModel()

    viewModel.loadDashboard()

    // give coroutines time to finish
    // simulates Android keeping activity alive
    delay(2000)

    // simulates activity destroyed
    viewModel.clear()
}