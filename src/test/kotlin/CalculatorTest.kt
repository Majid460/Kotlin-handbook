
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.DefaultAsserter.assertNotNull
import kotlin.test.DefaultAsserter.assertNull
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertSame

class CalculatorTest {


    @Test
    fun testAdd() {
        val calculator = Calculator()
        val result = calculator.add(2, 3)
        assertEquals(5, result)
    }
    @Test
    fun `demonstrating all key assertions`() {
        // Equality
        assertEquals(5, 2 + 3)
        assertNotEquals(6, 2 + 3)

        // Booleans
        assertTrue(5 > 3)
        assertFalse(5 < 3)

        // Null checks
        val name: String? = "Alice"
        assertNotNull(",",name)

        val empty: String? = null
        assertNull("",empty)

        // Same object in memory (reference equality)
        val list = listOf(1, 2, 3)
        assertSame(list, list)
    }

}
class Calculator {
    fun add(a: Int, b: Int): Int {
        return a + b
    }
}
// Repo test
data class User(val id: Int,val name: String)
class UserRepository{
    fun findById(id: Int): User {
        return User(1,"Alice")
    }
}
class UserService(private val userRepository: UserRepository) {
    fun getUsername(id: Int): String {
        return userRepository.findById(id).name
    }
}
class TestUserService{
    lateinit var userRepository: UserRepository
    lateinit var userService: UserService
    @BeforeTest
    fun `setup`(){
        userRepository = mockk<UserRepository>()
        userService = UserService(userRepository)
    }
    @AfterTest
    fun `teardown`() {
        clearMocks(userRepository)
    }
    @Test
    fun `should return username with valid id`(){
        // Tell the mockk to return for id
        every { userRepository.findById(1) } returns User(1,"Alice")
        assertEquals("Alice",userService.getUsername(1))
        // Verify the dependency was actually called
        verify { userRepository.findById(1) }

    }
}