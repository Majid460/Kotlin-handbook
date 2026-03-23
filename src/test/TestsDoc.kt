@file:Suppress("unused", "UnusedImport", "ClassName")

/*
 * ════════════════════════════════════════════════════════════════════════
 *  ANDROID TESTING — COMPLETE REFERENCE GUIDE
 *  Author  : Majid
 *  Purpose : Production-grade reference covering every aspect of Android
 *            testing. Unit tests, Integration tests, and Espresso UI tests.
 *            Includes setup, patterns, fake repositories, coroutine testing,
 *            Flow testing with Turbine, Hilt injection, and best practices.
 * ════════════════════════════════════════════════════════════════════════
 *
 *  TABLE OF CONTENTS
 *  ─────────────────────────────────────────────────────────────────────
 *  SECTION 1  →  Testing Philosophy and Pyramid
 *  SECTION 2  →  Gradle Setup and Dependencies
 *  SECTION 3  →  Unit Tests Complete Guide
 *               3a. JUnit4 Basics
 *               3b. MainDispatcherRule
 *               3c. Fake Repository Pattern
 *               3d. Test Data Builders
 *               3e. UseCase Tests
 *               3f. ViewModel Tests with Turbine
 *               3g. Mapper Tests
 *               3h. MockK when to use mocks
 *  SECTION 4  →  Integration Tests Complete Guide
 *               4a. Room DAO Tests
 *               4b. Repository and Room Tests
 *  SECTION 5  →  Espresso UI Tests Complete Guide
 *               5a. Basic Espresso
 *               5b. RecyclerView Actions
 *               5c. Idling Resources
 *               5d. Hilt and Espresso
 *  SECTION 6  →  Test Naming Conventions
 *  SECTION 7  →  Fake vs Mock Decision Guide
 *  SECTION 8  →  Common Mistakes and Fixes
 *  SECTION 9  →  Quick Reference Cheat Sheet
 * ════════════════════════════════════════════════════════════════════════
 */


// ════════════════════════════════════════════════════════════════════════
//  SECTION 1 — TESTING PHILOSOPHY AND PYRAMID
// ════════════════════════════════════════════════════════════════════════

/**
 * # Testing Philosophy
 *
 * ## The Core Principle
 * Tests exist to give you confidence that your code works correctly.
 * Not just for catching bugs but for enabling safe refactoring,
 * documenting intended behaviour, and shipping features faster.
 *
 * ## The Testing Pyramid
 * ```
 *           +-------------------+
 *           |  E2E / ESPRESSO   |  <- Few. Slow. Full user flows.
 *           |  ~10% of tests    |     Device or Emulator required.
 *           +-------------------+
 *           |   INTEGRATION     |  <- Medium. Multiple components.
 *           |   ~20% of tests   |     Room + Repo, ViewModel + UseCase.
 *           +-------------------+
 *           |    UNIT TESTS     |  <- Many. Fast. Single class.
 *           |   ~70% of tests   |     Pure JVM. No Android needed.
 *           +-------------------+
 * ```
 *
 * ## Why This Distribution?
 * ```
 * Unit Test    runs in ~5ms    run 1000 tests in 5 seconds
 * Integration  runs in ~500ms  run 100 tests in 50 seconds
 * Espresso     runs in ~3s     run 10 tests in 30 seconds
 * ```
 * More unit tests equals faster CI pipeline equals faster development.
 *
 * ## The AAA Pattern - Every Test Follows This
 * ```kotlin
 * // ARRANGE - set up the world (data, fakes, initial state)
 * // ACT     - call the method being tested
 * // ASSERT  - verify the outcome
 * ```
 *
 * ## What Makes a Good Test - FIRST Principles
 * ```
 * F - Fast       : runs in milliseconds
 * I - Isolated   : does not depend on other tests
 * R - Repeatable : same result every single run
 * S - Self-checking : pass or fail automatically no manual inspection
 * T - Timely     : written alongside the code not months later
 * ```
 */
object TestingPhilosophy


// ════════════════════════════════════════════════════════════════════════
//  SECTION 2 — GRADLE SETUP AND DEPENDENCIES
// ════════════════════════════════════════════════════════════════════════

/**
 * # Gradle Setup
 *
 * ## Test Source Sets in Android
 * ```
 * src/
 *   main/        production code
 *   test/        unit tests (runs on JVM - no Android framework)
 *   androidTest/ instrumented tests (runs on device or emulator)
 * ```
 *
 * ## Unit Test Dependencies added to src/test/
 * ```kotlin
 * dependencies {
 *     // JUnit4 - test runner and basic assertions
 *     testImplementation("junit:junit:4.13.2")
 *
 *     // Kotlin coroutines test utilities
 *     // Provides runTest, TestCoroutineDispatcher, advanceUntilIdle
 *     testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
 *
 *     // Turbine - Flow testing library by CashApp
 *     // Provides Flow.test { awaitItem(), awaitComplete(), awaitError() }
 *     testImplementation("app.cash.turbine:turbine:1.0.0")
 *
 *     // MockK - Kotlin-first mocking library
 *     // Provides mockk(), every {}, coEvery {}, verify {}, coVerify {}
 *     testImplementation("io.mockk:mockk:1.13.8")
 *
 *     // Google Truth - more readable assertions
 *     // assertThat(value).isEqualTo(expected) vs assertEquals(expected, value)
 *     testImplementation("com.google.truth:truth:1.1.5")
 *
 *     // AndroidX ViewModel testing support
 *     testImplementation("androidx.arch.core:core-testing:2.2.0")
 * }
 * ```
 *
 * ## Integration and Espresso Dependencies added to src/androidTest/
 * ```kotlin
 * dependencies {
 *     // AndroidX Test base for instrumented testing
 *     androidTestImplementation("androidx.test.ext:junit:1.1.5")
 *     androidTestImplementation("androidx.test:runner:1.5.2")
 *     androidTestImplementation("androidx.test:rules:1.5.0")
 *
 *     // Espresso - UI interaction and verification
 *     androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
 *     androidTestImplementation("androidx.test.espresso:espresso-contrib:3.5.1")
 *
 *     // Room testing - in-memory database for integration tests
 *     androidTestImplementation("androidx.room:room-testing:2.6.0")
 *
 *     // Hilt testing - inject fakes into instrumented tests
 *     androidTestImplementation("com.google.dagger:hilt-android-testing:2.48")
 *     kaptAndroidTest("com.google.dagger:hilt-android-compiler:2.48")
 *
 *     // Coroutines test support for instrumented tests
 *     androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
 * }
 * ```
 */
object GradleSetup


// ════════════════════════════════════════════════════════════════════════
//  SECTION 3 — UNIT TESTS
// ════════════════════════════════════════════════════════════════════════

/**
 * # Unit Tests Complete Guide
 *
 * ## Definition
 * A unit test verifies ONE class in complete isolation.
 * Every dependency is replaced with a fake or mock.
 * Runs on the JVM with zero Android framework involvement.
 *
 * ## File Location
 * ```
 * src/test/java/com/app/app/
 * ```
 *
 * ## Key Annotations
 * ```kotlin
 * @Test        marks a method as a test case
 * @Before      runs before EACH test - use for setup
 * @After       runs after EACH test - use for cleanup
 * @BeforeClass runs ONCE before ALL tests in the class
 * @AfterClass  runs ONCE after ALL tests in the class
 * @Ignore      skips this test - add a reason why in the string
 * ```
 *
 * ## Basic JUnit4 Test Structure
 * ```kotlin
 * class MyClassTest {
 *
 *     // System Under Test - the class being tested
 *     private lateinit var sut: MyClass
 *
 *     @Before
 *     fun setup() {
 *         // Runs before every @Test method
 *         sut = MyClass()
 *     }
 *
 *     @After
 *     fun teardown() {
 *         // Runs after every @Test method
 *         // Clean up resources here
 *     }
 *
 *     @Test
 *     fun `does something correctly`() {
 *         // ARRANGE
 *         val input = "test"
 *
 *         // ACT
 *         val result = sut.process(input)
 *
 *         // ASSERT
 *         assertEquals("expected", result)
 *     }
 * }
 * ```
 *
 * ## JUnit4 Assertion Methods
 * ```kotlin
 * assertEquals(expected, actual)       // values are equal
 * assertNotEquals(unexpected, actual)  // values are NOT equal
 * assertTrue(condition)                // condition is true
 * assertFalse(condition)               // condition is false
 * assertNull(value)                    // value is null
 * assertNotNull(value)                 // value is not null
 * assertSame(expected, actual)         // same object reference
 * assertThrows<Exception> { block }    // block throws exception
 * ```
 *
 * ## Google Truth Assertions - More Readable
 * ```kotlin
 * // JUnit style - harder to read
 * assertEquals(2, result.size)
 * assertTrue(result.all { it.isBreaking })
 *
 * // Truth style - reads like English
 * assertThat(result).hasSize(2)
 * assertThat(result).isNotEmpty()
 * assertThat(result.all { it.isBreaking }).isTrue()
 * assertThat(result).contains(expectedItem)
 * assertThat(result).doesNotContain(unexpectedItem)
 * assertThat(name).startsWith("By ")
 * assertThat(preview).endsWith("...")
 * assertThat(value).isEqualTo(expected)
 * assertThat(value).isGreaterThan(0)
 * ```
 */
object UnitTestGuide


/**
 * # MainDispatcherRule - Required for Coroutine Tests
 *
 * ## The Problem Without This Rule
 * ```kotlin
 * // This CRASHES in unit tests:
 * viewModelScope.launch {
 *     // Dispatchers.Main is not available outside Android
 * }
 * // Exception: Module with the Main dispatcher had failed to initialize
 * ```
 *
 * ## Why It Crashes
 * Dispatchers.Main requires an Android Looper which does not exist
 * on the JVM. Unit tests run on the JVM with no Android and no Looper.
 *
 * ## The Fix - MainDispatcherRule
 * Replaces Dispatchers.Main with a TestCoroutineDispatcher
 * that works on the JVM. Applied as a JUnit @get:Rule.
 *
 * ## Implementation
 * ```kotlin
 * class MainDispatcherRule(
 *     val testDispatcher: TestCoroutineDispatcher = TestCoroutineDispatcher()
 * ) : TestWatcher() {
 *
 *     // Before each test install test dispatcher as Main
 *     override fun starting(description: Description) {
 *         Dispatchers.setMain(testDispatcher)
 *     }
 *
 *     // After each test restore real Main dispatcher
 *     override fun finished(description: Description) {
 *         Dispatchers.resetMain()
 *         testDispatcher.cleanupTestCoroutines()
 *     }
 * }
 * ```
 *
 * ## Usage in Test Class
 * ```kotlin
 * class NewsViewModelTest {
 *
 *     // Applied automatically to every @Test in this class
 *     @get:Rule
 *     val mainDispatcherRule = MainDispatcherRule()
 *
 *     @Test
 *     fun `my coroutine test`() = runTest {
 *         // Dispatchers.Main now works correctly here
 *     }
 * }
 * ```
 *
 * ## runTest vs runBlocking
 * ```kotlin
 * runBlocking { }
 * // Blocks the calling thread until coroutine completes
 * // Does NOT advance virtual time past delays
 * // Use only for simple suspend functions with no delays
 *
 * runTest { }
 * // Purpose-built for testing coroutines
 * // Automatically advances virtual time past ALL delays
 * // delay(5000) completes instantly inside runTest
 * // Use for ViewModel tests, Flow tests, anything with delay()
 * ```
 */
object MainDispatcherRuleGuide


/**
 * # Fake Repository Pattern - The Most Important Unit Test Pattern
 *
 * ## Why Fake Instead of Mock?
 * ```
 * MOCK using MockK:
 * - Framework generates a fake object at runtime
 * - You define behaviour per-test using every{} or coEvery{}
 * - Good for simple suspend functions and verifying call counts
 * - Bad for Flow testing, stateful behaviour, multiple emissions
 *
 * FAKE:
 * - YOU write a real in-memory implementation
 * - Stores data in MutableStateFlow - emits like the real thing
 * - Good for Flow testing, offline-first repos, complex state
 * - Tests read naturally with no mock setup boilerplate
 * - Closer to real behaviour - finds integration bugs early
 * ```
 *
 * ## The Repository Interface in domain layer
 * ```kotlin
 * interface NewsRepository {
 *     fun getNews(): Flow<List<News>>
 *     suspend fun refreshNews()
 *     suspend fun getNewsById(id: String): News?
 *     suspend fun bookmarkNews(id: String)
 * }
 * ```
 *
 * ## The Fake Implementation placed in src/test/
 * ```kotlin
 * class FakeNewsRepository : NewsRepository {
 *
 *     // Internal State
 *
 *     // MutableStateFlow replaces the Room database entirely
 *     // All Flow collectors are notified when value changes
 *     private val newsFlow = MutableStateFlow<List<News>>(emptyList())
 *
 *     // Method call counters for verification in tests
 *     var refreshCallCount = 0
 *     var bookmarkCallCount = 0
 *     var lastBookmarkedId: String? = null
 *
 *     // Failure simulation flags
 *     // Set to true in tests that verify error handling
 *     var shouldThrowOnGet = false
 *     var shouldThrowOnRefresh = false
 *
 *     // Interface Implementation
 *
 *     override fun getNews(): Flow<List<News>> {
 *         if (shouldThrowOnGet) {
 *             return flow { throw IOException("Network error") }
 *         }
 *         return newsFlow
 *     }
 *
 *     override suspend fun refreshNews() {
 *         refreshCallCount++
 *         if (shouldThrowOnRefresh) {
 *             throw IOException("Refresh failed")
 *         }
 *     }
 *
 *     override suspend fun getNewsById(id: String): News? {
 *         return newsFlow.value.find { it.id == id }
 *     }
 *
 *     override suspend fun bookmarkNews(id: String) {
 *         bookmarkCallCount++
 *         lastBookmarkedId = id
 *         newsFlow.value = newsFlow.value.map { news ->
 *             if (news.id == id) news.copy(isBookmarked = true) else news
 *         }
 *     }
 *
 *     // Test Helper Methods
 *     // These do NOT exist on the real repository interface
 *     // They are ONLY for controlling the fake from tests
 *
 *     fun emitNews(news: List<News>) { newsFlow.value = news }
 *     fun emitEmpty() { newsFlow.value = emptyList() }
 *
 *     fun reset() {
 *         newsFlow.value = emptyList()
 *         refreshCallCount = 0
 *         bookmarkCallCount = 0
 *         lastBookmarkedId = null
 *         shouldThrowOnGet = false
 *         shouldThrowOnRefresh = false
 *     }
 * }
 * ```
 */
object FakeRepositoryGuide


/**
 * # Test Data Builders - Reusable Test Data
 *
 * ## Why Test Data Builders?
 * ```kotlin
 * // WITHOUT builder - repetitive and fragile
 * // If News gains a new required field every test breaks
 * val news = News("id1", "title", "content", "url", false, 123L, "author", "tech")
 *
 * // WITH builder - only specify what the test cares about
 * val news = TestData.newsItem()                     // all defaults
 * val breaking = TestData.newsItem(isBreaking = true) // only what matters
 * ```
 *
 * ## Implementation
 * ```kotlin
 * object TestData {
 *
 *     // Base factory - all parameters have sensible defaults
 *     // Override ONLY what the specific test cares about
 *     fun newsItem(
 *         id: String = "test-id-1",
 *         title: String = "Test Article Title",
 *         content: String = "Test article content body text.",
 *         imageUrl: String = "https://test.com/image.jpg",
 *         isBreaking: Boolean = false,
 *         publishedAt: Long = 1_000_000L,
 *         author: String = "Test Author",
 *         category: String = "technology"
 *     ) = News(
 *         id = id, title = title, content = content,
 *         imageUrl = imageUrl, isBreaking = isBreaking,
 *         publishedAt = publishedAt, author = author, category = category
 *     )
 *
 *     // Convenience builders for common test scenarios
 *     fun breakingNews(id: String = "breaking-1") = newsItem(
 *         id = id,
 *         title = "BREAKING: Major Event Happening Now",
 *         isBreaking = true
 *     )
 *
 *     fun regularNews(id: String = "regular-1") = newsItem(
 *         id = id,
 *         title = "Regular Article About Something",
 *         isBreaking = false
 *     )
 *
 *     // Build a list of N items instantly
 *     fun newsList(count: Int) = (1..count).map { i ->
 *         newsItem(id = "id-$i", title = "Article $i")
 *     }
 *
 *     // Mixed list with both types
 *     fun mixedNewsList() = listOf(
 *         breakingNews(id = "b1"),
 *         regularNews(id = "r1"),
 *         breakingNews(id = "b2"),
 *         regularNews(id = "r2")
 *     )
 * }
 * ```
 */
object TestDataBuilderGuide


/**
 * # UseCase Unit Tests
 *
 * ## What to Test in a UseCase
 * ```
 * Business rule filtering such as breaking news filter
 * Business rule transformation such as sorting and grouping
 * Error propagation - does error reach the caller
 * Edge cases - empty list, single item, null values
 * Reactive updates - does list update when repo emits new data
 * ```
 *
 * ## Complete UseCase Test
 * ```kotlin
 * class GetBreakingNewsUseCaseTest {
 *
 *     private val fakeRepository = FakeNewsRepository()
 *     private val useCase = GetBreakingNewsUseCase(fakeRepository)
 *
 *     @Before fun setup() = fakeRepository.reset()
 *
 *     @Test
 *     fun `returns only breaking news from mixed list`() = runTest {
 *         // ARRANGE
 *         fakeRepository.emitNews(TestData.mixedNewsList())
 *
 *         // ACT - first() collects the first emission from the Flow
 *         val result = useCase().first()
 *
 *         // ASSERT
 *         assertThat(result).hasSize(2)
 *         assertThat(result.all { it.isBreaking }).isTrue()
 *     }
 *
 *     @Test
 *     fun `returns empty list when no breaking news exists`() = runTest {
 *         fakeRepository.emitNews(listOf(
 *             TestData.regularNews("r1"),
 *             TestData.regularNews("r2")
 *         ))
 *         val result = useCase().first()
 *         assertThat(result).isEmpty()
 *     }
 *
 *     @Test
 *     fun `propagates repository error to caller`() = runTest {
 *         fakeRepository.shouldThrowOnGet = true
 *         assertThrows<IOException> { useCase().first() }
 *     }
 *
 *     @Test
 *     fun `reacts to list updates over time`() = runTest {
 *         fakeRepository.emitNews(emptyList())
 *
 *         useCase().test {
 *             assertThat(awaitItem()).isEmpty()                    // first emission
 *             fakeRepository.emitNews(listOf(TestData.breakingNews())) // update repo
 *             assertThat(awaitItem()).hasSize(1)                  // second emission
 *             cancelAndIgnoreRemainingEvents()
 *         }
 *     }
 * }
 * ```
 */
object UseCaseTestGuide


/**
 * # ViewModel Tests with Turbine
 *
 * ## What is Turbine?
 * A library by CashApp that makes testing Kotlin Flows readable.
 * Without Turbine Flow testing involves complex coroutine wrangling.
 * With Turbine it reads almost like plain English.
 *
 * ## Turbine API - Complete Reference
 * ```kotlin
 * flow.test {
 *     awaitItem()                      // wait for next emission and return it
 *     awaitComplete()                  // wait for flow to complete normally
 *     awaitError()                     // wait for flow to throw an exception
 *     skipItems(n)                     // skip next N emissions silently
 *     expectNoEvents()                 // assert nothing has been emitted yet
 *     cancelAndIgnoreRemainingEvents() // end the test ignoring leftover events
 * }
 * ```
 *
 * ## What to Test in a ViewModel
 * ```
 * Initial state is correct - Loading before any data arrives
 * State transitions - Loading to Success to Error to Empty
 * One-time events - Snackbar, Navigation, ScrollToTop
 * User actions trigger correct state changes
 * Refresh triggers repository refreshNews call
 * Filter changes update the displayed list correctly
 * Error from repo becomes Error state in ViewModel
 * ```
 *
 * ## Complete ViewModel Test
 * ```kotlin
 * class NewsViewModelTest {
 *
 *     @get:Rule
 *     val mainDispatcherRule = MainDispatcherRule()
 *
 *     private val fakeRepository = FakeNewsRepository()
 *     private lateinit var viewModel: NewsViewModel
 *
 *     @Before
 *     fun setup() {
 *         fakeRepository.reset()
 *         viewModel = NewsViewModel(
 *             getBreakingNews = GetBreakingNewsUseCase(fakeRepository),
 *             refreshNews = RefreshNewsUseCase(fakeRepository)
 *         )
 *     }
 *
 *     @Test
 *     fun `initial state is Loading`() = runTest {
 *         assertThat(viewModel.uiState.value)
 *             .isInstanceOf(NewsUiState.Loading::class.java)
 *     }
 *
 *     @Test
 *     fun `state becomes Success when news arrives`() = runTest {
 *         viewModel.uiState.test {
 *             assertThat(awaitItem()).isInstanceOf(NewsUiState.Loading::class.java)
 *
 *             fakeRepository.emitNews(listOf(TestData.breakingNews()))
 *
 *             val success = awaitItem() as NewsUiState.Success
 *             assertThat(success.items).hasSize(1)
 *             assertThat(success.items.first().title)
 *                 .isEqualTo("BREAKING: Major Event Happening Now")
 *
 *             cancelAndIgnoreRemainingEvents()
 *         }
 *     }
 *
 *     @Test
 *     fun `state becomes Empty when no breaking news exists`() = runTest {
 *         viewModel.uiState.test {
 *             skipItems(1)
 *             fakeRepository.emitNews(listOf(TestData.regularNews()))
 *             assertThat(awaitItem()).isInstanceOf(NewsUiState.Empty::class.java)
 *             cancelAndIgnoreRemainingEvents()
 *         }
 *     }
 *
 *     @Test
 *     fun `state becomes Error when repository throws`() = runTest {
 *         fakeRepository.shouldThrowOnGet = true
 *         viewModel.uiState.test {
 *             skipItems(1)
 *             val error = awaitItem() as NewsUiState.Error
 *             assertThat(error.message).isEqualTo("Network error")
 *             cancelAndIgnoreRemainingEvents()
 *         }
 *     }
 *
 *     @Test
 *     fun `ScrollToTop event emitted after refresh`() = runTest {
 *         fakeRepository.emitNews(TestData.newsList(3))
 *         viewModel.uiEvent.test {
 *             viewModel.onRefresh()
 *             assertThat(awaitItem())
 *                 .isInstanceOf(NewsUiEvent.ScrollToTop::class.java)
 *             cancelAndIgnoreRemainingEvents()
 *         }
 *     }
 *
 *     @Test
 *     fun `NavigateToDetail event emitted when item clicked`() = runTest {
 *         val news = TestData.breakingNews()
 *         fakeRepository.emitNews(listOf(news))
 *         viewModel.uiEvent.test {
 *             viewModel.onNewsItemClicked(news)
 *             val event = awaitItem() as NewsUiEvent.NavigateToDetail
 *             assertThat(event.newsId).isEqualTo(news.id)
 *             cancelAndIgnoreRemainingEvents()
 *         }
 *     }
 *
 *     @Test
 *     fun `refresh calls repository refreshNews exactly once`() = runTest {
 *         viewModel.onRefresh()
 *         assertThat(fakeRepository.refreshCallCount).isEqualTo(1)
 *     }
 * }
 * ```
 */
object ViewModelTestGuide


/**
 * # Mapper Unit Tests
 *
 * ## What to Test in a Mapper
 * ```
 * Every formatting function - dates, labels, truncation
 * Null handling - correct defaults applied
 * Edge cases - empty string, zero, boundary length values
 * Field mapping correctness - right field to right property
 * Flattening - nested DTO correctly flattened to domain model
 * ```
 *
 * ## Complete Mapper Test
 * ```kotlin
 * class NewsUiMapperTest {
 *
 *     // No @Rule needed - mapper has no coroutines whatsoever
 *     private val mapper = NewsUiMapper()
 *
 *     @Test
 *     fun `formattedDate shows Just now for timestamp under 1 minute ago`() {
 *         val news = TestData.newsItem(
 *             publishedAt = System.currentTimeMillis() - 30_000L
 *         )
 *         assertThat(mapper.toUiModel(news).formattedDate).isEqualTo("Just now")
 *     }
 *
 *     @Test
 *     fun `formattedDate shows minutes for timestamp 5 minutes ago`() {
 *         val fiveMinutesAgo = System.currentTimeMillis() - (5L * 60 * 1_000)
 *         val news = TestData.newsItem(publishedAt = fiveMinutesAgo)
 *         assertThat(mapper.toUiModel(news).formattedDate).isEqualTo("5 minutes ago")
 *     }
 *
 *     @Test
 *     fun `formattedDate shows hours for timestamp 3 hours ago`() {
 *         val threeHoursAgo = System.currentTimeMillis() - (3L * 60 * 60 * 1_000)
 *         val news = TestData.newsItem(publishedAt = threeHoursAgo)
 *         assertThat(mapper.toUiModel(news).formattedDate).isEqualTo("3 hours ago")
 *     }
 *
 *     @Test
 *     fun `authorLabel prepends By prefix to author name`() {
 *         val news = TestData.newsItem(author = "John Smith")
 *         assertThat(mapper.toUiModel(news).authorLabel).isEqualTo("By John Smith")
 *     }
 *
 *     @Test
 *     fun `categoryBadge converts category to uppercase`() {
 *         val news = TestData.newsItem(category = "technology")
 *         assertThat(mapper.toUiModel(news).categoryBadge).isEqualTo("TECHNOLOGY")
 *     }
 *
 *     @Test
 *     fun `contentPreview truncates content over 100 chars with ellipsis`() {
 *         val news = TestData.newsItem(content = "A".repeat(150))
 *         val preview = mapper.toUiModel(news).contentPreview
 *         assertThat(preview).hasLength(103)
 *         assertThat(preview).endsWith("...")
 *     }
 *
 *     @Test
 *     fun `contentPreview does not truncate content under 100 chars`() {
 *         val shortContent = "Short article."
 *         val news = TestData.newsItem(content = shortContent)
 *         val preview = mapper.toUiModel(news).contentPreview
 *         assertThat(preview).isEqualTo(shortContent)
 *         assertThat(preview).doesNotContain("...")
 *     }
 *
 *     @Test
 *     fun `id passes through mapper unchanged`() {
 *         val news = TestData.newsItem(id = "unique-id-123")
 *         assertThat(mapper.toUiModel(news).id).isEqualTo("unique-id-123")
 *     }
 * }
 * ```
 */
object MapperTestGuide


/**
 * # MockK - When to Use Mocks Instead of Fakes
 *
 * ## When MockK is Better Than a Fake
 * ```
 * Simple suspend functions with no Flow or complex state
 * Verifying a method was called with specific arguments
 * Quick one-off tests where writing a full fake is overkill
 * Capturing argument values passed to a method
 * ```
 *
 * ## MockK Core API
 * ```kotlin
 * // Create a mock instance
 * val mockRepo = mockk<NewsRepository>()
 *
 * // Stub a regular function
 * every { mockRepo.someValue } returns expectedValue
 *
 * // Stub a suspend function
 * coEvery { mockRepo.refreshNews() } just runs        // returns Unit
 * coEvery { mockRepo.getNewsById("id1") } returns news // returns value
 *
 * // Stub to throw an exception
 * coEvery { mockRepo.refreshNews() } throws IOException("error")
 *
 * // Verify a method was called
 * verify { mockRepo.someValue }
 * coVerify { mockRepo.refreshNews() }
 *
 * // Verify exactly N times
 * coVerify(exactly = 1) { mockRepo.refreshNews() }
 *
 * // Verify it was NOT called
 * verify(exactly = 0) { mockRepo.neverCalledMethod() }
 *
 * // Argument matchers
 * coEvery { mockRepo.getNewsById(any()) } returns news
 * coEvery { mockRepo.getNewsById(match { it.startsWith("b") }) } returns news
 *
 * // Capture argument for inspection
 * val slot = slot<String>()
 * coEvery { mockRepo.getNewsById(capture(slot)) } returns news
 * coVerify { mockRepo.getNewsById(any()) }
 * assertThat(slot.captured).isEqualTo("expected-id")
 * ```
 *
 * ## MockK Example for a Simple Suspend Function
 * ```kotlin
 * class RefreshNewsUseCaseTest {
 *
 *     private val mockRepo = mockk<NewsRepository>()
 *     private val useCase = RefreshNewsUseCase(mockRepo)
 *
 *     @Test
 *     fun `calls repository refreshNews exactly once`() = runTest {
 *         coEvery { mockRepo.refreshNews() } just runs
 *         useCase()
 *         coVerify(exactly = 1) { mockRepo.refreshNews() }
 *     }
 *
 *     @Test
 *     fun `propagates IOException from repository`() = runTest {
 *         coEvery { mockRepo.refreshNews() } throws IOException("No internet")
 *         assertThrows<IOException> { useCase() }
 *     }
 * }
 * ```
 */
object MockKGuide


// ════════════════════════════════════════════════════════════════════════
//  SECTION 4 — INTEGRATION TESTS
// ════════════════════════════════════════════════════════════════════════

/**
 * # Integration Tests Complete Guide
 *
 * ## Definition
 * Tests that verify multiple components working together correctly.
 * More realistic than unit tests but slower to execute.
 * Room integration tests run on device because Room requires Android SQLite.
 *
 * ## File Location
 * ```
 * src/androidTest/java/com/app/app/
 * ```
 *
 * ## Room DAO Integration Tests
 * ```
 * Tests the actual SQL queries against an in-memory SQLite database.
 * Verifies insert, query, update, delete, Flow emissions, foreign keys.
 * Room.inMemoryDatabaseBuilder creates a fresh database for each test.
 * No file written to disk - test stays isolated and fast.
 * ```
 *
 * ## Complete Room DAO Test
 * ```kotlin
 * @RunWith(AndroidJUnit4::class)
 * class NewsDaoTest {
 *
 *     private lateinit var database: NewsDatabase
 *     private lateinit var dao: NewsDao
 *
 *     @Before
 *     fun setup() {
 *         // In-memory database - created fresh before each test
 *         // allowMainThreadQueries - ONLY for tests, never production
 *         database = Room.inMemoryDatabaseBuilder(
 *             ApplicationProvider.getApplicationContext(),
 *             NewsDatabase::class.java
 *         )
 *         .allowMainThreadQueries()
 *         .build()
 *
 *         dao = database.newsDao()
 *     }
 *
 *     @After
 *     fun teardown() {
 *         database.close() // always close to release SQLite resources
 *     }
 *
 *     @Test
 *     fun insertNewsAndRetrieveById() = runTest {
 *         val entity = buildTestEntity(id = "test-1", title = "Test Article")
 *         dao.insert(entity)
 *         val retrieved = dao.getById("test-1")
 *         assertThat(retrieved).isNotNull()
 *         assertThat(retrieved?.title).isEqualTo("Test Article")
 *     }
 *
 *     @Test
 *     fun getNewsFlowEmitsWhenDataChanges() = runTest {
 *         dao.getNewsFlow().test {
 *             assertThat(awaitItem()).isEmpty()         // starts empty
 *             dao.insert(buildTestEntity("id-1"))       // insert item
 *             val updated = awaitItem()                 // Flow emits automatically
 *             assertThat(updated).hasSize(1)
 *             assertThat(updated.first().id).isEqualTo("id-1")
 *             cancelAndIgnoreRemainingEvents()
 *         }
 *     }
 *
 *     @Test
 *     fun deleteAllClearsTheTable() = runTest {
 *         dao.insertAll(listOf(
 *             buildTestEntity("1"),
 *             buildTestEntity("2"),
 *             buildTestEntity("3")
 *         ))
 *         assertThat(dao.getAll()).hasSize(3)
 *         dao.deleteAll()
 *         assertThat(dao.getAll()).isEmpty()
 *     }
 *
 *     @Test
 *     fun insertingDuplicateIdReplacesExistingRow() = runTest {
 *         dao.insert(buildTestEntity("id-1", title = "Original"))
 *         dao.insert(buildTestEntity("id-1", title = "Updated"))
 *         val all = dao.getAll()
 *         assertThat(all).hasSize(1)                   // still just one row
 *         assertThat(all.first().title).isEqualTo("Updated") // updated title
 *     }
 *
 *     // Helper to build test entities concisely
 *     private fun buildTestEntity(id: String, title: String = "Title $id") =
 *         NewsEntity(
 *             id = id, title = title, content = "Content",
 *             imageUrl = "url", isBreaking = false, publishedAt = 1000L,
 *             author = "Author", category = "tech",
 *             cachedAt = System.currentTimeMillis()
 *         )
 * }
 * ```
 *
 * ## Repository + Room Integration Test
 * ```kotlin
 * @RunWith(AndroidJUnit4::class)
 * class NewsRepositoryIntegrationTest {
 *
 *     private lateinit var database: NewsDatabase
 *     private lateinit var repository: NewsRepositoryImpl
 *
 *     @Before
 *     fun setup() {
 *         database = Room.inMemoryDatabaseBuilder(
 *             ApplicationProvider.getApplicationContext(),
 *             NewsDatabase::class.java
 *         ).allowMainThreadQueries().build()
 *
 *         repository = NewsRepositoryImpl(
 *             localDataSource = NewsLocalDataSource(database.newsDao()),
 *             mapper = NewsDataMapper()
 *         )
 *     }
 *
 *     @After fun teardown() = database.close()
 *
 *     @Test
 *     fun `returns empty list initially before any data inserted`() = runTest {
 *         val result = repository.getNews().first()
 *         assertThat(result).isEmpty()
 *     }
 *
 *     @Test
 *     fun `UI observes new data automatically after refresh`() = runTest {
 *         repository.getNews().test {
 *             assertThat(awaitItem()).isEmpty()
 *             repository.refreshNews()              // inserts data into Room
 *             assertThat(awaitItem()).isNotEmpty()   // Room Flow emits automatically
 *             cancelAndIgnoreRemainingEvents()
 *         }
 *     }
 * }
 * ```
 */
object IntegrationTestGuide


// ════════════════════════════════════════════════════════════════════════
//  SECTION 5 — ESPRESSO UI TESTS
// ════════════════════════════════════════════════════════════════════════

/**
 * # Espresso UI Tests Complete Guide
 *
 * ## Definition
 * Espresso tests run on a real device or emulator.
 * They simulate actual user interactions - tapping, typing, scrolling.
 * They test the entire application stack end-to-end.
 *
 * ## File Location
 * ```
 * src/androidTest/java/com/app/app/
 * ```
 *
 * ## Three Core Methods
 * ```kotlin
 * onView(matcher)        // FIND the view
 *     .perform(action)   // DO something to it
 *     .check(assertion)  // VERIFY the result
 * ```
 *
 * ## ViewMatchers - How to FIND Views
 * ```kotlin
 * withId(R.id.button)              // find by resource ID - most reliable
 * withText("Submit")               // find by exact text content
 * withText(R.string.submit)        // find by string resource ID
 * withContentDescription("icon")   // find by accessibility description
 * withHint("Enter email here")     // find by placeholder hint text
 * isDisplayed()                    // view is currently visible on screen
 * isEnabled()                      // view is enabled and interactable
 * isChecked()                      // checkbox or toggle is in checked state
 * isNotChecked()                   // checkbox or toggle is in unchecked state
 * hasFocus()                       // view currently has keyboard focus
 *
 * // Combining multiple matchers together
 * allOf(withId(R.id.btn), isDisplayed())     // both conditions must match
 * anyOf(withText("OK"), withText("Yes"))     // either condition must match
 * not(isDisplayed())                         // view must NOT be visible
 * ```
 *
 * ## ViewActions - What to DO to Views
 * ```kotlin
 * click()                // single tap on the view
 * longClick()            // long press on the view
 * doubleClick()          // double tap on the view
 * typeText("hello")      // type text showing keyboard first
 * replaceText("hello")   // replace all text without keyboard
 * clearText()            // clear all text from input field
 * pressBack()            // press the device back button
 * closeSoftKeyboard()    // dismiss the on-screen keyboard
 * swipeLeft()            // swipe left across the view
 * swipeRight()           // swipe right across the view
 * swipeUp()              // swipe up on the view
 * swipeDown()            // swipe down - triggers pull to refresh
 * scrollTo()             // scroll the view into the visible screen area
 * ```
 *
 * ## ViewAssertions - What to VERIFY
 * ```kotlin
 * matches(isDisplayed())           // view IS visible on screen
 * matches(isEnabled())             // view IS enabled
 * matches(withText("Hello"))       // view HAS this exact text
 * matches(isChecked())             // checkbox IS checked
 * matches(not(isDisplayed()))      // view is NOT visible on screen
 * doesNotExist()                   // view does not exist in the hierarchy
 * ```
 *
 * ## RecyclerView Specific Actions
 * ```kotlin
 * // Scroll to a specific position in the list
 * onView(withId(R.id.recycler_view))
 *     .perform(RecyclerViewActions.scrollToPosition<MyVH>(5))
 *
 * // Click on an item at a specific position
 * onView(withId(R.id.recycler_view))
 *     .perform(RecyclerViewActions.actionOnItemAtPosition<MyVH>(0, click()))
 *
 * // Scroll to an item that contains specific text
 * onView(withId(R.id.recycler_view))
 *     .perform(RecyclerViewActions.scrollTo<MyVH>(
 *         hasDescendant(withText("Breaking News"))
 *     ))
 * ```
 *
 * ## Idling Resources - CRITICAL for Async Operations
 * ```
 * The Problem:
 * Espresso executes assertions immediately after actions.
 * Your coroutine or network call has not finished yet.
 * The test fails because data is not loaded yet.
 *
 * The Solution:
 * Register an IdlingResource to tell Espresso to WAIT
 * until your async work is complete before running assertions.
 * ```
 *
 * ```kotlin
 * // Step 1 - Create the IdlingResource singleton
 * object EspressoIdlingResource {
 *     val countingIdlingResource = CountingIdlingResource("GLOBAL")
 *     fun increment() = countingIdlingResource.increment()
 *     fun decrement() = countingIdlingResource.decrement()
 * }
 *
 * // Step 2 - Wrap async work in your ViewModel
 * fun fetchNews() {
 *     EspressoIdlingResource.increment() // tell Espresso: I am busy
 *     viewModelScope.launch {
 *         try {
 *             repository.refreshNews()
 *         } finally {
 *             EspressoIdlingResource.decrement() // tell Espresso: I am done
 *         }
 *     }
 * }
 *
 * // Step 3 - Register and unregister in the test class
 * @Before
 * fun registerIdlingResource() {
 *     IdlingRegistry.getInstance()
 *         .register(EspressoIdlingResource.countingIdlingResource)
 * }
 *
 * @After
 * fun unregisterIdlingResource() {
 *     IdlingRegistry.getInstance()
 *         .unregister(EspressoIdlingResource.countingIdlingResource)
 * }
 *
 * // Step 4 - Espresso now automatically waits before asserting
 * @Test
 * fun newsListLoadsAndDisplays() {
 *     // Espresso waits until decrement() is called before running this
 *     onView(withId(R.id.recycler_view))
 *         .check(matches(isDisplayed()))
 * }
 * ```
 *
 * ## Hilt and Espresso - Injecting Fakes Into UI Tests
 * ```kotlin
 * // Step 1 - Create a Hilt test module that replaces the real one
 * @Module
 * @TestInstallIn(
 *     components = [SingletonComponent::class],
 *     replaces = [RepositoryModule::class]
 * )
 * object FakeRepositoryModule {
 *     @Provides @Singleton
 *     fun provideNewsRepository(): NewsRepository {
 *         return FakeNewsRepository().apply {
 *             emitNews(TestData.newsList(5)) // pre-populate with test data
 *         }
 *     }
 * }
 *
 * // Step 2 - Use HiltAndroidRule in the test class
 * @HiltAndroidTest
 * @RunWith(AndroidJUnit4::class)
 * class NewsActivityTest {
 *
 *     @get:Rule(order = 0)
 *     val hiltRule = HiltAndroidRule(this)
 *
 *     @get:Rule(order = 1)
 *     val activityRule = ActivityScenarioRule(NewsActivity::class.java)
 *
 *     @Inject
 *     lateinit var repository: NewsRepository
 *
 *     @Before fun setup() = hiltRule.inject()
 *
 *     @Test
 *     fun newsListDisplaysInjectedTestData() {
 *         onView(withId(R.id.recycler_view))
 *             .check(matches(isDisplayed()))
 *         onView(withText("Article 1"))
 *             .check(matches(isDisplayed()))
 *     }
 *
 *     @Test
 *     fun errorStateShowsRetryButton() {
 *         (repository as FakeNewsRepository).shouldThrowOnGet = true
 *         onView(withId(R.id.retry_button))
 *             .check(matches(isDisplayed()))
 *     }
 * }
 * ```
 *
 * ## Complete Espresso Test Class
 * ```kotlin
 * @RunWith(AndroidJUnit4::class)
 * class NewsScreenEspressoTest {
 *
 *     @get:Rule
 *     val activityRule = ActivityScenarioRule(NewsActivity::class.java)
 *
 *     @Before
 *     fun registerIdlingResource() {
 *         IdlingRegistry.getInstance()
 *             .register(EspressoIdlingResource.countingIdlingResource)
 *     }
 *
 *     @After
 *     fun unregisterIdlingResource() {
 *         IdlingRegistry.getInstance()
 *             .unregister(EspressoIdlingResource.countingIdlingResource)
 *     }
 *
 *     @Test
 *     fun newsListIsVisibleOnLaunch() {
 *         onView(withId(R.id.recycler_view))
 *             .check(matches(isDisplayed()))
 *     }
 *
 *     @Test
 *     fun clickingNewsItemNavigatesToDetailScreen() {
 *         onView(withId(R.id.recycler_view))
 *             .perform(RecyclerViewActions
 *                 .actionOnItemAtPosition<NewsAdapter.ViewHolder>(0, click()))
 *         onView(withId(R.id.detail_content))
 *             .check(matches(isDisplayed()))
 *     }
 *
 *     @Test
 *     fun searchInputFiltersListResults() {
 *         onView(withId(R.id.search_input))
 *             .perform(click(), typeText("breaking"), closeSoftKeyboard())
 *         onView(withText("BREAKING: Major Event"))
 *             .check(matches(isDisplayed()))
 *     }
 *
 *     @Test
 *     fun pullToRefreshUpdatesContentList() {
 *         onView(withId(R.id.swipe_refresh_layout))
 *             .perform(swipeDown())
 *         onView(withId(R.id.recycler_view))
 *             .check(matches(isDisplayed()))
 *     }
 * }
 * ```
 */
object EspressoTestGuide


// ════════════════════════════════════════════════════════════════════════
//  SECTION 6 — TEST NAMING CONVENTIONS
// ════════════════════════════════════════════════════════════════════════

/**
 * # Test Naming Conventions
 *
 * ## Recommended Format
 * ```kotlin
 * // Format 1 - Given When Then (most descriptive for complex tests)
 * fun `given breaking news list when filter applied then returns only breaking`()
 *
 * // Format 2 - should do something (clean and readable)
 * fun `should return only breaking news when filter is active`()
 *
 * // Format 3 - does something (concise for simple tests)
 * fun `returns empty list when repository is empty`()
 * fun `emits error state when network fails`()
 * fun `formats timestamp as Just now for recent articles`()
 * ```
 *
 * ## Naming Rules
 * ```
 * Use backtick function names - reads like plain English
 * Describe the OUTCOME - what should happen as a result
 * Include the CONDITION - when this happens or given this state
 * Be specific - not test1 or worksCorrectly or checkNews
 * Never use test prefix - JUnit4 finds tests by @Test annotation alone
 * ```
 *
 * ## Good vs Bad Examples
 * ```kotlin
 * // BAD names
 * fun test1()
 * fun testMapping()
 * fun worksCorrectly()
 * fun checkNews()
 * fun verifyState()
 *
 * // GOOD names
 * fun `returns breaking news when filter is enabled`()
 * fun `emits Loading then Success when data arrives`()
 * fun `formats date as 2 hours ago for timestamp 2 hours in past`()
 * fun `throws IllegalStateException when news id is null`()
 * fun `refresh increments repository call count by exactly 1`()
 * fun `contentPreview truncates to 100 chars and appends ellipsis`()
 * ```
 */
object TestNamingGuide


// ════════════════════════════════════════════════════════════════════════
//  SECTION 7 — FAKE VS MOCK DECISION GUIDE
// ════════════════════════════════════════════════════════════════════════

/**
 * # Fake vs Mock Decision Guide
 *
 * ## Decision Tree
 * ```
 * Does the dependency return a Flow or StateFlow?
 *     YES -> Use FAKE (MockK cannot emit multiple values naturally)
 *     NO  continue
 *
 * Does the test need to verify STATE changing over time?
 *     YES -> Use FAKE
 *     NO  continue
 *
 * Is the dependency used across many test classes?
 *     YES -> Use FAKE (write once, reuse everywhere)
 *     NO  continue
 *
 * Is this a simple suspend function verification only?
 *     YES -> Use MOCK (faster to set up for one-off tests)
 *     NO  -> Use FAKE
 * ```
 *
 * ## Comparison Table
 * ```
 * Feature                FAKE                    MOCK using MockK
 * Flow and StateFlow      Natural with emit()     Complex and awkward
 * Multiple emissions      Easy                    Hard
 * State across calls      Built in                Awkward to simulate
 * Call count tracking     Manual counter var      verify(exactly=N)
 * Argument capture        Manual lastArg field    slot<T>() capture
 * Setup effort            More upfront work       Less upfront work
 * Test readability        Very clear              Can get verbose
 * Reusability             High - shared object    Medium - per test
 * ```
 */
object FakeVsMockGuide


// ════════════════════════════════════════════════════════════════════════
//  SECTION 8 — COMMON MISTAKES AND FIXES
// ════════════════════════════════════════════════════════════════════════

/**
 * # Common Testing Mistakes and Their Fixes
 *
 * ## Mistake 1 - Not Using MainDispatcherRule
 * ```kotlin
 * // WRONG - crashes with Main dispatcher not initialized
 * class MyViewModelTest {
 *     @Test
 *     fun test() = runTest {
 *         viewModel.loadData() // uses Dispatchers.Main internally
 *     }
 * }
 *
 * // RIGHT
 * class MyViewModelTest {
 *     @get:Rule
 *     val mainDispatcherRule = MainDispatcherRule() // add this
 *
 *     @Test
 *     fun test() = runTest { viewModel.loadData() }
 * }
 * ```
 *
 * ## Mistake 2 - Using runBlocking Instead of runTest
 * ```kotlin
 * // WRONG - delay(5000) actually waits 5 real seconds
 * @Test
 * fun test() = runBlocking {
 *     viewModel.loadWithDelay() // has delay(5000) inside
 * }
 *
 * // RIGHT - delay(5000) completes instantly in virtual time
 * @Test
 * fun test() = runTest {
 *     viewModel.loadWithDelay() // completes in milliseconds
 * }
 * ```
 *
 * ## Mistake 3 - Not Resetting Fake Between Tests
 * ```kotlin
 * // WRONG - state from test1 leaks into test2
 * class MyTest {
 *     val fake = FakeNewsRepository()
 *
 *     @Test fun test1() { fake.emitNews(list1) }
 *     @Test fun test2() { /* fake still has list1 from test1 */ }
 * }
 *
 * // RIGHT - always reset in @Before
 * class MyTest {
 *     val fake = FakeNewsRepository()
 *
 *     @Before fun setup() { fake.reset() } // clean state every test
 *
 *     @Test fun test1() { fake.emitNews(list1) }
 *     @Test fun test2() { /* starts completely clean */ }
 * }
 * ```
 *
 * ## Mistake 4 - Forgetting cancelAndIgnoreRemainingEvents in Turbine
 * ```kotlin
 * // WRONG - Turbine fails if uncollected events remain after test ends
 * @Test
 * fun test() = runTest {
 *     flow.test {
 *         awaitItem()
 *         // test ends but flow still has pending events -> FAIL
 *     }
 * }
 *
 * // RIGHT - always cancel at the end
 * @Test
 * fun test() = runTest {
 *     flow.test {
 *         awaitItem()
 *         cancelAndIgnoreRemainingEvents() // clean exit always
 *     }
 * }
 * ```
 *
 * ## Mistake 5 - Not Unregistering Espresso IdlingResource
 * ```kotlin
 * // WRONG - idling resource leaks between test methods
 * @Before fun setup() {
 *     IdlingRegistry.getInstance().register(idlingResource)
 * }
 * // No @After to unregister - next test has stale idling resource
 *
 * // RIGHT - always pair register with unregister
 * @Before fun registerIdling() {
 *     IdlingRegistry.getInstance().register(idlingResource)
 * }
 * @After fun unregisterIdling() {
 *     IdlingRegistry.getInstance().unregister(idlingResource) // always
 * }
 * ```
 *
 * ## Mistake 6 - Testing Implementation Instead of Behaviour
 * ```kotlin
 * // WRONG - testing HOW the code works internally
 * @Test
 * fun test() = runTest {
 *     useCase()
 *     verify { someInternalList.filter(any()) } // testing internals
 * }
 *
 * // RIGHT - testing the observable outcome from outside
 * @Test
 * fun `returns only breaking news`() = runTest {
 *     val result = useCase().first()
 *     assertThat(result.all { it.isBreaking }).isTrue()
 * }
 * ```
 */
object CommonMistakesGuide


// ════════════════════════════════════════════════════════════════════════
//  SECTION 9 — QUICK REFERENCE CHEAT SHEET
// ════════════════════════════════════════════════════════════════════════

/**
 * # Quick Reference Cheat Sheet
 *
 * ## Which Test Goes Where?
 * ```
 * Class to Test         Test Type      Location          Key Tools
 * UseCase               Unit           src/test          JUnit + Turbine + Fake
 * ViewModel             Unit           src/test          JUnit + Turbine + MainDispatcherRule
 * Mapper                Unit           src/test          JUnit + Truth
 * Room DAO              Integration    src/androidTest   Room inMemory + Turbine
 * Repository + Room     Integration    src/androidTest   Room inMemory + Turbine
 * Full screen flow      Espresso       src/androidTest   Espresso + Hilt + Idling
 * ```
 *
 * ## Test Setup Checklist
 * ```
 * Unit Test Checklist:
 * Add @get:Rule MainDispatcherRule if testing coroutines or ViewModel
 * Create FakeRepository instance at class level
 * Call fake.reset() in @Before to ensure clean state
 * Use runTest for all coroutine tests not runBlocking
 * Use Turbine flow.test{} block for all Flow assertions
 * End every Turbine block with cancelAndIgnoreRemainingEvents()
 *
 * Integration Test Checklist:
 * Use Room.inMemoryDatabaseBuilder in @Before setup
 * Add .allowMainThreadQueries() for tests only
 * Call database.close() in @After teardown
 * Add @RunWith(AndroidJUnit4::class) to the test class
 *
 * Espresso Test Checklist:
 * Add ActivityScenarioRule in @get:Rule
 * Add HiltAndroidRule if using dependency injection
 * Register IdlingResource in @Before
 * Unregister IdlingResource in @After
 * Add @HiltAndroidTest annotation if injecting fakes
 * ```
 *
 * ## Turbine Quick Reference
 * ```kotlin
 * flow.test {
 *     awaitItem()                      // get next emitted item
 *     skipItems(3)                     // skip next 3 emissions
 *     awaitComplete()                  // flow completed normally
 *     awaitError()                     // flow threw an exception
 *     expectNoEvents()                 // assert nothing emitted yet
 *     cancelAndIgnoreRemainingEvents() // end test cleanly always
 * }
 * ```
 *
 * ## MockK Quick Reference
 * ```kotlin
 * val mock = mockk<MyClass>()
 * every { mock.property } returns value
 * coEvery { mock.suspendFun() } returns value
 * coEvery { mock.unitFun() } just runs
 * coEvery { mock.failFun() } throws Exception("message")
 * verify { mock.property }
 * coVerify(exactly = 1) { mock.suspendFun() }
 * coVerify(exactly = 0) { mock.neverCalledFun() }
 * ```
 *
 * ## Espresso Quick Reference
 * ```kotlin
 * // Find by ID and click
 * onView(withId(R.id.button)).perform(click())
 *
 * // Find by text and verify visible
 * onView(withText("Hello")).check(matches(isDisplayed()))
 *
 * // Type into an input field
 * onView(withId(R.id.input))
 *     .perform(typeText("search query"), closeSoftKeyboard())
 *
 * // Click item in RecyclerView at position 0
 * onView(withId(R.id.recycler_view))
 *     .perform(RecyclerViewActions
 *         .actionOnItemAtPosition<MyVH>(0, click()))
 *
 * // Verify view does not exist
 * onView(withId(R.id.loading)).check(doesNotExist())
 * ```
 *
 * ## Interview Answer on Testing Strategy
 * ```
 * I follow the testing pyramid with the majority of tests being fast
 * unit tests running on the JVM. For ViewModels and UseCases I use a
 * fake repository which is a hand-written in-memory implementation of
 * the repository interface. This gives me full control over what data
 * the ViewModel receives and lets me verify state transitions using
 * Turbine for Flow assertions. I always use MainDispatcherRule to
 * replace Dispatchers.Main in unit tests. For Room I write integration
 * tests against an in-memory database that verify real SQL queries and
 * Flow emissions. I use Espresso only for critical user journeys like
 * login or checkout and inject fakes using Hilt test modules to control
 * the application state during UI tests. I register an IdlingResource
 * to synchronise Espresso assertions with async coroutine work.
 * ```
 */
object QuickReferenceGuide