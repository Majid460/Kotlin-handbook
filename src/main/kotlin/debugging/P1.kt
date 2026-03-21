package debugging

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlin.time.Duration.Companion.milliseconds


data class User(
    val id: String,
    val name: String
)

class UserRepository {

    fun getUser(): User? {
        return null
    }

}

class ProfileViewModel(
    private val repository: UserRepository
) {

    // val userName = MutableLiveData<String>()
    val userName = MutableStateFlow<String>("")
    val searchQueryFlow: Flow<Int> = flowOf(11, 2, 2, 2, 3, 2, 2)
    suspend fun loadUser() {
        val user = repository.getUser()
        userName.value = user?.name ?: "default"
        user?.let {
            userName.value = it.name
        }
        coroutineScope {
            searchQueryFlow
                .buffer()
                .debounce(300.milliseconds)
                .collectLatest { query ->
//                    val result = repository.search(query)
//                    _state.value = result
                }
        }

        // userName.value = user!!.name
        // --> Issue is !!. Repo returns null it will throw null pointer exception
    }
}

