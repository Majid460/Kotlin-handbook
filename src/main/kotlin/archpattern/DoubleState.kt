package be.business.composefoundation.archpattern

import androidx.compose.runtime.external.kotlinx.collections.immutable.PersistentList

package com.reference.uistate

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────────────────────
// DOUBLE UI STATE PATTERN — Reference File
//
// WHY:
//   A sealed class alone forces you to duplicate shared fields (query, filters)
//   across every subclass. A data class alone can't express mutually exclusive
//   async states (you end up with isLoading + isError + isSuccess booleans that
//   can all be true at the same time).
//
//   The solution: combine both.
//     • LoadState  (sealed) → mutually exclusive async phases
//     • ScreenState (data)  → fields that always exist regardless of phase
//
// IMMUTABILITY NOTES:
//   • @Immutable tells the Compose compiler this class will never change
//     after creation → skips unnecessary recompositions → better performance.
//   • PersistentList (kotlinx-collections-immutable) is truly immutable at
//     runtime — unlike listOf() which can be cast and mutated.
//     Any "change" produces a brand-new list via structural sharing (fast).
//
// GRADLE DEPS NEEDED:
//   implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.7")
// ─────────────────────────────────────────────────────────────────────────────


// ─────────────────────────────────────────────────────────────────────────────
// 1. MODEL
// ─────────────────────────────────────────────────────────────────────────────

/** A single product shown in the list. */
@Immutable // tells Compose: this object never mutates → safe to skip recomposition
data class Product(
    val id: Int,
    val name: String,
    val price: Double
)


// ─────────────────────────────────────────────────────────────────────────────
// 2. LOAD STATE — sealed, mutually exclusive async phases
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Represents the async loading phase of the screen.
 *
 * These states are MUTUALLY EXCLUSIVE — the screen can only be in one at a time.
 * This is what sealed classes are designed for.
 *
 * Rule: always REPLACE entirely, never partially update.
 *   ✅ _state.update { it.copy(loadState = LoadState.Loading) }
 *   ❌ loadState.copy(...)  ← you'd never do this
 */
@Immutable
sealed class LoadState {

    /** Nothing has been requested yet. Initial state. */
    object Idle : LoadState()

    /** A network/db request is in flight. */
    object Loading : LoadState()

    /**
     * Request completed successfully.
     *
     * @param products Truly immutable list — safe for Compose, safe from casting.
     *   Any update (e.g. add item) returns a NEW list, original untouched.
     */
    data class Success(
        val products: PersistentList<Product> = persistentListOf()
    ) : LoadState()

    /**
     * Request failed.
     *
     * @param message Human-readable error to show the user.
     */
    data class Error(val message: String) : LoadState()
}


// ─────────────────────────────────────────────────────────────────────────────
// 3. SCREEN STATE — data class, owns ALL screen state
// ─────────────────────────────────────────────────────────────────────────────

/**
 * The single source of truth for the entire screen.
 *
 * Fields here exist REGARDLESS of which [LoadState] phase we are in.
 * The user can type in the search box whether we are loading, showing
 * results, or showing an error — so [query] lives here, not inside [LoadState].
 *
 * Use [copy] to change individual fields atomically via [MutableStateFlow.update]:
 * ```
 * _state.update { it.copy(query = "shoes") }
 * _state.update { it.copy(loadState = LoadState.Loading) }
 * ```
 *
 * @param query    Current search query. Always available regardless of load phase.
 * @param isRefreshing Whether a pull-to-refresh is in progress.
 * @param loadState    The current async phase of the screen.
 */
@Immutable
data class ScreenState(
    val query: String = "",
    val isRefreshing: Boolean = false,
    val loadState: LoadState = LoadState.Idle
)


// ─────────────────────────────────────────────────────────────────────────────
// 4. FAKE REPOSITORY — stands in for real network/db layer
// ─────────────────────────────────────────────────────────────────────────────

/** Simulates a repository. Replace with your real implementation. */
class ProductRepository {
    suspend fun getProducts(query: String): List<Product> {
        // Replace with real API / Room call
        return listOf(
            Product(1, "Nike Air Max", 120.0),
            Product(2, "Adidas Boost", 110.0),
            Product(3, "Puma RS-X", 95.0)
        ).filter { it.name.contains(query, ignoreCase = true) || query.isBlank() }
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// 5. VIEWMODEL
// ─────────────────────────────────────────────────────────────────────────────

/**
 * ViewModel for a search screen using the Double UI State pattern.
 *
 * State update rules:
 *   • [MutableStateFlow.update] is always used instead of direct `.value =`
 *     assignment — it is atomic (Compare-And-Set) so two coroutines updating
 *     state simultaneously can never overwrite each other.
 *   • [copy] preserves every untouched field automatically.
 *   • [LoadState] is always REPLACED entirely — never partially updated.
 *
 * Example flow:
 * ```
 * onQueryChanged("shoes")
 *   → ScreenState(query="shoes", loadState=Idle)      ← loadState untouched
 *
 * loadProducts()
 *   → ScreenState(query="shoes", loadState=Loading)   ← query untouched
 *   → ScreenState(query="shoes", loadState=Success([…]))
 *
 * onRefresh()
 *   → ScreenState(query="shoes", isRefreshing=true, loadState=Success([…]))
 *   → ScreenState(query="shoes", isRefreshing=false, loadState=Success([…new…]))
 * ```
 */
class ProductViewModel(
    private val repository: ProductRepository = ProductRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(ScreenState())

    /** Read-only state exposed to the UI. */
    val state = _state.asStateFlow()

    init {
        loadProducts()
    }

    // ── Public API (called from UI) ──────────────────────────────────────────

    /**
     * Called every time the user types in the search field.
     * Only [query] changes — [loadState] is untouched.
     */
    fun onQueryChanged(query: String) {
        _state.update { it.copy(query = query) }
        loadProducts() // re-fetch with new query
    }

    /**
     * Called when the user pulls to refresh.
     * Sets [isRefreshing] independently of [loadState].
     */
    fun onRefresh() {
        _state.update { it.copy(isRefreshing = true) }
        loadProducts()
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    private fun loadProducts() {
        viewModelScope.launch {

            // Replace loadState → Loading. query and isRefreshing untouched. ✅
            _state.update { it.copy(loadState = LoadState.Loading) }

            suspendRunCatching { repository.getProducts(_state.value.query) }
                .fold(
                    onSuccess = { products ->
                        _state.update {
                            it.copy(
                                // toPersistentList() → truly immutable at runtime
                                loadState = LoadState.Success(products.toPersistentList()),
                                isRefreshing = false
                            )
                        }
                    },
                    onFailure = { error ->
                        _state.update {
                            it.copy(
                                loadState = LoadState.Error(error.message ?: "Unknown error"),
                                isRefreshing = false
                            )
                        }
                    }
                )
        }
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// 6. SAFE COROUTINE HELPER
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Safe alternative to [runCatching] for suspend functions.
 *
 * [runCatching] catches [kotlinx.coroutines.CancellationException] which breaks
 * structured concurrency — the coroutine never learns it was cancelled.
 * This wrapper rethrows [CancellationException] so cancellation always propagates.
 *
 * Usage:
 * ```
 * suspendRunCatching { repository.getProducts() }
 *     .fold(
 *         onSuccess = { … },
 *         onFailure = { … }  // CancellationException will never land here
 *     )
 * ```
 */
suspend fun <T> suspendRunCatching(block: suspend () -> T): Result<T> {
    return try {
        Result.success(block())
    } catch (e: kotlinx.coroutines.CancellationException) {
        throw e          // always rethrow — structured concurrency depends on this
    } catch (e: Throwable) {
        Result.failure(e)
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// 7. COMPOSE UI — how to consume the double state
// ─────────────────────────────────────────────────────────────────────────────

/*
@Composable
fun ProductScreen(viewModel: ProductViewModel = viewModel()) {

    val state by viewModel.state.collectAsStateWithLifecycle()

    Column {

        // ── Always rendered — lives on ScreenState, not inside LoadState ──
        TextField(
            value = state.query,
            onValueChange = viewModel::onQueryChanged,
            placeholder = { Text("Search products…") }
        )

        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = viewModel::onRefresh
        ) {

            // ── Driven entirely by LoadState ──────────────────────────────
            when (val loadState = state.loadState) {

                is LoadState.Idle -> {
                    // show empty / welcome UI
                }

                is LoadState.Loading -> {
                    CircularProgressIndicator()
                }

                is LoadState.Success -> {
                    // loadState.products is PersistentList<Product>
                    // @Immutable on Product → Compose skips recomposition
                    // for items that haven't changed ✅
                    LazyColumn {
                        items(
                            items = loadState.products,
                            key = { it.id }
                        ) { product ->
                            Text("${product.name} — $${product.price}")
                        }
                    }
                }

                is LoadState.Error -> {
                    Text("Error: ${loadState.message}")
                }
            }
        }
    }
}
*/