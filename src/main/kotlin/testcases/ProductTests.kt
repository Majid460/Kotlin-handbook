package testcases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

// Model
data class Product(
    val id: String,
    val title: String,
    val price: Double,
    val isOnSale: Boolean,
    val rating: Double
)

// Repository Interface
interface ProductRepository {
    fun getProducts(): Flow<List<Product>>
    suspend fun refreshProducts()
}

// UseCase — this is what you are testing
class GetSaleProductsUseCase(
    private val repository: ProductRepository
) {
    operator fun invoke(): Flow<List<Product>> {
        return repository.getProducts()
            .map { products -> products.filter { it.isOnSale } }
            .distinctUntilChanged()
    }
}

// Service — also needs testing
class ProductService(
    private val repository: ProductRepository
) {
    suspend fun getTop():Flow<List<Product>>{
        return repository.getProducts().flatMapLatest { products ->
                flow {
                    products
                }
            }
        }
    suspend fun getTopRated(): List<Product> {
        return repository.getProducts()
            .first()
            .filter { it.rating >= 4.0 }
            .sortedByDescending {it.rating }
    }

    suspend fun getCheapestOnSale(): Product? {
        return repository.getProducts()
            .first()
            .filter { it.isOnSale }
            .minByOrNull { it.price }
    }

    suspend fun refresh() {
        repository.refreshProducts()
    }
}