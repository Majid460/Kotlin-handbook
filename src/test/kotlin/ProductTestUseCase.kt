import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import testcases.FakeProductRepository
import testcases.GetSaleProductsUseCase
import testcases.TestData

class GetSaleProductsUseCaseTest {

    // 1. Create fake repository
    private val fakeRepository = FakeProductRepository()

    // 2. Inject fake into UseCase
    //    UseCase thinks it's talking to REAL repository
    //    but actually talking to fake
    private val useCase = GetSaleProductsUseCase(fakeRepository)

    @Before
    fun setup() {
        fakeRepository.reset() // clean state before each test
    }

    @Test
    fun `returns only sale products from mixed list`() = runTest  {

        // ARRANGE
        // Use TestData to create meaningful data
        // Feed it into fake repository
        fakeRepository.emitProducts(TestData.mixedSaleList())
        //                          ↑
        //               This puts data into MutableStateFlow
        //               UseCase will receive this when it calls getProducts()

        // ACT
        // UseCase calls repository.getProducts()
        // Gets our fake data back
        // Filters it
        val result = useCase().first()
        println(result)
        //                     ↑
        //           first() collects the first emission from the Flow

        // ASSERT
        assertEquals(2, result.size)
        assertTrue(result.all { it.isOnSale })
    }
}

/**

---

## The Full Data Journey Visualized
```
TEST CODE:
fakeRepository.emitProducts(TestData.mixedSaleList())
│
│  puts this list into MutableStateFlow:
│  [saleProduct, regularProduct, saleProduct, regularProduct]
│
▼
FAKE REPOSITORY:
productsFlow.value = [saleProduct, regularProduct, saleProduct, regularProduct]
│
│  UseCase calls repository.getProducts()
│  fake returns productsFlow
│
▼
USE CASE:
.map { products -> products.filter { it.isOnSale } }
│
│  filters the list
│  keeps only isOnSale = true
│
▼
RESULT COLLECTED IN TEST:
[saleProduct, saleProduct]
│
▼
ASSERT:
assertEquals(2, result.size)
assertTrue(result.all { it.isOnSale }) */
object ProductUseCase