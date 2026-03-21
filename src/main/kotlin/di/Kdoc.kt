package di

/**
 * # Dependency Injection in Android — Complete Reference
 *
 * A comprehensive guide covering Manual DI, Dagger 2, and Hilt.
 * Covers @Inject, @Module, @Provides, @Binds, Scopes, and best practices.
 *
 * ---
 *
 * ## Table of Contents
 *
 * 1. [What is Dependency Injection]
 * 2. [What is a Dependency]
 * 3. [Three Ways to Inject]
 * 4. [@Inject Constructor]
 * 5. [Manual DI]
 * 6. [Dagger 2]
 * 7. [Hilt Setup]
 * 8. [@Module]
 * 9. [@Provides]
 * 10. [@Binds]
 * 11. [@Provides vs @Binds]
 * 12. [Why companion object in Abstract Module]
 * 13. [Scopes]
 * 14. [Special Hilt Annotations]
 * 15. [Special Context Injections]
 * 16. [Real Production Structure]
 * 17. [Common Mistakes]
 * 18. [Decision Tree]
 * 19. [Cheat Sheet]
 *
 * ---
 *
 * ## 1. What is Dependency Injection
 *
 * You do not create your dependencies. Someone else creates them and hands them to you.
 *
 * **Without DI — tightly coupled:**
 * ```kotlin
 * class UserViewModel {
 *     val repo = UserRepositoryImpl() // UserViewModel creates its own dependency
 *                                     // Cannot swap for a fake in tests
 *                                     // Cannot reuse the same instance
 *                                     // Rigid — changing Impl breaks ViewModel
 * }
 * ```
 *
 * **With DI — loosely coupled:**
 * ```kotlin
 * class UserViewModel @Inject constructor(
 *     val repo: UserRepository // dependency is given from outside
 *                              // Easy to swap for a fake in tests
 *                              // Hilt manages the instance lifecycle
 *                              // ViewModel does not care which Impl is used
 * )
 * ```
 *
 * ---
 *
 * ## 2. What is a Dependency
 *
 * If class A needs class B to work, then B is a dependency of A.
 *
 * ```kotlin
 * class Car {
 *     val engine = Engine() // Car DEPENDS ON Engine
 *                           // Engine is a dependency of Car
 * }
 * ```
 *
 * **The problem with creating dependencies inside a class:**
 * ```kotlin
 * class Car {
 *     val engine = Engine()           // Car creates Engine
 * }
 *
 * class Engine {
 *     val fuelSystem = FuelSystem()   // Engine creates FuelSystem
 * }
 *
 * class FuelSystem {
 *     val injector = Injector()       // FuelSystem creates Injector
 * }
 * // Problems:
 * // - Cannot test Car without a real Engine
 * // - Cannot swap Engine for a different type
 * // - Cannot share a single Engine instance across classes
 * // - Deeply nested creation is hard to trace and debug
 * // - Violates Single Responsibility Principle
 * ```
 *
 * ---
 *
 * ## 3. Three Ways to Inject
 *
 * ### Constructor Injection — Always your first choice
 * ```kotlin
 * class UserRepository @Inject constructor(
 *     private val apiService: ApiService,    // Hilt provides this
 *     private val dataManager: DataManager   // Hilt provides this
 * )
 * // Hilt sees @Inject constructor and builds the entire chain automatically
 * // Most readable, most testable, most recommended
 * ```
 *
 * ### Field Injection — Only when Android creates the class
 * ```kotlin
 * @AndroidEntryPoint
 * class UserFragment : Fragment() {
 *
 *     @Inject lateinit var repository: UserRepository // Hilt fills this in
 *
 *     // Use for: Activity, Fragment, Service, BroadcastReceiver
 *     // These are created by Android, not by you, so constructor injection is impossible
 *     // @AndroidEntryPoint triggers Hilt to fill all @Inject fields automatically
 * }
 * ```
 *
 * ### Method Injection — Almost never use this
 * ```kotlin
 * class SomeClass {
 *     lateinit var repository: UserRepository
 *
 *     @Inject
 *     fun init(repository: UserRepository) {  // Hilt calls this after construction
 *         this.repository = repository         // Avoid — constructor injection is always better
 *     }
 * }
 * ```
 *
 * ---
 *
 * ## 4. @Inject Constructor — The Foundation
 *
 * Tells Hilt: "You are responsible for building this class."
 *
 * **Without @Inject — Hilt is blind:**
 * ```kotlin
 * class GetUsersUseCaseImpl(              // No @Inject
 *     private val repository: UserRepository
 * ) : GetUsersUseCase
 * // Hilt sees this class and says: "I have no idea how to create this"
 * // If you try to inject it anywhere → COMPILE ERROR
 * // error: [Dagger/MissingBinding] GetUsersUseCaseImpl cannot be provided
 * //         without an @Inject constructor or an @Provides-annotated method.
 * ```
 *
 * **With @Inject — Hilt builds the full chain:**
 * ```kotlin
 * class GetUsersUseCaseImpl @Inject constructor(
 *     private val repository: UserRepository  // Hilt provides this automatically
 * ) : GetUsersUseCase
 * // Hilt generates a factory at compile time (you never see this):
 * //
 * // class GetUsersUseCaseImpl_Factory(
 * //     private val repository: Provider<UserRepository>
 * // ) {
 * //     fun create(): GetUsersUseCaseImpl {
 * //         return GetUsersUseCaseImpl(repository.get())
 * //     }
 * // }
 * ```
 *
 * **Hilt chains all the way down automatically:**
 * ```kotlin
 * // UserFragment
 * //     @Inject UserViewModel
 * //         @Inject constructor(UserRepository)
 * //             @Inject constructor(ApiService)
 * //                 ← provided via @Module @Provides (Retrofit)
 * //
 * // Every class in the chain must be known to Hilt. Known means ONE of:
 * // 1. Has @Inject constructor
 * // 2. Has @Provides in a module
 * // 3. Has @Binds in a module
 * //
 * // If any class in the chain is missing → COMPILE ERROR, never a runtime crash
 * ```
 *
 * ---
 *
 * ## 5. Manual DI — Understanding What Hilt Does For You
 *
 * Before using Hilt, understand what it automates.
 *
 * ```kotlin
 * // Step 1 — Define your classes
 * class ApiService {
 *     fun getUsers(): List<User> = listOf()
 * }
 *
 * class UserRepository(private val api: ApiService) {
 *     fun fetchUsers() = api.getUsers()
 * }
 *
 * class UserViewModel(private val repo: UserRepository) {
 *     val users = repo.fetchUsers()
 * }
 *
 * // Step 2 — Wire everything manually in Application
 * class MyApp : Application() {
 *     val apiService = ApiService()
 *     val userRepository = UserRepository(apiService)
 *     // ViewModels grab dependencies from here
 * }
 *
 * // Problem: As the app grows this file becomes a nightmare with 100+ manual wirings.
 * // Solution: Hilt generates all this wiring automatically at compile time.
 * ```
 *
 * ---
 *
 * ## 6. Dagger 2 — The Four Core Concepts
 *
 * Dagger generates wiring code at compile time — fast and type-safe.
 *
 * ### @Inject — Mark constructor for injection
 * ```kotlin
 * class UserRepository @Inject constructor(
 *     private val api: ApiService
 * )
 * ```
 *
 * ### @Module + @Provides — Teach Dagger how to build objects it cannot infer
 * ```kotlin
 * @Module
 * class NetworkModule {
 *
 *     @Provides
 *     @Singleton
 *     fun provideRetrofit(): Retrofit {
 *         return Retrofit.Builder()
 *             .baseUrl("https://api.example.com")
 *             .build()
 *     }
 *
 *     @Provides
 *     fun provideApiService(retrofit: Retrofit): ApiService {
 *         return retrofit.create(ApiService::class.java)
 *     }
 * }
 * ```
 *
 * ### @Component — The bridge between modules and injection targets
 * ```kotlin
 * @Singleton
 * @Component(modules = [NetworkModule::class])
 * interface AppComponent {
 *     fun inject(activity: MainActivity)
 * }
 * ```
 *
 * ### Using in Application
 * ```kotlin
 * class MyApp : Application() {
 *     val appComponent: AppComponent = DaggerAppComponent.create()
 * }
 *
 * class MainActivity : AppCompatActivity() {
 *     @Inject lateinit var viewModel: UserViewModel
 *
 *     override fun onCreate(savedInstanceState: Bundle?) {
 *         (application as MyApp).appComponent.inject(this) // manual inject call
 *         super.onCreate(savedInstanceState)
 *         // viewModel is now available
 *     }
 * }
 * // Problem with Dagger: You need SubComponents, manual inject() calls,
 * // and separate module setup for every Android component.
 * // Solution: Hilt handles all of this automatically.
 * ```
 *
 * ---
 *
 * ## 7. Hilt Setup — Minimum Required Code
 *
 * ```kotlin
 * // Step 1 — Application class. That is it. Hilt sets up the entire DI container.
 * @HiltAndroidApp
 * class MyApp : Application()
 *
 * // Step 2 — Enable injection in Activity
 * @AndroidEntryPoint
 * class MainActivity : AppCompatActivity() {
 *
 *     @Inject lateinit var repository: UserRepository // auto-injected by Hilt
 *
 *     override fun onCreate(savedInstanceState: Bundle?) {
 *         super.onCreate(savedInstanceState)
 *         // repository is already ready here
 *     }
 * }
 *
 * // Step 3 — ViewModel with Hilt
 * @HiltViewModel
 * class UserViewModel @Inject constructor(
 *     private val repository: UserRepository
 * ) : ViewModel()
 *
 * // Step 4 — Get ViewModel in Fragment
 * @AndroidEntryPoint
 * class UserFragment : Fragment() {
 *     private val viewModel: UserViewModel by viewModels() // Hilt wires it automatically
 * }
 * ```
 *
 * ---
 *
 * ## 8. @Module — The Recipe Book
 *
 * Tells Hilt how to create objects it cannot figure out on its own.
 *
 * **When you need a @Module:**
 * ```
 * YES → Class is from a third-party library (Retrofit, Room, OkHttp, Moshi)
 * YES → You want to inject an interface (not the concrete class)
 * YES → Object creation requires custom configuration logic
 * YES → Constructor takes params Hilt cannot infer (hardcoded String, Int, URL)
 *
 * NO  → You own the class and can add @Inject constructor to it
 * ```
 *
 * **@InstallIn is mandatory on every module:**
 * ```kotlin
 * @Module
 * @InstallIn(SingletonComponent::class) // mandatory — tells Hilt where this module lives
 * object NetworkModule {
 *
 *     @Provides
 *     @Singleton
 *     fun provideRetrofit(): Retrofit {
 *         return Retrofit.Builder()
 *             .baseUrl("https://api.example.com")
 *             .addConverterFactory(GsonConverterFactory.create())
 *             .build()
 *     }
 * }
 * ```
 *
 * **Two types of modules:**
 * ```kotlin
 * // object module — use when ALL functions are @Provides (static, no state)
 * @Module
 * @InstallIn(SingletonComponent::class)
 * object NetworkModule {
 *     @Provides
 *     @Singleton
 *     fun provideRetrofit(): Retrofit { ... }
 * }
 *
 * // abstract class module — use when ANY function is @Binds
 * @Module
 * @InstallIn(SingletonComponent::class)
 * abstract class RepositoryModule {
 *     @Binds
 *     abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository
 * }
 * ```
 *
 * ---
 *
 * ## 9. @Provides — Writing the Creation Logic Yourself
 *
 * Use when you need to write code to build the object.
 *
 * ```kotlin
 * @Module
 * @InstallIn(SingletonComponent::class)
 * object NetworkModule {
 *
 *     // @Provides lets you write ANY code to create the object
 *     @Provides
 *     @Singleton
 *     fun provideOkHttpClient(): OkHttpClient {
 *         return OkHttpClient.Builder()
 *             .connectTimeout(30, TimeUnit.SECONDS)
 *             .addInterceptor(HttpLoggingInterceptor().apply {
 *                 level = HttpLoggingInterceptor.Level.BODY
 *             })
 *             .build()
 *     }
 *
 *     // Hilt automatically passes OkHttpClient from above into this function
 *     // You never call these functions manually — Hilt chains them
 *     @Provides
 *     @Singleton
 *     fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
 *         return Retrofit.Builder()
 *             .baseUrl("https://api.example.com")
 *             .client(okHttpClient)
 *             .addConverterFactory(GsonConverterFactory.create())
 *             .build()
 *     }
 *
 *     @Provides
 *     @Singleton
 *     fun provideApiService(retrofit: Retrofit): ApiService {
 *         return retrofit.create(ApiService::class.java)
 *     }
 *
 *     // @Provides when constructor takes a value Hilt cannot infer
 *     @Provides
 *     @Singleton
 *     fun provideGetUsersUseCase(
 *         repository: UserRepository // Hilt provides this
 *     ): GetUsersUseCase {
 *         return GetUsersUseCase(repository, maxResults = 50) // you supply the Int
 *     }
 * }
 * ```
 *
 * **Use @Provides when:**
 * ```
 * YES → Third-party class (Retrofit, OkHttp, Room, Moshi, Glide)
 * YES → Object needs configuration before returning
 * YES → Constructor takes params Hilt cannot infer (hardcoded String, Int, URL)
 * YES → Conditional creation logic needed
 * NO  → Your function body is just → return impl (use @Binds instead)
 * ```
 *
 * ---
 *
 * ## 10. @Binds — Mapping an Interface to its Implementation
 *
 * Use when you just need to tell Hilt which concrete class to use for an interface.
 *
 * ```kotlin
 * // Step 1 — Define the interface
 * interface UserRepository {
 *     fun getUsers(): List<User>
 * }
 *
 * // Step 2 — Concrete implementation with @Inject constructor
 * // @Inject constructor is MANDATORY for @Binds to work
 * // Without it, Hilt cannot build the impl and @Binds has nothing to map to
 * class UserRepositoryImpl @Inject constructor(
 *     private val apiService: ApiService
 * ) : UserRepository {
 *     override fun getUsers() = apiService.fetchUsers()
 * }
 *
 * // Step 3 — Bind the interface to the implementation
 * @Module
 * @InstallIn(SingletonComponent::class)
 * abstract class RepositoryModule {             // must be abstract class for @Binds
 *
 *     @Binds
 *     @Singleton
 *     abstract fun bindUserRepository(          // no body — just a mapping declaration
 *         impl: UserRepositoryImpl              // Hilt builds this via @Inject constructor
 *     ): UserRepository                         // return type = the interface
 * }
 *
 * // Now anywhere you inject UserRepository, Hilt gives you UserRepositoryImpl
 * // The caller never knows about the concrete class
 * @HiltViewModel
 * class UserViewModel @Inject constructor(
 *     private val repository: UserRepository    // receives UserRepositoryImpl automatically
 * ) : ViewModel()
 * ```
 *
 * **Use @Binds when:**
 * ```
 * YES → You have an interface and want to inject it (not the concrete class)
 * YES → The implementation already has @Inject constructor
 * YES → Your @Provides body would just be → return impl
 * NO  → You need custom configuration logic (use @Provides instead)
 * ```
 *
 * ---
 *
 * ## 11. @Provides vs @Binds — Side by Side
 *
 * ```kotlin
 * // ❌ Wrong — using @Provides when body is just return impl
 * @Module
 * @InstallIn(SingletonComponent::class)
 * object RepositoryModule {
 *
 *     @Provides
 *     @Singleton
 *     fun provideUserRepository(
 *         impl: UserRepositoryImpl
 *     ): UserRepository {
 *         return impl  // pointless body — this is exactly what @Binds does
 *     }
 * }
 *
 * // ✅ Correct — using @Binds for interface → impl mapping
 * @Module
 * @InstallIn(SingletonComponent::class)
 * abstract class RepositoryModule {
 *
 *     @Binds
 *     @Singleton
 *     abstract fun bindUserRepository(
 *         impl: UserRepositoryImpl
 *     ): UserRepository  // no body needed
 * }
 * ```
 *
 * ```
 * Comparison Table:
 * ┌──────────────────┬─────────────────────────────┬──────────────────────────────┐
 * │                  │ @Provides                   │ @Binds                       │
 * ├──────────────────┼─────────────────────────────┼──────────────────────────────┤
 * │ Function         │ Has a body                  │ Abstract, no body            │
 * │ Module type      │ object                      │ abstract class               │
 * │ Use case         │ Third-party / custom config │ Interface → Impl mapping     │
 * │ Impl needs       │ No                          │ Yes — @Inject constructor    │
 * │ @Inject?         │                             │ is mandatory                 │
 * │ Generated code   │ More                        │ Less — leaner                │
 * └──────────────────┴─────────────────────────────┴──────────────────────────────┘
 *
 * Golden Rule:
 * If your @Provides body is just → return impl
 * You should be using            → @Binds
 * ```
 *
 * ---
 *
 * ## 12. Why companion object in Abstract Module
 *
 * This is one of the most important structural concepts in Hilt modules.
 *
 * **The Problem — @Binds forces abstract class, but @Provides needs a function body:**
 * ```kotlin
 * // ❌ This does NOT compile
 * @Module
 * @InstallIn(SingletonComponent::class)
 * abstract class AppModule {
 *
 *     @Binds
 *     abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository
 *
 *     @Provides                           // ERROR
 *     @Singleton                          // abstract class cannot have
 *     fun provideRetrofit(): Retrofit {   // non-abstract @Provides functions
 *         return Retrofit.Builder().build()
 *     }
 * }
 * // Why it errors:
 * // @Provides functions need to be called by Hilt to create objects.
 * // In an abstract class, Hilt cannot call a regular instance function
 * // because the class itself is abstract — it cannot be instantiated.
 * // So Hilt has no object to call the function on.
 * ```
 *
 * **The Fix — companion object makes @Provides static:**
 * ```kotlin
 * // ✅ Correct — companion object solves the conflict
 * @Module
 * @InstallIn(SingletonComponent::class)
 * abstract class AppModule {
 *
 *     // @Binds — abstract, belongs to the abstract class
 *     // Hilt resolves this as a mapping at compile time, no instantiation needed
 *     @Binds
 *     @Singleton
 *     abstract fun bindUserRepository(
 *         impl: UserRepositoryImpl
 *     ): UserRepository
 *
 *     @Binds
 *     @ViewModelScoped
 *     abstract fun bindGetUsersUseCase(
 *         impl: GetUsersUseCaseImpl
 *     ): GetUsersUseCase
 *
 *     companion object {
 *         // @Provides — static, belongs to companion object
 *         // companion object is STATIC — lives on the class itself, not on an instance
 *         // Hilt calls these like static methods — no instantiation of AppModule needed
 *
 *         @Provides
 *         @Singleton
 *         fun provideRetrofit(): Retrofit {
 *             return Retrofit.Builder()
 *                 .baseUrl("https://api.example.com")
 *                 .build()
 *         }
 *
 *         @Provides
 *         @Singleton
 *         fun provideApiService(retrofit: Retrofit): ApiService {
 *             return retrofit.create(ApiService::class.java)
 *         }
 *     }
 * }
 *
 * // What Hilt sees internally:
 * //
 * // AppModule (abstract class)
 * //     ├── bindUserRepository()    → abstract → @Binds → compile-time mapping only
 * //     ├── bindGetUsersUseCase()   → abstract → @Binds → compile-time mapping only
 * //     └── companion object        → static   → Hilt calls directly like a static method
 * //             ├── provideRetrofit()
 * //             └── provideApiService()
 * //
 * // Hilt never instantiates AppModule itself.
 * ```
 *
 * **Mental model:**
 * ```
 * abstract class   → where @Binds live  (mappings, no bodies, no instantiation)
 * companion object → where @Provides live (static, have bodies, called directly)
 * ```
 *
 * ---
 *
 * ## 13. Scopes — Controlling How Long an Object Lives
 *
 * Without a scope annotation, Hilt creates a NEW instance every single time
 * something asks for that dependency.
 *
 * ```kotlin
 * // No scope — new instance on EVERY injection
 * @Module
 * @InstallIn(SingletonComponent::class)
 * object FormModule {
 *     @Provides
 *     fun provideFormValidator(): FormValidator = FormValidator()
 *     // Every class that injects FormValidator gets its own separate instance
 * }
 *
 * // @Singleton — one instance for the ENTIRE app lifetime
 * @Module
 * @InstallIn(SingletonComponent::class)
 * object NetworkModule {
 *     @Provides
 *     @Singleton  // created once, reused everywhere, destroyed when app is killed
 *     fun provideRetrofit(): Retrofit = Retrofit.Builder().build()
 * }
 *
 * // @ViewModelScoped — one instance per ViewModel, survives screen rotation
 * @Module
 * @InstallIn(ViewModelComponent::class)
 * abstract class UseCaseModule {
 *     @Binds
 *     @ViewModelScoped  // tied to ViewModel lifetime
 *     abstract fun bindGetUsersUseCase(impl: GetUsersUseCaseImpl): GetUsersUseCase
 * }
 *
 * // @ActivityScoped — one instance per Activity, destroyed when Activity is destroyed
 * @Module
 * @InstallIn(ActivityComponent::class)
 * object AnalyticsModule {
 *     @Provides
 *     @ActivityScoped
 *     fun provideAnalyticsTracker(@ActivityContext ctx: Context): AnalyticsTracker =
 *         AnalyticsTracker(ctx)
 * }
 *
 * // @FragmentScoped — one instance per Fragment
 * @Module
 * @InstallIn(FragmentComponent::class)
 * abstract class FragmentModule {
 *     @Binds
 *     @FragmentScoped
 *     abstract fun bindFragmentHelper(impl: FragmentHelperImpl): FragmentHelper
 * }
 * ```
 *
 * **Full Scope Reference:**
 * ```
 * ┌──────────────────────────┬────────────────────────────────┬───────────────────────────────────┬──────────────────────────────────┐
 * │ Annotation               │ Component                      │ Lives as long as                  │ Use for                          │
 * ├──────────────────────────┼────────────────────────────────┼───────────────────────────────────┼──────────────────────────────────┤
 * │ @Singleton               │ SingletonComponent             │ Entire app                        │ Retrofit, Room, OkHttp, ApiService│
 * │ @ActivityRetainedScoped  │ ActivityRetainedComponent      │ Activity + survives rotation       │ Rarely needed directly           │
 * │ @ViewModelScoped         │ ViewModelComponent             │ The ViewModel                     │ UseCases, Paginators             │
 * │ @ActivityScoped          │ ActivityComponent              │ The Activity                      │ Analytics, Navigation helpers    │
 * │ @FragmentScoped          │ FragmentComponent              │ The Fragment                      │ Fragment-specific helpers        │
 * │ @ServiceScoped           │ ServiceComponent               │ The Service                       │ Service-specific objects         │
 * │ (none)                   │ —                              │ New instance every injection      │ Stateless validators, formatters │
 * └──────────────────────────┴────────────────────────────────┴───────────────────────────────────┴──────────────────────────────────┘
 * ```
 *
 * **Scope Rules:**
 * ```kotlin
 * // Rule 1 — Scope annotation must match @InstallIn component
 *
 * // ✅ Correct — scope matches component
 * @InstallIn(ViewModelComponent::class)
 * // @ViewModelScoped on the @Provides/@Binds function
 *
 * // ❌ Wrong — scope does not match component → compile error
 * @InstallIn(SingletonComponent::class)
 * // @ViewModelScoped on the @Provides/@Binds function → MISMATCH
 *
 * // Rule 2 — Cannot inject a narrow scope into a wider scope
 *
 * // ❌ Wrong — @ActivityScoped cannot live inside @Singleton
 * @Singleton
 * class UserManager @Inject constructor(
 *     private val tracker: AnalyticsTracker  // @ActivityScoped inside @Singleton → ERROR
 *                                             // Activity gets destroyed but Singleton holds reference
 *                                             // → MEMORY LEAK
 * )
 *
 * // ✅ Correct — @Singleton (wider) into @ActivityScoped (narrower) is always fine
 * @ActivityScoped
 * class ScreenHelper @Inject constructor(
 *     private val retrofit: Retrofit  // @Singleton into @ActivityScoped → fine
 * )
 *
 * // Rule 3 — Recommended scopes per layer
 * // Repository  → @Singleton      (expensive to create, shared across app)
 * // UseCase     → @ViewModelScoped (lightweight, scoped to ViewModel lifetime)
 * // ApiService  → @Singleton      (one Retrofit instance for the whole app)
 * // Database    → @Singleton      (one Room instance for the whole app)
 * ```
 *
 * ---
 *
 * ## 14. Special Hilt Annotations for Android
 *
 * ```kotlin
 * // @HiltAndroidApp — starts the entire Hilt DI container
 * // Must be on your Application class — without this nothing works
 * @HiltAndroidApp
 * class MyApp : Application()
 *
 * // @AndroidEntryPoint — enables field injection
 * // Use on: Activity, Fragment, Service, BroadcastReceiver, ContentProvider
 * @AndroidEntryPoint
 * class UserFragment : Fragment() {
 *     @Inject lateinit var repository: UserRepository // filled by Hilt
 * }
 *
 * // @HiltViewModel — enables constructor injection in ViewModel
 * // Without this, Hilt cannot inject into ViewModel constructor
 * @HiltViewModel
 * class UserViewModel @Inject constructor(
 *     private val getUsers: GetUsersUseCase
 * ) : ViewModel()
 * ```
 *
 * ---
 *
 * ## 15. Special Context Injections
 *
 * ```kotlin
 * // @ApplicationContext — safe in any scope including @Singleton
 * // Never leaks — Application context lives as long as the app
 * @Module
 * @InstallIn(SingletonComponent::class)
 * object DatabaseModule {
 *
 *     @Provides
 *     @Singleton
 *     fun provideDatabase(
 *         @ApplicationContext context: Context  // safe — app context never leaks
 *     ): AppDatabase {
 *         return Room.databaseBuilder(context, AppDatabase::class.java, "app_db").build()
 *     }
 * }
 *
 * // @ActivityContext — only safe in @ActivityScoped and narrower
 * // NEVER inject into @Singleton — Activity gets destroyed but Singleton holds reference → LEAK
 * @Module
 * @InstallIn(ActivityComponent::class)
 * object GlideModule {
 *
 *     @Provides
 *     @ActivityScoped
 *     fun provideGlide(
 *         @ActivityContext context: Context  // safe — scoped to Activity lifetime
 *     ): RequestManager {
 *         return Glide.with(context)
 *     }
 * }
 * ```
 *
 * ---
 *
 * ## 16. Real Production Module Structure
 *
 * ```kotlin
 * // ─────────────────────────────────────────────────────
 * // NetworkModule — @Provides, @Singleton, object module
 * // ─────────────────────────────────────────────────────
 * @Module
 * @InstallIn(SingletonComponent::class)
 * object NetworkModule {
 *
 *     @Provides
 *     @Singleton
 *     fun provideOkHttpClient(): OkHttpClient =
 *         OkHttpClient.Builder()
 *             .connectTimeout(30, TimeUnit.SECONDS)
 *             .addInterceptor(HttpLoggingInterceptor())
 *             .build()
 *
 *     @Provides
 *     @Singleton
 *     fun provideRetrofit(client: OkHttpClient): Retrofit =
 *         Retrofit.Builder()
 *             .baseUrl("https://api.example.com")
 *             .client(client)
 *             .addConverterFactory(GsonConverterFactory.create())
 *             .build()
 *
 *     @Provides
 *     @Singleton
 *     fun provideApiService(retrofit: Retrofit): ApiService =
 *         retrofit.create(ApiService::class.java)
 * }
 *
 * // ─────────────────────────────────────────────────────
 * // HomeDataModule — @Binds, @Singleton, abstract class module
 * // Repositories go in SingletonComponent — expensive to create
 * // ─────────────────────────────────────────────────────
 * @Module
 * @InstallIn(SingletonComponent::class)
 * abstract class HomeDataModule {
 *
 *     @Binds
 *     @Singleton
 *     abstract fun bindHomeRepository(
 *         impl: HomeRepositoryImpl
 *     ): HomeRepository
 * }
 *
 * // ─────────────────────────────────────────────────────
 * // HomeUseCaseModule — @Binds, @ViewModelScoped, abstract class module
 * // UseCases go in ViewModelComponent — scoped to ViewModel lifetime
 * // ─────────────────────────────────────────────────────
 * @Module
 * @InstallIn(ViewModelComponent::class)
 * abstract class HomeUseCaseModule {
 *
 *     @Binds
 *     @ViewModelScoped
 *     abstract fun bindGetPopularWallpapers(
 *         impl: GetPopularWallpapersImpl
 *     ): GetPopularWallpapers
 *
 *     @Binds
 *     @ViewModelScoped
 *     abstract fun bindGetRandomWallpapers(
 *         impl: GetRandomWallpapersImpl
 *     ): GetRandomWallpapers
 *
 *     @Binds
 *     @ViewModelScoped
 *     abstract fun bindGetCategoriesUseCase(
 *         impl: GetCategoriesUseCaseImpl
 *     ): GetCategoriesUseCase
 *
 *     @Binds
 *     @ViewModelScoped
 *     abstract fun bindAddFavoriteUseCase(
 *         impl: AddFavoriteWallpaperUseCaseImpl
 *     ): AddFavoriteWallpaperUseCase
 *
 *     @Binds
 *     @ViewModelScoped
 *     abstract fun bindGetFavoriteUseCase(
 *         impl: GetFavoriteWallpaperUseCaseImpl
 *     ): GetFavoriteWallpaperUseCase
 *
 *     @Binds
 *     @ViewModelScoped
 *     abstract fun bindPostDataUseCase(
 *         impl: PostDataUseCaseImpl
 *     ): PostDataUseCase
 * }
 *
 * // ─────────────────────────────────────────────────────
 * // Mixed module — when you need both @Binds and @Provides
 * // Use abstract class with companion object
 * // ─────────────────────────────────────────────────────
 * @Module
 * @InstallIn(SingletonComponent::class)
 * abstract class AppModule {
 *
 *     // @Binds in abstract class — interface mappings
 *     @Binds
 *     @Singleton
 *     abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository
 *
 *     @Binds
 *     @Singleton
 *     abstract fun bindAnalyticsService(impl: AnalyticsServiceImpl): AnalyticsService
 *
 *     companion object {
 *         // @Provides in companion object — third-party / custom config
 *         @Provides
 *         @Singleton
 *         fun provideDatabase(@ApplicationContext ctx: Context): AppDatabase =
 *             Room.databaseBuilder(ctx, AppDatabase::class.java, "db").build()
 *
 *         @Provides
 *         @Singleton
 *         fun provideMoshi(): Moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
 *     }
 * }
 *
 * // ─────────────────────────────────────────────────────
 * // Full wiring — how everything connects
 * // ─────────────────────────────────────────────────────
 * //
 * // @HiltAndroidApp MyApp
 * //         ↓
 * // NetworkModule   → ApiService (@Singleton)
 * // HomeDataModule  → HomeRepository (@Singleton) ← ApiService, DataManager
 * // HomeUseCaseModule → GetPopularWallpapers (@ViewModelScoped) ← HomeRepository
 * //         ↓
 * // @HiltViewModel HomeViewModel ← GetPopularWallpapers, GetRandomWallpapers
 * //         ↓
 * // @AndroidEntryPoint HomeFragment → by viewModels()
 * ```
 *
 * ---
 *
 * ## 17. Common Mistakes — Never Do These
 *
 * ```kotlin
 * // ❌ Mistake 1 — @Provides body is just return impl
 * @Provides
 * fun provideRepository(impl: UserRepositoryImpl): UserRepository = impl
 * // Fix: Use @Binds instead
 *
 * // ❌ Mistake 2 — No scope annotation — new instance on every injection
 * @Module
 * @InstallIn(SingletonComponent::class)
 * object NetworkModule {
 *     @Provides
 *     // Missing @Singleton — Retrofit gets recreated on every injection
 *     fun provideRetrofit(): Retrofit = Retrofit.Builder().build()
 * }
 * // Fix: Add @Singleton
 *
 * // ❌ Mistake 3 — Scope annotation does not match @InstallIn component
 * @Module
 * @InstallIn(SingletonComponent::class)
 * abstract class UseCaseModule {
 *     @Binds
 *     @ViewModelScoped  // mismatch with SingletonComponent → COMPILE ERROR
 *     abstract fun bindUseCase(impl: UseCaseImpl): UseCase
 * }
 * // Fix: Change @InstallIn to ViewModelComponent::class
 *
 * // ❌ Mistake 4 — @ActivityContext inside @Singleton → MEMORY LEAK
 * @Module
 * @InstallIn(SingletonComponent::class)
 * object BadModule {
 *     @Provides
 *     @Singleton
 *     fun provideSomething(
 *         @ActivityContext ctx: Context  // Activity dies but Singleton holds reference → LEAK
 *     ): Something = Something(ctx)
 * }
 * // Fix: Use @ApplicationContext instead
 *
 * // ❌ Mistake 5 — Injecting narrow scope into wider scope
 * @Singleton
 * class UserManager @Inject constructor(
 *     private val tracker: AnalyticsTracker  // @ActivityScoped → ERROR at compile time
 * )
 * // Fix: Use @ApplicationContext or restructure scope hierarchy
 *
 * // ❌ Mistake 6 — One giant module for everything
 * @Module
 * @InstallIn(SingletonComponent::class)
 * object GodModule {
 *     // repositories + use cases + network + database + analytics all in one file
 * }
 * // Fix: Split by layer — NetworkModule, DatabaseModule, RepositoryModule, UseCaseModule
 *
 * // ❌ Mistake 7 — @Binds without @Inject on implementation
 * class UserRepositoryImpl(  // Missing @Inject constructor
 *     private val api: ApiService
 * ) : UserRepository
 *
 * @Module
 * @InstallIn(SingletonComponent::class)
 * abstract class RepositoryModule {
 *     @Binds
 *     abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository
 *     // ERROR — Hilt cannot build UserRepositoryImpl without @Inject constructor
 * }
 * // Fix: Add @Inject constructor to UserRepositoryImpl
 * ```
 *
 * ---
 *
 * ## 18. Decision Tree — What to Use When
 *
 * ```
 * Need to provide a dependency?
 * │
 * ├── Do you own the class?
 * │    ├── YES → Add @Inject constructor — NO module needed ✅
 * │    │         class MyRepo @Inject constructor(...)
 * │    │
 * │    └── NO → Need a @Module
 * │              │
 * │              ├── Is it an interface?
 * │              │    └── YES → @Binds in abstract class module
 * │              │              Implementation must have @Inject constructor
 * │              │
 * │              └── Is it third-party / needs config / unknown params?
 * │                   └── YES → @Provides in object module
 * │                             or companion object if mixed with @Binds
 * │
 * ├── Do you inject the concrete class or the interface?
 * │    ├── Concrete (UserRepositoryImpl) → @Inject constructor, no module
 * │    └── Interface (UserRepository)    → @Binds module to resolve the mapping
 * │
 * └── What scope?
 *      ├── Expensive, shared app-wide       → @Singleton + SingletonComponent
 *      ├── Shared within one ViewModel      → @ViewModelScoped + ViewModelComponent
 *      ├── Shared within one Activity       → @ActivityScoped + ActivityComponent
 *      ├── Shared within one Fragment       → @FragmentScoped + FragmentComponent
 *      └── Stateless / cheap / no sharing  → No scope (new instance every time)
 * ```
 *
 * ---
 *
 * ## 19. One Page Cheat Sheet
 *
 * ```
 * ┌─────────────────────────┬────────────────────────────────────────────────────────────────┐
 * │ Annotation              │ Meaning                                                        │
 * ├─────────────────────────┼────────────────────────────────────────────────────────────────┤
 * │ @Inject constructor     │ Hilt, you are responsible for building this class              │
 * │ @Inject (field)         │ Hilt, fill this field — only for Activity/Fragment/Service     │
 * │ @Module                 │ Recipe book — teaches Hilt how to build things                 │
 * │ @InstallIn              │ Mandatory on every module — which component it lives in        │
 * │ @Provides               │ I will write the creation logic — has a function body          │
 * │ @Binds                  │ Map this interface to this impl — no body, just a declaration  │
 * │ companion object        │ Where @Provides live inside an abstract class — static calls   │
 * │ @Singleton              │ One instance, entire app lifetime                              │
 * │ @ViewModelScoped        │ One instance per ViewModel                                     │
 * │ @ActivityScoped         │ One instance per Activity                                      │
 * │ @FragmentScoped         │ One instance per Fragment                                      │
 * │ @ServiceScoped          │ One instance per Service                                       │
 * │ (no scope)              │ New instance on every injection                                │
 * │ @HiltAndroidApp         │ Start Hilt — must be on Application class                      │
 * │ @AndroidEntryPoint      │ Enable field injection on Activity, Fragment, Service          │
 * │ @HiltViewModel          │ Enable constructor injection on ViewModel                      │
 * │ @ApplicationContext     │ Inject app context — safe in any scope including @Singleton    │
 * │ @ActivityContext        │ Inject activity context — only safe in @ActivityScoped         │
 * └─────────────────────────┴────────────────────────────────────────────────────────────────┘
 *
 * Module Type Quick Pick:
 *  object module        → all @Provides functions
 *  abstract class       → all @Binds functions
 *  abstract class
 *   + companion object  → mixed @Binds and @Provides
 *
 * Scope Quick Pick:
 *  Retrofit, Room, OkHttp, Database → @Singleton
 *  Repository                       → @Singleton
 *  UseCase                          → @ViewModelScoped
 *  Analytics, Navigator             → @ActivityScoped
 *  Stateless helpers, validators    → No scope
 * ```
 */
class Kdoc