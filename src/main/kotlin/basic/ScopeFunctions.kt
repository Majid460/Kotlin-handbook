package basic

/**
 * ```
 * Scope Functions Cheat Sheet
 *
 * These functions execute a block of code in the context of an object.
 *
 * ─────────────────────────────────────────────────────────────
 * let
 * ─────────────────────────────────────────────────────────────
 * Executes a block with the object as `it` and returns the result.
 *
 * Use when:
 * - You need to transform a value
 * - You want null-safe calls (`?.let {}`)
 *
 * Example:
 * val length = name?.let { it.length }
 *
 * Signature:
 * inline fun <T, R> T.let(block: (T) -> R): R
 *
 *
 * ─────────────────────────────────────────────────────────────
 * run
 * ─────────────────────────────────────────────────────────────
 * Executes a block with the object as `this` and returns the result.
 *
 * Use when:
 * - You want to compute a result using object properties
 * - Cleaner access via `this`
 *
 * Example:
 * val length = name.run { length }
 *
 * Signature:
 * inline fun <T, R> T.run(block: T.() -> R): R
 *
 *
 * ─────────────────────────────────────────────────────────────
 * apply
 * ─────────────────────────────────────────────────────────────
 * Executes a block with the object as `this` and returns the SAME object.
 *
 * Use when:
 * - Object initialization / configuration
 * - Builder pattern
 *
 * Example:
 * val user = User().apply {
 *     name = "Majid"
 *     age = 25
 * }
 *
 * Signature:
 * inline fun <T> T.apply(block: T.() -> Unit): T
 *
 *
 * ─────────────────────────────────────────────────────────────
 * also
 * ─────────────────────────────────────────────────────────────
 * Executes a block with the object as `it` and returns the SAME object.
 *
 * Use when:
 * - Logging / debugging / side effects
 *
 * Example:
 * user.also {
 *     println("User created: $it")
 * }
 *
 * Signature:
 * inline fun <T> T.also(block: (T) -> Unit): T
 *
 *
 * ─────────────────────────────────────────────────────────────
 * Summary
 * ─────────────────────────────────────────────────────────────
 *
 * Function | Context | Returns     | Use Case
 * ---------|--------|-------------|------------------------
 * let      | it     | result      | Transform / null-safe
 * run      | this   | result      | Compute
 * apply    | this   | object      | Configure object
 * also     | it     | object      | Side effects
 *
 *
 * Rule of thumb:
 * - Need result? → let / run
 * - Need same object? → apply / also
 * - Prefer `this`? → run / apply
 * - Prefer `it`? → let / also
 * ```
 */
class ScopeFunctions {
}