package workmanager


/**
 * ```
 * // You schedule work to upload a photo at night
 * // User turns off WiFi → work fails silently
 * // Device restarts    → work is gone forever
 * // App is killed      → work stops immediately
 * // Battery saver on   → work is throttled or killed
 * // No retry logic     → you write it yourself every time
 * ```
 * ```
 *
 * You ended up writing hundreds of lines of boilerplate just to do one reliable background task.
 *```
 * ---
 *
 * ## 2. What WorkManager Is in One Line
 *
 * ```
 * > WorkManager is a guaranteed task scheduler. If you schedule work, it WILL run — even if the app is killed, even if the device restarts, even if it has to wait for WiFi or charging.
 *
 * The key word is **guaranteed**. No other API before it could promise that.
 *
 * ---
 * ```
 * ## 3. How WorkManager Solves Every Problem
 * ```
 *
 * Problem                          WorkManager Solution
 * ──────────────────────────────────────────────────────────────────
 * App killed mid-task              Work is persisted to a database — survives app death
 * Device restarts                  Work reschedules itself automatically on reboot
 * No WiFi                          Constraint system — waits until WiFi is available
 * Battery saver                    Respects battery optimisation, runs when appropriate
 * Retry on failure                 Built-in retry policies (linear, exponential backoff)
 * Chaining tasks                   Chain A → B → C, parallel fan-out, sequential fan-in
 * Progress reporting               setProgress() updates UI while work runs in background
 * One-time or periodic work        OneTimeWorkRequest and PeriodicWorkRequest built in
 * API level compatibility          Works from API 14+ — no version checks needed
 * Observability                    LiveData/Flow — observe work state from UI
 * ```
 *
 * ---
 *
 * ## 4. WorkManager vs Service — The Core Difference
 *
 * This is the most misunderstood thing about WorkManager.
 * ```
 * Service                                  WorkManager
 * ────────────────────────────────────────────────────────────────────────
 * Runs immediately when started            Runs when constraints are met
 * No persistence — killed = gone           Persisted in DB — survives everything
 * You manage the thread yourself           Runs on background thread automatically
 * No built-in retry                        Built-in retry with backoff policy
 * No constraint system                     Constraints: WiFi, charging, battery, storage
 * Not guaranteed on app death              Guaranteed — that is the whole point
 * No chaining                              Chain tasks like a pipeline
 * No progress reporting built in           setProgress() + observeWorkInfo()
 * You manage lifecycle                     WorkManager manages lifecycle
 * ```
 *
 * ---
 *
 * ## 5. Do You Still Need a Service?
 *
 * Yes — but for completely different use cases. They are not competitors.
 * ```
 * Use WorkManager when:
 * ✅ Upload a photo to server
 * ✅ Sync data with backend periodically
 * ✅ Process and compress a video
 * ✅ Send analytics batch to server
 * ✅ Download a file in background
 * ✅ Any task that must complete eventually even if app dies
 *
 * Use Service (specifically ForegroundService) when:
 * ✅ Playing music — user must see the notification while it plays
 * ✅ Live location tracking — ongoing, user-visible
 * ✅ Active call/VOIP — must stay alive while call is active
 * ✅ Real-time file download with visible progress notification
 * ✅ Anything where user SEES it happening right now via notification
 * ```
 *
 * The rule:
 * ```
 * WorkManager  → "Do this eventually, guarantee it completes"
 * ForegroundService → "Do this right now, user is actively aware of it"
 * ```
 *
 * WorkManager can even START a ForegroundService internally for long-running tasks. They work together.
 *
 * ---
 *
 * ## 6. Core Building Blocks
 * ```
 * WorkManager has 4 things you need to know:
 *
 * 1. Worker          → Where you write your actual work code
 * 2. WorkRequest     → What work to run + constraints + retry policy
 * 3. WorkManager     → The scheduler — you hand it the WorkRequest
 * 4. WorkInfo        → Observable state — ENQUEUED, RUNNING, SUCCEEDED, FAILED, CANCELLED*/
class WorkManager {
}