package coroutines

/**
 * # Kotlin Coroutines — Complete Summary
 *
 * ---
 *
 * ## 1. What is a Coroutine
 *
 * A coroutine is a suspendable unit of work. It can pause (`suspend`) at any
 * point, free the thread for other work, and resume later when ready.
 * It does NOT block the thread — it only suspends itself.
 *
 * ```
 * Thread ────────────────────────────────────────────►
 *          │ suspend         │ resume
 *          └── thread free ──┘
 *              (other work)
 * ```
 *
 * ---
 *
 * ## 2. Coroutine Scope
 *
 * Every coroutine must live inside a scope. The scope controls lifetime.
 * Cancel the scope → cancel ALL children inside it.
 *
 * ```
 * CoroutineScope
 *     ├── coroutine 1
 *     ├── coroutine 2
 *     └── coroutine 3
 *          └── child coroutine
 * Cancel scope → ALL cancelled
 * ```
 *
 * ### Android built-in scopes
 *
 * | Scope | Dispatcher | Cancelled when |
 * |---|---|---|
 * | `viewModelScope` | `Main.immediate` | ViewModel cleared |
 * | `lifecycleScope` | `Main.immediate` | Lifecycle destroyed |
 * | `rememberCoroutineScope()` | `Main.immediate` | Composable leaves |
 * | `GlobalScope` | `Default` | Never — avoid in production |
 *
 * ---
 *
 * ## 3. Dispatchers — which thread runs the coroutine
 *
 * | Dispatcher | Thread | Use for |
 * |---|---|---|
 * | `Dispatchers.Main` | Android UI thread | UI updates, state |
 * | `Dispatchers.Main.immediate` | UI thread, no queue delay | viewModelScope default |
 * | `Dispatchers.IO` | Background thread pool | Network, DB, file I/O |
 * | `Dispatchers.Default` | CPU thread pool | Sorting, parsing, heavy computation |
 * | `Dispatchers.Unconfined` | Caller thread | Rarely used |
 *
 * ---
 *
 * ## 4. Coroutine Builders — start a new coroutine
 *
 * ### launch
 * Fire and forget. Returns [Job]. Does NOT return a value.
 * Returns to caller IMMEDIATELY — does not wait for coroutine body.
 * ```kotlin
 * viewModelScope.launch {
 *     val news = repository.getNews()   // suspends here
 *     _uiState.value = news             // resumes here
 * }
 * // caller carries on immediately — coroutine runs independently
 * ```
 *
 * ### async
 * Returns [Deferred]<T>. Use when you need a result.
 * Fires immediately like launch — but `.await()` suspends until result ready.
 * ```kotlin
 * val deferred = async { fetchUser() }   // fires immediately
 * val user = deferred.await()            // suspends until fetchUser() returns
 * ```
 *
 * ### runBlocking
 * Blocks the current thread until coroutine completes.
 * Only for tests and `main()` — NEVER on Android main thread.
 * ```kotlin
 * @Test
 * fun test() = runBlocking {
 *     val result = repository.getData()
 *     assertEquals("expected", result)
 * }
 * ```
 *
 * ---
 *
 * ## 5. Scope Builders — create a new scope boundary
 *
 * ### withContext
 * Switches dispatcher. Suspends caller until block completes.
 * Does NOT wait for `launch` fired inside it — they become orphaned.
 * ```kotlin
 * val result = withContext(Dispatchers.IO) {
 *     database.getUser()     // runs on IO thread
 * }
 * // back on original dispatcher — result available here
 * ```
 *
 * ### coroutineScope
 * Same dispatcher. Suspends caller until block AND all children complete.
 * One child fails → all siblings cancelled → exception propagates up.
 * ```kotlin
 * coroutineScope {
 *     launch { fetchUser() }    // child 1
 *     launch { fetchPosts() }   // child 2
 * }
 * // only reaches here when BOTH children are done
 * ```
 *
 * ### supervisorScope
 * Same as coroutineScope but children are independent.
 * One child fails → siblings keep running → exception stays isolated.
 * ```kotlin
 * supervisorScope {
 *     launch { throw Exception("fails") }   // child 1 fails
 *     launch { fetchPosts() }               // child 2 still runs ✓
 * }
 * ```
 *
 * ---
 *
 * ## 6. Sequential vs Parallel execution
 *
 * ### Sequential — direct suspend calls, one after another
 * ```kotlin
 * val user  = fetchUser()     // suspends — waits 300ms
 * val posts = fetchPosts()    // only starts after user done — waits 500ms
 * // total: 800ms
 * ```
 *
 * ### Parallel — multiple coroutines started before any await
 * ```kotlin
 * coroutineScope {
 *     val user  = async { fetchUser() }    // starts immediately
 *     val posts = async { fetchPosts() }   // starts immediately
 *     // both running at the same time
 *     println("${user.await()} ${posts.await()}")
 *     // total: max(300, 500) = 500ms
 * }
 * ```
 *
 * ---
 *
 * ## 7. launch inside scope builders — control flow
 *
 * `launch` ALWAYS returns immediately to the caller regardless of which
 * scope builder it lives inside. The difference is only whether the
 * scope builder WAITS for the launched coroutine to finish.
 *
 * ```
 * withContext {
 *     launch { }           ← returns immediately ✓
 *     println("runs now")  ← runs before launch body
 * }
 * // does NOT wait for launch ✗ — orphaned coroutine
 *
 * coroutineScope {
 *     launch { }           ← returns immediately ✓
 *     println("runs now")  ← runs before launch body
 * }
 * // WAITS for launch to finish ✓ — properly structured
 * ```
 *
 * ---
 *
 * ## 8. Structured Concurrency
 *
 * Every coroutine belongs to a scope and cannot outlive it.
 * Cancellation flows DOWN. Exceptions flow UP.
 *
 * ```
 * Scope (SupervisorJob — children independent)
 *     ├── child 1 fails ✗
 *     └── child 2 keeps running ✓
 *
 * Scope (regular Job — children linked)
 *     ├── child 1 fails ✗
 *     └── child 2 cancelled ✗
 * ```
 *
 * ---
 *
 * ## 9. CoroutineContext — combining elements with +
 *
 * ```kotlin
 * CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
 * //                    │                    │
 * //             children independent    main thread
 * //             scope stays alive       no queue delay
 * //             if one child fails      if already on Main
 * ```
 *
 * | Element | Controls |
 * |---|---|
 * | `Job()` | Lifecycle — one fails, all fail |
 * | `SupervisorJob()` | Lifecycle — failures isolated |
 * | `Dispatchers.Main` | Runs on UI thread |
 * | `Dispatchers.IO` | Runs on IO thread pool |
 * | `+` operator | Combines context elements |
 *
 * ---
 *
 * ## 10. Golden rules
 *
 * - `launch` always returns immediately — caller never waits
 * - Direct suspend call always suspends the current coroutine sequentially
 * - Parallel = multiple `async` or `launch` started BEFORE any `.await()`
 * - `withContext` = switch thread, sequential inside, orphans launches
 * - `coroutineScope` = same thread, waits for ALL children
 * - `supervisorScope` = same as coroutineScope but failures are isolated
 * - `viewModelScope` = runs on Dispatchers.Main `viewModelScope = CoroutineScope(
 *     SupervisorJob() + Dispatchers.Main )`
 * - Never use `launch` inside `withContext` — use `coroutineScope` instead
 * - Always use `Dispatchers.IO` for network/database work
 * - Never use `runBlocking` on Android main thread
 * - Cancel scope = cancel all children — no leaks
 */
fun coroutinesSummary() = Unit
