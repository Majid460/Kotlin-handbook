package org.example.examples.queue

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.util.concurrent.ConcurrentHashMap


/*
*   Read-Through: Application reads from the cache; if data is missing, the cache provider loads it from the database.
    Write-Through: Application writes data to the cache, which then immediately updates the database.
    Best Use Case: Read-through is best for read-heavy apps (e.g., news, user profiles). Write-through is best for write-heavy apps needing strong consistency (e.g., financial data).
* */
// --------------------
// Model
// --------------------
data class TestData(val name: String, val age: Int, val phone: Int)

// --------------------
// Cache Entry with TTL
// --------------------
data class CacheEntry<V>(
    val value: V,
    val expiryTime: Long
) {
    fun isExpired(): Boolean = System.currentTimeMillis() > expiryTime
}

// --------------------
// Database Simulation
// --------------------
class Database<K, V> {
    private val db = ConcurrentHashMap<K, V>()

    suspend fun put(key: K, value: V) {
        delay(100) // simulate heavy DB write
        db[key] = value
        println("DB Updated: $key")
    }

    suspend fun get(key: K): V? {
        delay(50) // simulate heavy DB read
        return db[key]
    }
}

// --------------------
// Cache + Queue System
// --------------------
class CacheWithQueue<K, V>(
    private val database: Database<K, V>,
    private val ttlMillis: Long = 5000,
    private val scope: CoroutineScope
) {

    private val cache = ConcurrentHashMap<K, CacheEntry<V>>()

    // Queue for write operations
    private val writeQueue = Channel<Pair<K, V>>(Channel.UNLIMITED)
    var workerJob: Job? = null
    val sc = CoroutineScope(SupervisorJob() + Dispatchers.Default) // Custom private scope of class

    init {
        // Start Background work
        workerJob = scope.launch {
            for ((key, value) in writeQueue) {
                val expiryTime = System.currentTimeMillis() + ttlMillis   // Current time + time to live = expiry time
                cache[key] = CacheEntry(value, expiryTime)
                database.put(key, value)
                println("Worker processed: $key")
            }
        }
    }

    // Enqueue write
    @OptIn(DelicateCoroutinesApi::class)
    suspend fun put(key: K, value: V) {
        if (!writeQueue.isClosedForSend) {
            writeQueue.send(Pair(key, value))
        }
    }

    // Read with TTL and db fallback
    suspend fun get(key: K): V? {
        val entry = cache[key]
        if (entry != null && !entry.isExpired()) {
            println("Cache Hit: $key")
            return entry.value
        }
        if (entry != null && entry.isExpired()) {
            println("Cache Expired: $key")
            cache.remove(key)
        }
        println("Cache Miss: $key")
        val dbValue = database.get(key)
        if (dbValue != null) {
            println("DB Fallback: $key -> enqueue caching")
            writeQueue.send(key to dbValue)
        }

        return dbValue
    }

    suspend fun closeChannel() {
        println("Closing write queue in 5sec...")
        delay(5000)
        // 1 Stop accepting new writes
        writeQueue.close()
        // 2 Wait for worker to finish remaining items
        workerJob?.join()
        // 3 Cancel scope if needed
        // scope.cancel()  // This class does not own this scope by cancelling this it will throw exception

        println("Shutdown complete")
    }
}

// --------------------
// Test Harness
// --------------------
fun main() = runBlocking {

    val database = Database<String, TestData>()

    val cacheSystem = CacheWithQueue(
        database = database,
        ttlMillis = 5000,
        scope = this
    )

    val user1 = TestData("Alice", 30, 111)
    val user2 = TestData("Bob", 25, 222)

    println("=== Insert via Queue ===")
    cacheSystem.put("111", user1)
    delay(500)
    cacheSystem.put("222", user2)
    delay(500)
    cacheSystem.put("224", user2)
    delay(500)
    cacheSystem.put("225", user2)
    delay(500)
    cacheSystem.put("226", user2)
    delay(500)
    cacheSystem.put("227", user2)
    delay(500)
    cacheSystem.put("228", user2)
    delay(500)
    cacheSystem.put("229", user2)

    delay(1000) // allow worker to process


    println("\n=== First Fetch (should hit cache) ===")
    println(cacheSystem.get("111"))
    println(cacheSystem.get("222"))

    println("\n=== Wait 6 seconds (TTL expires) ===")
    delay(6000)

    println("\n=== Second Fetch (expired → DB fallback) ===")
    println(cacheSystem.get("111"))
    println(cacheSystem.get("222"))

    delay(500) // let worker re-cache

    println("\n=== Third Fetch (should hit cache again) ===")
    println(cacheSystem.get("111"))
    println(cacheSystem.get("222"))
    cacheSystem.closeChannel()
}
