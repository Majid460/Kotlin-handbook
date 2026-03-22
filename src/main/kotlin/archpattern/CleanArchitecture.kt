@file:Suppress("unused")

package archpattern

import kotlinx.serialization.SerialName

/*
 * ════════════════════════════════════════════════════════════════════════
 *  CLEAN ARCHITECTURE — COMPLETE MODELS & MAPPERS
 *  Author  : Majid
 *  Purpose : Production-grade reference for all model layers.
 *            Every class has full KDoc explaining WHAT it is,
 *            WHERE it lives, WHY it exists, and HOW it differs
 *            from similar classes in other layers.
 * ════════════════════════════════════════════════════════════════════════
 *
 *  LAYER MAP
 *  ─────────────────────────────────────────────────────────────────────
 *  1. DATA / REMOTE  → NewsDto, NewsMetaDto
 *  2. DATA / LOCAL   → NewsEntity
 *  3. DOMAIN         → News  (pure Kotlin — zero Android imports)
 *  4. PRESENTATION   → NewsUiModel, NewsUiState, NewsUiEvent
 *  5. MAPPERS        → NewsDataMapper, NewsUiMapper
 *
 *  DATA FLOW
 *  ─────────────────────────────────────────────────────────────────────
 *  API JSON
 *    └─► [Retrofit] ──► NewsDto
 *                          └─► [NewsDataMapper.toDomain()]  ──► News
 *                                  ├─► [NewsDataMapper.toEntity()] ──► NewsEntity ──► Room
 *                                  └─► [NewsUiMapper.toUiModel()]  ──► NewsUiModel ──► UI
 *
 *  Room
 *    └─► NewsEntity
 *            └─► [NewsDataMapper.toDomain()] ──► News
 *                    └─► [NewsUiMapper.toUiModel()] ──► NewsUiModel ──► UI
 *
 *  THE ONE RULE
 *  ─────────────────────────────────────────────────────────────────────
 *  Each model contains ONLY what its own layer needs.
 *  Mappers are the ONLY classes allowed to know about two layers at once.
 * ════════════════════════════════════════════════════════════════════════
 */

// ─────────────────────────────────────────────────────────────────────────
// Simulated annotations
// In a real project these come from:
//   androidx.room:room-runtime  →  @Entity, @PrimaryKey, @ColumnInfo
//   com.google.code.gson:gson   →  @SerializedName
// ─────────────────────────────────────────────────────────────────────────

annotation class Entity(val tableName: String = "")
annotation class PrimaryKey
annotation class ColumnInfo(val name: String = "")


// ════════════════════════════════════════════════════════════════════════
//  LAYER 1 — DATA / REMOTE
//  Real package : com.app.data.remote.dto
//  Gradle module: :data
// ════════════════════════════════════════════════════════════════════════

/**
 * **DTO — Data Transfer Object for a remote news article.**
 *
 * **Package:** `com.app.data.remote.dto`
 * **Module:**  `:data`
 *
 * ---
 *
 * ### What is a DTO?
 * A DTO (Data Transfer Object) is a class whose sole job is to model
 * exactly what the remote API sends back over the wire as JSON.
 * It is shaped by the **backend's** naming conventions, not ours.
 *
 * ### Why does this class exist separately from [News]?
 * The API uses inconsistent, abbreviated field names (`_id`, `ttl`, `img_url`).
 * Rather than polluting the rest of the app with these names, we absorb
 * all the inconsistency here using `@SerializedName` so the domain model
 * [News] can use clean, readable property names.
 *
 * ### Strict rules for this class
 * - Only used inside the `:data` module.
 * - Only passed to [NewsDataMapper.toDomain] — never further.
 * - Never passed into the Domain or Presentation layers.
 * - Never contains business logic or formatting.
 *
 * ### Key structural difference from [NewsEntity]
 * Contains a nested object [NewsMetaDto]. Room cannot store nested objects,
 * so [NewsDataMapper] **flattens** meta into top-level columns on [NewsEntity].
 *
 * @property id           Unique article identifier. API field: `_id`.
 * @property title        Article headline. API field: `ttl` (backend abbreviation).
 * @property content      Full article body. API field: `body`.
 * @property publishedAt  Publication time as Unix epoch milliseconds. API field: `published_at`.
 * @property imageUrl     Cover image URL string. API field: `img_url`.
 * @property isBreaking   True when the article is flagged as breaking news. API field: `is_breaking`.
 * @property meta         Nested metadata object containing author and category. Flattened by mapper.
 *
 * @see NewsMetaDto            Nested object inside this DTO.
 * @see NewsDataMapper.toDomain The only place that should consume this class.
 */
data class NewsDto(
    @SerialName("_id")
    val id: String,

    @SerialName("ttl")
    val title: String,

    @SerialName("body")
    val content: String,

    @SerialName("published_at")
    val publishedAt: Long,

    @SerialName("img_url")
    val imageUrl: String,

    @SerialName("is_breaking")
    val isBreaking: Boolean,

    @SerialName("meta")
    val meta: NewsMetaDto
)

/**
 * **Nested metadata DTO embedded inside [NewsDto].**
 *
 * **Package:** `com.app.data.remote.dto`
 * **Module:**  `:data`
 *
 * ---
 *
 * ### Why does this exist as a separate class?
 * The remote API wraps author and category inside a nested JSON object:
 * ```json
 * {
 *   "_id": "abc",
 *   "meta": {
 *     "author": "John Smith",
 *     "category": "technology"
 *   }
 * }
 * ```
 * Gson requires a matching Kotlin class to deserialize the nested object.
 * This class mirrors that structure exactly.
 *
 * ### What happens to this after parsing?
 * [NewsDataMapper] flattens [author] and [category] into top-level
 * fields on [News] and [NewsEntity]. This class is never used beyond the mapper.
 *
 * @property author   Full name of the article author.
 * @property category Category identifier string e.g. `"technology"`, `"sports"`.
 *
 * @see NewsDto        Parent DTO that contains this object.
 * @see NewsDataMapper Flattens this into [News.author] and [News.category].
 */
data class NewsMetaDto(
    @SerialName("author")
    val author: String,

    @SerialName("category")
    val category: String
)


// ════════════════════════════════════════════════════════════════════════
//  LAYER 2 — DATA / LOCAL
//  Real package : com.app.data.local.entity
//  Gradle module: :data
// ════════════════════════════════════════════════════════════════════════

/**
 * **Room Entity — local database representation of a cached news article.**
 *
 * **Package:** `com.app.data.local.entity`
 * **Module:**  `:data`
 *
 * ---
 *
 * ### What is a Room Entity?
 * A Room Entity is a Kotlin class annotated with `@Entity` that maps
 * directly to a table in the SQLite database. Each property maps to
 * a column in that table.
 *
 * ### Why does this class exist separately from [News]?
 * Database storage has concerns the business model should not know about:
 * - `@Entity`, `@PrimaryKey`, `@ColumnInfo` are database annotations.
 * - [cachedAt] tracks when the row was written for cache expiry checks.
 * - Flattened fields — SQLite cannot store nested objects like [NewsMetaDto].
 *
 * ### Key differences from [NewsDto]
 * | Feature        | NewsDto            | NewsEntity                   |
 * |----------------|--------------------|------------------------------|
 * | Shaped for     | JSON parsing       | SQLite storage               |
 * | Nested objects | Yes (NewsMetaDto)  | No — meta is flattened       |
 * | Extra fields   | None               | cachedAt for expiry          |
 * | Annotations    | @SerializedName    | @Entity, @ColumnInfo         |
 *
 * ### Key differences from [News] (Domain)
 * | Feature        | NewsEntity                | News                       |
 * |----------------|---------------------------|----------------------------|
 * | Purpose        | Store in SQLite           | Business logic             |
 * | cachedAt       | Present                   | Absent — not business      |
 * | Annotations    | @Entity, @PrimaryKey      | None whatsoever            |
 * | Android dep.   | Yes (Room)                | Zero                       |
 *
 * @property id          Primary key. Matches [News.id] and [NewsDto.id].
 * @property title       Article headline.
 * @property content     Full article body.
 * @property imageUrl    Cover image URL string.
 * @property isBreaking  True when this is a breaking news article.
 * @property publishedAt Unix epoch milliseconds of original publication.
 * @property author      Flattened from [NewsMetaDto.author]. SQLite cannot store nested objects.
 * @property category    Flattened from [NewsMetaDto.category]. SQLite cannot store nested objects.
 * @property cachedAt    Unix epoch milliseconds of when this row was inserted.
 *                       Used by the repository to determine whether cached data has expired.
 *                       This field does not exist in [News] — it is purely a database concern.
 *
 * @see NewsDataMapper.toEntity  Converts [News] into [NewsEntity] for storage.
 * @see NewsDataMapper.toDomain  Converts [NewsEntity] into [News] for business use.
 */
@Entity(tableName = "news")
data class NewsEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "content")
    val content: String,

    @ColumnInfo(name = "image_url")
    val imageUrl: String,

    @ColumnInfo(name = "is_breaking")
    val isBreaking: Boolean,

    @ColumnInfo(name = "published_at")
    val publishedAt: Long,

    @ColumnInfo(name = "author")
    val author: String,

    @ColumnInfo(name = "category")
    val category: String,

    @ColumnInfo(name = "cached_at")
    val cachedAt: Long = System.currentTimeMillis()
)


// ════════════════════════════════════════════════════════════════════════
//  LAYER 3 — DOMAIN
//  Real package : com.app.domain.model
//  Gradle module: :domain
// ════════════════════════════════════════════════════════════════════════

/**
 * **Domain model — the single source of truth for what a News article is.**
 *
 * **Package:** `com.app.domain.model`
 * **Module:**  `:domain`
 *
 * ---
 *
 * ### The most important model in the app
 * This is the heart of Clean Architecture. This class represents
 * the business concept of a "News article" with zero awareness of:
 * - How it was fetched (API, database, cache, mock).
 * - How it will be displayed (formatting, labels, truncation).
 * - Any Android framework class whatsoever.
 *
 * ### Why zero Android imports?
 * Because the domain layer must be testable with plain JUnit — no
 * emulator, no Robolectric, no Android test runner. If [News] imported
 * anything from `android.*` or `androidx.*`, it would break that guarantee.
 *
 * ### Comparison with [NewsDto]
 * ```
 * NewsDto.id       → "_id"         (API field name — abbreviated)
 * News.id          → "id"          (clean, readable name)
 *
 * NewsDto.title    → "ttl"         (API abbreviation)
 * News.title       → "title"       (readable)
 *
 * NewsDto.meta     → NewsMetaDto   (nested object)
 * News.author      → String        (flattened by mapper)
 * News.category    → String        (flattened by mapper)
 * ```
 *
 * ### Comparison with [NewsEntity]
 * ```
 * NewsEntity.cachedAt → Long       (database-only field for expiry)
 * News                → absent     (not a business concern)
 *
 * NewsEntity          → has @Entity, @PrimaryKey, @ColumnInfo
 * News                → has no annotations whatsoever
 * ```
 *
 * ### Comparison with [NewsUiModel]
 * ```
 * News.publishedAt        → 1711234567000L   (raw Long)
 * NewsUiModel.formattedDate → "2 hours ago"  (formatted by mapper)
 *
 * News.author             → "John Smith"
 * NewsUiModel.authorLabel → "By John Smith"  (label added by mapper)
 *
 * News.content            → full body text   (thousands of chars)
 * NewsUiModel.contentPreview → "first 100..."(truncated by mapper)
 * ```
 *
 * @property id          Unique identifier for this article.
 * @property title       Raw headline text. No formatting applied.
 * @property content     Full article body text. No truncation.
 * @property imageUrl    URL string of the cover image.
 *                       Image loading is handled by Coil in the UI — not here.
 * @property isBreaking  Business flag indicating if this is breaking news.
 * @property publishedAt Raw Unix epoch timestamp in milliseconds.
 *                       Formatting into "2 hours ago" is done by [NewsUiMapper].
 * @property author      Author name as a plain string. Prefix label added by [NewsUiMapper].
 * @property category    Category as a plain lowercase string e.g. `"technology"`.
 *                       Uppercasing for badge display is done by [NewsUiMapper].
 *
 * @see NewsDataMapper Creates [News] from [NewsDto] or [NewsEntity].
 * @see NewsUiMapper   Creates [NewsUiModel] from [News].
 */
data class News(
    val id: String,
    val title: String,
    val content: String,
    val imageUrl: String,
    val isBreaking: Boolean,
    val publishedAt: Long,
    val author: String,
    val category: String
)


// ════════════════════════════════════════════════════════════════════════
//  LAYER 4 — PRESENTATION
//  Real package : com.app.feature.news.model
//  Gradle module: :feature:news
// ════════════════════════════════════════════════════════════════════════

/**
 * **UI model — display-ready representation of a news article for the UI layer.**
 *
 * **Package:** `com.app.feature.news.model`
 * **Module:**  `:feature:news`
 *
 * ---
 *
 * ### Why does this exist separately from [News]?
 * Composables and ViewHolders should do ONE thing: **render**.
 * They should never format timestamps, build label strings,
 * or truncate content. Those are presentation concerns that
 * belong in [NewsUiMapper] — not in the UI itself.
 *
 * This model carries pre-formatted, display-ready values so that
 * every field is bound directly to a View or Composable with zero
 * additional transformation needed.
 *
 * ### Strict rules for this class
 * - Created exclusively by [NewsUiMapper].
 * - Consumed exclusively by Composables, ViewHolders, and ViewModel state.
 * - Never passed to the Domain or Data layers.
 * - Never passed to a Repository, UseCase, or DAO.
 *
 * ### Comparison with [News]
 * | Property              | News                      | NewsUiModel                       |
 * |-----------------------|---------------------------|-----------------------------------|
 * | Date                  | `publishedAt: Long`       | `formattedDate: "2 hours ago"`    |
 * | Author                | `author: "John Smith"`    | `authorLabel: "By John Smith"`    |
 * | Category              | `category: "technology"`  | `categoryBadge: "TECHNOLOGY"`     |
 * | Content               | Full body (1000+ chars)   | `contentPreview: "first 100..."` |
 * | Raw timestamp present | Yes                       | No — UI does not need it          |
 *
 * @property id             Unique identifier. Used as the stable key for DiffUtil in
 *                          RecyclerView adapters and as the `key {}` in Compose LazyColumn
 *                          to prevent unnecessary redraws when the list updates.
 * @property title          Display-ready headline. Bound directly to TextView or Text().
 * @property imageUrl       Cover image URL. Passed directly to Coil's AsyncImage or
 *                          imageView.load(). Coil handles threading, caching, and lifecycle.
 * @property isBreaking     Controls the visibility of the breaking news badge in the UI.
 * @property formattedDate  Human-readable relative time e.g. `"Just now"`, `"3 hours ago"`.
 *                          Computed by [NewsUiMapper.formatRelativeTime].
 * @property authorLabel    Display-ready author label e.g. `"By John Smith"`.
 *                          Computed by [NewsUiMapper.buildAuthorLabel].
 * @property categoryBadge  Uppercase category string for a badge chip e.g. `"TECHNOLOGY"`.
 * @property contentPreview First 100 characters of article body followed by `"..."`.
 *                          Used as the subtitle in list items.
 *
 * @see NewsUiMapper.toUiModel The only place that creates instances of this class.
 */
data class NewsUiModel(
    val id: String,
    val title: String,
    val imageUrl: String,
    val isBreaking: Boolean,
    val formattedDate: String,
    val authorLabel: String,
    val categoryBadge: String,
    val contentPreview: String
)

/**
 * **Sealed class representing every possible state of the News screen.**
 *
 * **Package:** `com.app.feature.news.model`
 * **Module:**  `:feature:news`
 *
 * ---
 *
 * ### Why a sealed class instead of multiple booleans?
 * A naive approach uses multiple boolean flags:
 * ```kotlin
 * // BAD — multiple booleans create illegal state combinations
 * data class NewsScreenState(
 *     val isLoading: Boolean,
 *     val isError: Boolean,
 *     val isEmpty: Boolean,
 *     val items: List<NewsUiModel>
 * )
 * // What does isLoading=true AND isError=true mean?
 * // That is an illegal state — but the type system silently allows it.
 * ```
 * A sealed class makes **illegal states unrepresentable**:
 * ```kotlin
 * // GOOD — exactly one state is active at a time
 * val state: NewsUiState = NewsUiState.Loading
 * // Compiler enforces exhaustive when branches — you cannot forget a case.
 * ```
 *
 * ### Why is [Empty] separate from [Error]?
 * An empty list is a **valid successful response** — the server returned HTTP 200
 * with zero items. That is fundamentally different from a failure (network error,
 * HTTP 500). The UI shows an empty-state illustration for [Empty] and a retry
 * button for [Error]. Conflating them would cause wrong UI behaviour.
 *
 * ### ViewModel usage
 * ```kotlin
 * _uiState.value = NewsUiState.Loading
 * _uiState.value = NewsUiState.Success(items)
 * _uiState.value = NewsUiState.Error("No internet connection")
 * _uiState.value = NewsUiState.Empty
 * ```
 *
 * ### UI usage — exhaustive when (compiler enforced)
 * ```kotlin
 * when (val state = uiState) {
 *     is NewsUiState.Loading  -> ShowSpinner()
 *     is NewsUiState.Success  -> NewsList(state.items)
 *     is NewsUiState.Error    -> ErrorView(state.message)
 *     is NewsUiState.Empty    -> EmptyView()
 * }
 * ```
 */
sealed class NewsUiState {

    /**
     * Initial loading state shown while data is being fetched for the first time.
     *
     * **UI behaviour:** Display a ProgressBar or Shimmer skeleton.
     * Do not show the list or the error view.
     */
    object Loading : NewsUiState()

    /**
     * Data successfully loaded and ready to display.
     *
     * **UI behaviour:** Hide the progress bar. Submit [items] to the
     * ListAdapter via `submitList()` and show the RecyclerView.
     *
     * @property items Non-empty list of display-ready [NewsUiModel] objects.
     *                 Guaranteed non-empty — see [Empty] for the zero-item case.
     */
    data class Success(val items: List<NewsUiModel>) : NewsUiState()

    /**
     * A failure occurred while loading or refreshing data.
     *
     * **UI behaviour:** Hide the progress bar and list.
     * Show an error message and a **Retry** button that re-triggers the fetch.
     *
     * @property message Human-readable description of the failure.
     *                   May be `null` if the exception carried no message.
     */
    data class Error(val message: String?) : NewsUiState()

    /**
     * Data loaded successfully but the server returned zero articles.
     *
     * **UI behaviour:** Hide the progress bar and error view.
     * Show an empty-state illustration e.g. "No news available right now."
     *
     * **Why separate from [Error]?**
     * An empty list is a valid 200 response — not a failure.
     * Showing a retry button for an empty state would confuse the user.
     */
    object Empty : NewsUiState()
}

/**
 * **Sealed class representing one-time UI events that must fire exactly once.**
 *
 * **Package:** `com.app.feature.news.model`
 * **Module:**  `:feature:news`
 *
 * ---
 *
 * ### Why SharedFlow and not StateFlow for events?
 * **StateFlow** always replays its last value to new collectors.
 * If a Snackbar event were stored in StateFlow, rotating the screen would
 * cause a new collector to receive the stale event and show the Snackbar
 * again — a bug the user would notice every time they rotate.
 *
 * **SharedFlow** with `replay = 0` fires an event to active collectors
 * once and never replays it. This is exactly the behaviour needed for
 * navigation, Snackbars, and one-time scroll commands.
 *
 * ### ViewModel setup
 * ```kotlin
 * private val _uiEvent = MutableSharedFlow<NewsUiEvent>(
 *     replay = 0,
 *     extraBufferCapacity = 1,
 *     onBufferOverflow = BufferOverflow.DROP_OLDEST
 * )
 * val uiEvent: SharedFlow<NewsUiEvent> = _uiEvent.asSharedFlow()
 * ```
 *
 * ### UI collection — inside repeatOnLifecycle
 * ```kotlin
 * viewModel.uiEvent.collect { event ->
 *     when (event) {
 *         is NewsUiEvent.ShowSnackbar     -> showSnackbar(event.message)
 *         is NewsUiEvent.NavigateToDetail -> navigateTo(event.newsId)
 *         is NewsUiEvent.ScrollToTop      -> recyclerView.smoothScrollToPosition(0)
 *     }
 * }
 * ```
 */
sealed class NewsUiEvent {

    /**
     * Instructs the UI to show a Snackbar with the given message.
     *
     * **When emitted:** Background sync completes, a non-fatal error occurs,
     * or an action confirmation is needed (e.g. "Article saved").
     *
     * @property message Text to display inside the Snackbar.
     */
    data class ShowSnackbar(val message: String) : NewsUiEvent()

    /**
     * Instructs the UI to navigate to the detail screen for the given article.
     *
     * **When emitted:** User taps a news item in the list.
     * The ViewModel emits this event; the Activity or Fragment handles the
     * actual Navigation Component call. This keeps navigation out of the
     * ViewModel while still allowing it to trigger navigation.
     *
     * @property newsId The ID of the article to show on the detail screen.
     *                  Passed as a Navigation Component route argument.
     */
    data class NavigateToDetail(val newsId: String) : NewsUiEvent()

    /**
     * Instructs the UI to scroll the news list back to the top.
     *
     * **When emitted:** After a pull-to-refresh completes successfully
     * so the user immediately sees the newest content at position 0.
     */
    object ScrollToTop : NewsUiEvent()
}


// ════════════════════════════════════════════════════════════════════════
//  LAYER 5 — MAPPERS
//  Mappers are the ONLY classes allowed to import from two layers at once.
//  They translate models across layer boundaries cleanly.
// ════════════════════════════════════════════════════════════════════════

/**
 * **Mapper between the Data layer and the Domain layer.**
 *
 * **Package:** `com.app.data.mapper`
 * **Module:**  `:data`
 *
 * ---
 *
 * ### Responsibility
 * Translates between Data layer models ([NewsDto], [NewsEntity])
 * and the Domain layer model ([News]).
 *
 * This is the **only** class in the project permitted to know about both:
 * - Data models: [NewsDto], [NewsEntity]
 * - Domain model: [News]
 *
 * ### Why not put mapping inside the model itself?
 * If [NewsDto] had a `fun toDomain(): News` extension, the Data layer
 * would import the Domain layer — which is technically acceptable but
 * mixes transport concerns with translation logic. A dedicated mapper
 * is cleaner, independently testable, and makes layer boundaries explicit.
 *
 * ### Method summary
 * | Method              | Input        | Output       | When to call                  |
 * |---------------------|--------------|--------------|-------------------------------|
 * | toDomain(dto)       | [NewsDto]    | [News]       | After Retrofit parses JSON    |
 * | toEntity(domain)    | [News]       | [NewsEntity] | Before inserting into Room    |
 * | toDomain(entity)    | [NewsEntity] | [News]       | After reading from Room       |
 *
 * @see NewsUiMapper Companion mapper for the Domain to Presentation direction.
 */
class NewsDataMapper {

    /**
     * Converts a remote API response [NewsDto] into a clean domain [News] object.
     *
     * ### What this does
     * - Renames abbreviated API field names to readable domain property names.
     * - Flattens the nested [NewsMetaDto] into top-level [News] properties.
     *
     * ### What this does NOT do
     * - Does not format any strings — formatting belongs in [NewsUiMapper].
     * - Does not add [NewsEntity.cachedAt] — that belongs in [toEntity].
     *
     * @param dto The raw API response object parsed by Retrofit and Gson.
     * @return    A clean [News] domain object with no API-specific concerns.
     *
     * Example:
     * ```kotlin
     * val news: News = mapper.toDomain(dto)
     * // news.author == "Jane"   (flattened from dto.meta.author)
     * // news.category == "tech" (flattened from dto.meta.category)
     * ```
     */
    fun toDomain(dto: NewsDto): News {
        return News(
            id = dto.id,
            title = dto.title,
            content = dto.content,
            imageUrl = dto.imageUrl,
            isBreaking = dto.isBreaking,
            publishedAt = dto.publishedAt,
            author = dto.meta.author,   // flattened from nested NewsMetaDto
            category = dto.meta.category  // flattened from nested NewsMetaDto
        )
    }

    /**
     * Converts a domain [News] object into a [NewsEntity] ready for Room storage.
     *
     * ### What this adds
     * Sets [NewsEntity.cachedAt] to the current system time.
     * This timestamp is used by the repository to check cache expiry.
     * It is a database concern — it does not exist in [News].
     *
     * @param domain The business object to persist into the local database.
     * @return       A [NewsEntity] ready to be inserted via a Room DAO.
     *
     * Example:
     * ```kotlin
     * val entity: NewsEntity = mapper.toEntity(news)
     * newsDao.insert(entity)
     * // entity.cachedAt == System.currentTimeMillis() at the time of mapping
     * ```
     */
    fun toEntity(domain: News): NewsEntity {
        return NewsEntity(
            id = domain.id,
            title = domain.title,
            content = domain.content,
            imageUrl = domain.imageUrl,
            isBreaking = domain.isBreaking,
            publishedAt = domain.publishedAt,
            author = domain.author,
            category = domain.category,
            cachedAt = System.currentTimeMillis() // DB-only field added here
        )
    }

    /**
     * Converts a [NewsEntity] retrieved from Room into a domain [News] object.
     *
     * ### What this intentionally drops
     * [NewsEntity.cachedAt] is omitted. The cache timestamp has no business
     * meaning and the domain layer should never know about caching internals.
     *
     * @param entity The database row retrieved from Room via a DAO query.
     * @return       A clean [News] domain object with no database-specific concerns.
     *
     * Example:
     * ```kotlin
     * val entities: List<NewsEntity> = newsDao.getAll()
     * val domainList: List<News> = entities.map { mapper.toDomain(it) }
     * ```
     */
    fun toDomain(entity: NewsEntity): News {
        return News(
            id = entity.id,
            title = entity.title,
            content = entity.content,
            imageUrl = entity.imageUrl,
            isBreaking = entity.isBreaking,
            publishedAt = entity.publishedAt,
            author = entity.author,
            category = entity.category
            // cachedAt intentionally omitted — not a business concern
        )
    }
}

/**
 * **Mapper from the Domain layer to the Presentation layer.**
 *
 * **Package:** `com.app.feature.news.mapper`
 * **Module:**  `:feature:news`
 *
 * ---
 *
 * ### Responsibility
 * Converts a clean domain [News] object into a display-ready [NewsUiModel]
 * by applying all formatting, labelling, and truncation logic.
 *
 * ### Why not format inside the Composable or ViewHolder?
 * Putting formatting logic inside the UI makes it:
 * - Untestable — you would need a UI test just to verify "2 hours ago" logic.
 * - Duplicated — if two screens show the same field, both format it separately.
 * - Hard to change — updating the date format requires touching every screen.
 *
 * Centralising all formatting here means it can be verified with plain JUnit
 * and updated in exactly one place.
 *
 * ### Strict rules
 * - Only imports from `:domain` and `:feature:news`.
 * - Never imports from `:data` (no DTO or Entity knowledge allowed).
 *
 * @see NewsDataMapper Handles the Data to Domain direction.
 */
class NewsUiMapper {

    /**
     * Converts a domain [News] object into a display-ready [NewsUiModel].
     *
     * Applies all formatting decisions in one place:
     * - Timestamp converted to relative time via [formatRelativeTime].
     * - Author name prefixed via [buildAuthorLabel].
     * - Category uppercased for badge display.
     * - Content truncated to a preview via [buildContentPreview].
     *
     * @param domain The business object received from a UseCase or Repository.
     * @return       A [NewsUiModel] where every field is ready for direct rendering.
     *
     * Example:
     * ```kotlin
     * val uiModel = mapper.toUiModel(news)
     * titleTextView.text  = uiModel.title          // no formatting needed in UI
     * dateTextView.text   = uiModel.formattedDate  // already "2 hours ago"
     * authorTextView.text = uiModel.authorLabel    // already "By John Smith"
     * ```
     */
    fun toUiModel(domain: News): NewsUiModel {
        return NewsUiModel(
            id = domain.id,
            title = domain.title,
            imageUrl = domain.imageUrl,
            isBreaking = domain.isBreaking,
            formattedDate = formatRelativeTime(domain.publishedAt),
            authorLabel = buildAuthorLabel(domain.author),
            categoryBadge = domain.category.uppercase(),
            contentPreview = buildContentPreview(domain.content)
        )
    }

    /**
     * Converts a Unix epoch timestamp into a human-readable relative time string.
     *
     * ### Output examples
     * | Elapsed time | Output              |
     * |--------------|---------------------|
     * | < 1 minute   | `"Just now"`        |
     * | 1–59 minutes | `"5 minutes ago"`   |
     * | 1–23 hours   | `"3 hours ago"`     |
     * | 1+ days      | `"2 days ago"`      |
     *
     * @param timestamp Unix epoch time in milliseconds.
     * @return          Human-readable relative time string for display.
     */
    private fun formatRelativeTime(timestamp: Long): String {
        val diffMs = System.currentTimeMillis() - timestamp
        val minutes = diffMs / (1_000L * 60)
        val hours = minutes / 60
        val days = hours / 24

        return when {
            minutes < 1L -> "Just now"
            minutes < 60L -> "$minutes minutes ago"
            hours < 24L -> "$hours hours ago"
            else -> "$days days ago"
        }
    }

    /**
     * Prepends `"By "` to the author's name to produce a display label.
     *
     * Example:
     * ```
     * "John Smith"  →  "By John Smith"
     * ```
     *
     * @param author Raw author name string from [News.author].
     * @return       Display-ready author label for a TextView or Text() composable.
     */
    private fun buildAuthorLabel(author: String): String = "By $author"

    /**
     * Truncates article body content to a short preview for list item subtitles.
     *
     * Appends `"..."` only when truncation actually occurs, to avoid displaying
     * `"Short text..."` when the full content fits within the limit.
     *
     * Example:
     * ```
     * // content.length > 100
     * "Lorem ipsum dolor sit amet, consectetur..."  →  "Lorem ipsum dolor sit amet, consect..."
     *
     * // content.length <= 100
     * "Short article."  →  "Short article."   (unchanged, no ellipsis added)
     * ```
     *
     * @param content Full article body from [News.content].
     * @return        A string of at most 100 characters, with `"..."` appended if truncated.
     */
    private fun buildContentPreview(content: String): String {
        return if (content.length > 100) {
            "${content.take(100)}..."
        } else {
            content
        }
    }
}


/**
`  ════════════════════════════════════════════════════════════════════════`
## QUICK REFERENCE
`    ════════════════════════════════════════════════════════════════════════`

 * ```
 *  MODEL LOCATION TABLE
 *  ─────────────────────────────────────────────────────────────────────
 *  Class           Layer           Package                    Module
 *  ──────────────────────────────────────────────────────────────────────
 *  NewsDto         Data/Remote     data.remote.dto            :data
 *  NewsMetaDto     Data/Remote     data.remote.dto            :data
 *  NewsEntity      Data/Local      data.local.entity          :data
 *  News            Domain          domain.model               :domain
 *  NewsUiModel     Presentation    feature.news.model         :feature:news
 *  NewsUiState     Presentation    feature.news.model         :feature:news
 *  NewsUiEvent     Presentation    feature.news.model         :feature:news
 *  NewsDataMapper  Data            data.mapper                :data
 *  NewsUiMapper    Presentation    feature.news.mapper        :feature:news
 *
 *  MAPPER DIRECTION TABLE
 *  ─────────────────────────────────────────────────────────────────────
 *  Mapper           Method              Input         Output
 *  ──────────────────────────────────────────────────────────────────────
 *  NewsDataMapper   toDomain(dto)       NewsDto    →  News
 *  NewsDataMapper   toEntity(domain)    News       →  NewsEntity
 *  NewsDataMapper   toDomain(entity)    NewsEntity →  News
 *  NewsUiMapper     toUiModel(domain)   News       →  NewsUiModel
 *
 *  THE ONE RULE — WHAT BELONGS WHERE
 *  ─────────────────────────────────────────────────────────────────────
 *  @SerializedName   → only in DTO classes              (data/remote/dto)
 *  @Entity           → only in Entity classes           (data/local/entity)
 *  cachedAt          → only in NewsEntity               (database concern)
 *  formattedDate     → only in NewsUiModel              (display concern)
 *  isSelected        → only in NewsUiModel              (UI-only flag)
 *
 *  @SerializedName inside News (domain)     → WRONG
 *  @Entity inside News (domain)             → WRONG
 *  "2 hours ago" formatting inside News     → WRONG
 *  cachedAt inside NewsUiModel              → WRONG
 *  isSelected inside News (domain)          → WRONG
 * ════════════════════════════════════════════════════════════════════════
 * ## What About UiModel?
 *
 * UiModel also has ZERO nullable fields
 * because it receives data from Domain
 * which already has ZERO nullable fields
 *
 * The null handling chain:
 *
 * DTO (nullable allowed)
 *     ↓
 * Mapper (resolves ALL nulls)
 *     ↓
 * Domain (zero nulls)
 *     ↓
 * UiModel (zero nulls)
 *     ↓
 * UI (just renders — no null checks ever)
 *
 */
fun quickRefTable() = Unit