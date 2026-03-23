package collections

import jdk.nashorn.internal.ir.annotations.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import testcases.Product

/**
 * ### kotlinx-collections-immutable
 * ```
 * Provides truly `immutable and persistent` collection types for
 * Kotlin. While Kotlin’s standard library offers read-only collection interfaces like List and
 * `Set`, these are merely views that hide mutability rather than guarantee it. kotlinx-collections immutable
 * fills this gap with collections that cannot be modified after creation, providing
 * thread safety and predictable behavior.
 * The Problem with Read-Only Collections
 * Kotlin’s `listOf()`, setOf(), and mapOf()` return read-only collections, but they don’t guarantee
 * “immutability”. The underlying collection might still be mutable, and the read-only
 * interface is just a restricted view:
 * ```
 * */
fun immutable() {
    val immutableList = persistentListOf<Int>(1, 2, 3)
    // Cannot cast to mutable - it's a completely different type
    // (immutableList as MutableList<String>) // ClassCastException!

    val newList =
        immutableList.add(4) // It does not change in original even it creates another list -> [1, 2, 3, 4]
    val remove = immutableList.remove(1) // It still performed on the original returned -> [2, 3]
    println(newList)
    val updated = immutableList.set(1, 1) // return a new list with new node changed
    println(remove)
}

/**
 * ### Persistent Collections
 * The library implements persistent data structures, which efficiently share structure between
 * the original and modified versions. This makes modification operations much faster than
 * copying the entire collection:
 *
 * PersistentList extends ImmutableList
 * Adds modification operations that return NEW lists
 * Original list NEVER changes

 *  */

fun demonstratePersistence() {
    // Create a persistent list with 1000 elements
    val original = (1..1000).toPersistentList()

    // Adding an element doesn't copy all 1000 elements
    // Instead, it shares structure with the original
    val modified = original.add(1001)

    // Both lists exist simultaneously with minimal memory overhead
    println(original.size) // 1000
    println(modified.size) // 1001
}

/**
 *
 *
 * ---
 *
 * ## ImmutableList vs PersistentList
 * ```
 * ImmutableList:
 * → Guarantees contents never change
 * → Read-only operations only
 * → No way to produce modified versions
 * → Used when: you just need stability guarantee
 *
 * PersistentList:
 * → Extends ImmutableList
 * → Same immutability guarantee
 * → ADDS modification operations returning new lists
 * → Used when: you need to produce modified versions efficiently
 * → PersistentList IS-A ImmutableList*/
// ImmutableList — just reading
fun displayProducts(products: ImmutableList<Product>) {
    products.forEach { it }  // read only
}

// PersistentList — reading AND producing new versions
fun addProduct(
    products: PersistentList<Product>,
    newProduct: Product
): PersistentList<Product> {
    return products.add(newProduct)  // returns new list
}

/**
 * ```
 * val original = persistentListOf("a", "b", "c", "d", "e")
 * val modified = original.add("f")
 *
 * // Memory — SHARED structure:
 * //
 * //          [root node]
 * //         /           \
 * //    [node 1]        [node 2]
 * //    /     \         /     \
 * //  [a]    [b]     [c]    [d]
 * //
 * // Both original AND modified point to SAME nodes
 * // Only the NEW or CHANGED nodes are created
 * // Everything else is SHARED
 * ```
 *
 * ---
 *
 * ## How Structural Sharing Works — Tree Structure
 *
 * PersistentList uses a data structure called **Hash Array Mapped Trie (HAMT)**:
 * ```
 * Original list: [a, b, c, d, e, f, g, h]
 *
 * Internal tree:
 *                     [ROOT]
 *                    /      \
 *               [NODE A]    [NODE B]
 *               /    \      /    \
 *             [a]   [b]  [c]   [d]
 *                         \
 *                     [NODE C]
 *                     /    \
 *                   [e]   [f]
 * ```
 *
 * ### Now add item "i":
 * ```
 * Modified list: [a, b, c, d, e, f, g, h, i]
 *
 *                     [NEW ROOT]         ← only this is new
 *                    /          \
 *               [NODE A]    [NEW NODE B] ← only this is new
 *               /    \      /    \
 *             [a]   [b]  [c]   [d]
 *                         \        \
 *                     [NODE C]   [NEW NODE] ← only this is new
 *                     /    \       \
 *                   [e]   [f]     [i] ← only this is new
 *
 * Nodes shared: NODE A, [a], [b], [c], [d], NODE C, [e], [f]
 * Nodes created: NEW ROOT, NEW NODE B, NEW NODE, [i]
 *
 * 8 items in list → only 4 new allocations needed!
 * ```
 *
 * ---
 *
 * ## Performance Comparison
 * ```
 * Operation          Regular List        PersistentList
 * ─────────────────────────────────────────────────────
 * add(item)          O(n) copy all      O(log n) structural share
 * remove(item)       O(n) copy all      O(log n) structural share
 * set(index, item)   O(n) copy all      O(log n) structural share
 * get(index)         O(1)               O(log n) slightly slower
 * iterate            O(n)               O(n) same
 * memory for modify  O(n) full copy     O(log n) shared nodes
 * ```
 * ```
 * For a list of 1,000,000 items:
 *
 * Regular list add:
 * → copies all 1,000,000 items
 * → ~8MB memory allocation
 * → O(n) time
 *
 * PersistentList add:
 * → creates ~20 new nodes (log₃₂ of 1,000,000 ≈ 4 levels)
 * → ~160 bytes memory allocation
 * → O(log n) time
 * → 50,000x less memory used*/

// The Wrong Way — Regular List in State
// BAD — unstable, causes unnecessary recomposition
data class ProductScreenStateWrong(
    val isLoading: Boolean = false,
    val products: List<Product> = emptyList(),  // unstable
    val error: String? = null
)

/** ```Kotlin
 * @Composable
 * fun ProductScreen(state: ProductScreenStateWrong) {
 * ProductList(products = state.products)
 * // ProductList recomposes every time ProductScreen recomposes
 * // even if products list didn't change
 * // because List is unstable
}
 */
// GOOD — stable, enables skip optimization
@Immutable
data class ProductScreenState(
    val isLoading: Boolean = false,
    val products: ImmutableList<Product> = persistentListOf(),  // stable
    val error: String? = null
)

/** ```Kotlin
 * @Composable
 * fun ProductScreen(state: ProductScreenState) {
 * ProductList(products = state.products)
 * // ProductList can be SKIPPED if products reference unchanged
 * // Compose trusts ImmutableList completely
}*/
fun goodWay() = Unit

// Use in View Model
/**
 * ```kotlin
 * class ProductViewModel : ViewModel() {
 *
 *     private val _state = MutableStateFlow(ProductScreenState())
 *     val state: StateFlow<ProductScreenState> = _state.asStateFlow()
 *
 *     fun loadProducts(newProducts: List<Product>) {
 *         _state.update { currentState ->
 *             currentState.copy(
 *                 // Convert to PersistentList when storing
 *                 products = newProducts.toPersistentList(),
 *                 isLoading = false
 *             )
 *         }
 *     }
 *
 *     fun addProduct(product: Product) {
 *         _state.update { currentState ->
 *             currentState.copy(
 *                 // PersistentList.add returns NEW list
 *                 // Old list unchanged
 *                 // Structural sharing used internally
 *                 products = currentState.products.add(product)
 *             )
 *         }
 *     }
 *
 *     fun removeProduct(productId: String) {
 *         _state.update { currentState ->
 *             currentState.copy(
 *                 products = currentState.products
 *                     .removeAll { it.id == productId }
 *             )
 *         }
 *     }
 *
 *     fun updateProduct(updated: Product) {
 *         _state.update { currentState ->
 *             val index = currentState.products
 *                 .indexOfFirst { it.id == updated.id }
 *             if (index == -1) return@update currentState
 *             currentState.copy(
 *                 // set returns NEW list with item replaced
 *                 // structural sharing — only changed node created
 *                 products = currentState.products.set(index, updated)
 *             )
 *         }
 *     }
 * }
 *
 * */

fun main() {
    immutable()
    demonstratePersistence()
}

/**
 * "Regular Kotlin List is an interface that could be backed by a mutable list, so Compose marks it as unstable and recomposes every time. ImmutableList from kotlinx-collections-immutable provides a compile-time immutability guarantee that Compose trusts completely, enabling skip optimization. PersistentList extends ImmutableList and adds modification operations that use structural sharing — a tree-based approach where only changed nodes are created and unchanged nodes are shared between old and new versions. This means unchanged list items have the same object reference in the new list, so Compose's reference equality check skips recomposing those items. Combined this gives you both Compose stability for performance and efficient O(log n) list updates instead of O(n) full copies.*/
object Summary