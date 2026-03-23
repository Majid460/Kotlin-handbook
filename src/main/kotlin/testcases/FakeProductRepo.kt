package testcases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import java.io.IOException

object TestData {

    // Base factory - all parameters have sensible defaults
    // Override ONLY what the specific test cares about
    fun product(
        id: String = "test-id-1",
        title: String = "Test Product Title",
        price: Double = 100.9,
        isOnSale: Boolean = false,
        rating: Double = 3.5
    ) = Product(
        id = id,
        title = title,
        price = price,
        isOnSale = isOnSale,
        rating = rating
    )
    // CONVENIENCE BUILDERS
    // Named clearly so tests read like English

    // GetSaleProductsUseCase needs these two:
    fun saleProduct(id: String = "sale-1") =
        product(id = id, isOnSale = true)

    fun regularProduct(id: String = "regular-1") =
        product(id = id, isOnSale = false)

    // ProductService.getTopRated() needs these:
    fun highRatedProduct(id: String = "high-1") =
        product(id = id, rating = 4.5)  // above 4.0 threshold

    fun lowRatedProduct(id: String = "low-1") =
        product(id = id, rating = 2.5)  // below 4.0 threshold

    // ProductService.getCheapestOnSale() needs these:
    // Multiple sale products with different prices
    fun cheapSaleProduct(id: String = "cheap-1") =
        product(id = id, isOnSale = true, price = 3.99)

    fun expensiveSaleProduct(id: String = "expensive-1") =
        product(id = id, isOnSale = true, price = 49.99)

    // LIST BUILDERS
    fun productList(count: Int) = (1..count).map { i ->
        product(id = "id-$i", title = "Product $i")
    }

    fun mixedSaleList() = listOf(
        saleProduct("s1"),
        regularProduct("r1"),
        saleProduct("s2"),
        regularProduct("r2")
    )
}

class FakeProductRepository : ProductRepository {

    // This is the HEART of the fake
    // MutableStateFlow acts as your fake database
    // Whatever you put here is what the UseCase receives
    private val productsFlow = MutableStateFlow<List<Product>>(emptyList())

    var refreshCallCount = 0
    var shouldThrowOnGet = false
    var shouldThrowOnRefresh = false
    // your code here
    override fun getProducts(): Flow<List<Product>> {
        if (shouldThrowOnGet) {
            return flow { throw IOException("Network error") }
        }
        return productsFlow  // returns the flow directly
    }

    override suspend fun refreshProducts() {
        refreshCallCount++
        if (shouldThrowOnRefresh) {
            throw IOException("Refresh failed")
        }
    }
    // Test helper — this is how you FEED data into tests
    fun emitProducts(products: List<Product>) {
        productsFlow.value = products
    }

    fun reset() {
        productsFlow.value = emptyList()
        refreshCallCount = 0
        shouldThrowOnGet = false
        shouldThrowOnRefresh = false
    }
}