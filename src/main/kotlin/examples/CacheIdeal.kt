package org.example.examples


import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.util.concurrent.ConcurrentHashMap

// --- Data classes for testing ---
data class TestData(
    val name: String,
    val age: Int,
    val phone: Int
)

data class CacheItemIdeal<T>(
    val data: T,
    val ttlMillis: Long
) {
    private val createdAt: Long = System.currentTimeMillis()
    fun isExpired(): Boolean = System.currentTimeMillis() > createdAt + ttlMillis
}

// Extension to convert any object to a CacheItem
fun <T> T.toCacheItem(ttlMillis: Long = 5000): CacheItemIdeal<T> = CacheItemIdeal(this, ttlMillis)

// --- CacheIdeal interface ---
interface CacheIdeal<K, V> {
    fun put(key: K, value: V)
    fun get(key: K): V?
    fun remove(key: K)
}

// Thread-safe CacheManager using ConcurrentHashMap
class CacheManagerImp<K, V> : CacheIdeal<K, V> {
    private val map = ConcurrentHashMap<K, V>()

    override fun put(key: K, value: V) {
        map[key] = value
        println("CacheIdeal Updated: $key")
    }

    override fun get(key: K): V? = map[key]

    override fun remove(key: K) {
        map.remove(key)
        println("CacheIdeal Removed: $key")
    }
}

// --- DB interface ---
interface DBIdeal<K, V> {
    suspend fun put(key: K, value: V, ttlMillis: Long? = null)
    suspend fun get(key: K): V?
}

// --- Database with cacheIdeal integration ---
class DataBaseImp<K, V>(
    private val cacheIdeal: CacheIdeal<K, CacheItemIdeal<V>>,
    private val defaultTTL: Long = 5000 // default TTL in ms
) : DBIdeal<K, V> {

    private val db = ConcurrentHashMap<K, V>()

    override suspend fun put(key: K, value: V, ttlMillis: Long?) {
        val ttl = ttlMillis ?: defaultTTL
        cacheIdeal.put(key, value.toCacheItem(ttl))
        delay(100) // simulate DB write
        db[key] = value
        println("DB Updated: $key")
    }

    override suspend fun get(key: K): V? {
        var cacheItem = cacheIdeal.get(key)

        if (cacheItem != null && cacheItem.isExpired()) {
            println("Removing expired cacheIdeal: $key")
            cacheIdeal.remove(key)
            cacheItem = null
        }

        return if (cacheItem != null) {
            println("CacheIdeal Hit: $key")
            cacheItem.data
        } else {
            println("CacheIdeal Miss: $key")
            val dbValue = db[key]
            if (dbValue != null) {
                println("DB Fallback: $key -> caching")
                cacheIdeal.put(key, dbValue.toCacheItem(defaultTTL))
            }
            dbValue
        }
    }
}

// --- Test harness ---
fun main() = runBlocking {
    // Thread-safe cache storing CacheItemIdeal<TestData>
    val cache = CacheManagerImp<String, CacheItemIdeal<TestData>>()

    // Database works with raw TestData but caches CacheItemIdeal<TestData>
    val db = DataBaseImp(cache, defaultTTL = 5000)

    val user1 = TestData("Alice", 30, 111)
    val user2 = TestData("Bob", 25, 222)

    println("=== Insert users into DB ===")
    db.put("111", user1)
    db.put("222", user2)

    println("\n=== First fetch (should hit cache) ===")
    println(db.get("111"))
    println(db.get("222"))

    println("\n=== Wait 6 seconds (TTL expires) ===")
    delay(6000)

    println("\n=== Second fetch (cache expired, fallback to DB) ===")
    println(db.get("111")) // cache miss, DB fallback
    println(db.get("222")) // cache miss, DB fallback

    println("\n=== Third fetch (immediate, should hit cache again) ===")
    println(db.get("111"))
    println(db.get("222"))
}
