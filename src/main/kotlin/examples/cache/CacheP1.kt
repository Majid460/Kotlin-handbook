package org.example.examples.cache

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.runBlocking
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// Cache = Temporary fast storage to reduce load on main system.

/*
* Instead of hitting:
    Database
    External API
    Microservice
*
You store frequently used data in:
    Memory (Redis, in-memory map)
    Distributed cache
*
Example real-world systems:
    Redis
    Memcached
* */
// Task1
/*
* TASK 1 — Build a Simple Cache with TTL
Scenario
You have a function:
    fun fetchUserFromDatabase(id: Int): String
      It simulates heavy DB call (use Thread.sleep(1000)).
    Your Task:
    Create a Cache<K, V> class
    Store:
        value
        timestamp
        Add TTL (e.g. 5 seconds)
    If data exists and not expired → return cached
    Otherwise → fetch from DB and cache it
* */

/*
* @Address
* */
enum class PaymentStatus {
    PAID,
    PENDING,
    FAILED,
    NOT_PAID
}

data class Address(
    val houseNo: String,
    val street: String,
    val town: String,
    val city: String,
    val state: String,
    val country: String,
    val postCode: String,
) {
    constructor() : this("", "", "", "", "", "", "")
}

private fun setAddress(block: Address.() -> Unit): Address {
    return Address().apply(block)
}

/*
* @Addresses
* */
data class Addresses(
    val deliveryAdd: Address,
    val billingAddress: Address
) {
    constructor() : this(Address(), Address())
}

/*
* @order
* */
data class Order(
    val orderId: String,
    val customerName: String,
    val customerId: String,
    val addresses: Addresses,
    val paymentStatus: PaymentStatus,
    val orderDate: Long
) {
    constructor() : this("", "", ",", Addresses(), PaymentStatus.NOT_PAID, Instant.now().toEpochMilli())
}

// ExtensionFunction for Long to String Format Date
private fun Long.toFormatDate(): String {
    val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a")
        .withZone(ZoneId.systemDefault())
    return formatter.format(Instant.ofEpochMilli(this))
}

data class TestData(
    val name: String,
    val age: Int,
    val phone: Int,
    val addresses: Addresses = Addresses(),
    val orders: List<Order> = emptyList()
){
    fun fromCacheItem(cacheItem: CacheItem<TestData>): TestData {
        return TestData(
            cacheItem.data.name,
            cacheItem.data.age,
            cacheItem.data.phone,
            cacheItem.data.addresses
        )
    }
}
// Extension function to convert any object to CacheItem
private fun <T> T.toCacheItem(ttlSeconds: Long = 3600): CacheItem<T> {
    return CacheItem(this, ttlSeconds)
}
data class CacheItem<T>(
    val data: T,
    val ttlSeconds: Long
) {
    private val createdAt: Long = Instant.now().epochSecond

    fun isExpired(): Boolean {
        val now = Instant.now().epochSecond
        return now > createdAt + ttlSeconds
    }
}

// Design System
interface Cache<K, V> {
    fun put(key: K, value: V)
    fun get(key: K): V?
    fun remove(key: K)
}

class CacheManager<K, V>() : Cache<K, V> {
    private val _cache = MutableStateFlow<Map<K, V>>(emptyMap())
    val cache: StateFlow<Map<K, V>>
        get() = _cache

    override fun put(key: K, value: V) {
        _cache.value = _cache.value + (key to value)
        println("Cache Updated: $key")
    }

    override fun get(key: K): V? {
        return cache.value[key]
    }

    override fun remove(key: K) {
        _cache.value = _cache.value - key
        println("Cache Removed: $key")
    }

}

private interface DB<K, V> {
    suspend fun put(key: K, value: V)
    suspend fun get(key: K): V?
}

private class DataBase<K, V>(val cache: Cache<K, CacheItem<V>>, private val defaultTTL: Long = 5 ) : DB<K, V> {
    private val _db = MutableStateFlow<Map<K, V>>(emptyMap())
    val db: StateFlow<Map<K, V>>
        get() = _db

    override suspend fun put(key: K, value: V) {
        val cacheItem = value.toCacheItem(defaultTTL)
        cache.put(key, cacheItem)
        delay(100) // simulate DB write
        _db.value = _db.value + (key to value)
        println("DB Updated: $key")
    }
    override suspend fun get(key: K): V? {
        val cacheItem = cache.get(key)
        if (cacheItem != null && cacheItem.isExpired()) {
            println("Removing expired cache: $key")
            cache.remove(key) // safe removal
        }

        val validCacheItem = cache.get(key)
        return if (validCacheItem != null && !validCacheItem.isExpired()) {
            println("Cache Hit: $key")
            validCacheItem.data
        } else {
            println("Cache Miss: $key")
            val dbValue = _db.value[key]
            if (dbValue != null) {
                println("DB Fallback: $key -> caching")
                cache.put(key, dbValue.toCacheItem(defaultTTL))
            }
            dbValue
        }
    }
}

private fun fetchFromDB() = runBlocking {
    // CacheManager stores CacheItem<TestData>
    val cache: Cache<String, CacheItem<TestData>> = CacheManager()

    // Database works with raw TestData
    val db = DataBase(cache)

    // Example TestData
    val testData = TestData("Test1", 22, 121222222)

    // Put into DB (auto wraps into CacheItem)
    db.put("1234567890", testData)

    // Get from DB (auto unwraps CacheItem)
    val retrieved = db.get("1234567890")
    println("Retrieved: $retrieved")
}

private fun main() = runBlocking {
    val cache = CacheManager<String, CacheItem<TestData>>()
    val db = DataBase(cache, defaultTTL = 5) // TTL 5 sec for testing

    val user1 = TestData("Alice", 30, 111)
    val user2 = TestData("Bob", 25, 222)

    println("=== Insert users into DB ===")
    db.put("111", user1)
    db.put("222", user2)

    println("\n=== First fetch (should hit cache) ===")
    val d = db.get("111")
    println(d)
    db.get("222")


    println("\n=== Wait 6 seconds (TTL expires) ===")
    delay(6000)

    println("\n=== Second fetch (cache expired, fallback to DB) ===")
    db.get("111") // cache miss, DB fallback and re-cache
    db.get("222") // cache miss, DB fallback and re-cache

    println("\n=== Third fetch (immediate, should hit cache again) ===")
    val d11 = db.get("111")
    println(d11)
    val d12 = db.get("222")
    println(d12)

}
//fun main() {
//    fetchFromDB()
//}
