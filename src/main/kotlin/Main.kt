import coroutines.supervisorScopeFetchAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import coroutines.flows.UserImpl

suspend fun CoroutineScope.callAllCoroutines() {
    val job2 = async {
        supervisorScopeFetchAll()
    }
    awaitAll(job2)
}

fun main() {
    println("Starting a Coroutine")
    UserImpl()
//    runBlocking {
//        coroutineScope {
//            callAllCoroutines()
//        }
//    }
}
