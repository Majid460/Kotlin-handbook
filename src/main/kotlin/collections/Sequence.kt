package org.example.collections

/*
* Sequence:
* 1. Lazy data processing
* 2. Call all operations on single element
* 3. Execute when a terminal operator used such as (toList(),toSet(),first(),count(),sum(),find()).
* 4. Underlying implementation just has Iterator<T>. (hasNext():Boolean, next():T)
* 5. Memory efficient: Does not store an intermediate list() such as when run map())
*
* Use it when:
    Large collections
    Many chained operations
    Early termination (first, any, find)
    Performance-sensitive logic
*
* */
// Examples
fun createSequence() {
    val seq = sequenceOf(1, 2, 3, 4, 5, 6, 7, 9)
    val res = seq.mapIndexed { index, x -> if (index % 2 == 0) x * 2 else x * 3 }
        .groupBy { i -> if (i % 2 == 0) "even" else "odd" }

    println(res)
    // O(n)
    // Lets simplify this
    val (evenList, odd) = seq.partition { i -> i % 2 == 0 }
    println(evenList)
    println(odd)

    // Create using the list
    val list = listOf(1, 2, 3, 4)
    val sequence = list.asSequence()
    println(sequence.toList())

    // Using generate function
    val seqGen = generateSequence(1) { it * 2 }
    println(seqGen.take(5).toList())
}
fun sequenceExecution() {
    println("-----Execution flow------")
    val result = (1..10).asSequence()
        .filter {
            println("Filtering $it")
            it % 2 == 0

        }
        .map {
            println("Mapping $it")
            it * 2
        }
        .take(2) // Another intermediate operation
        .toList() // This is the terminal operation that starts everything.
    println("Result: $result")
}
/*
*  Execution Flow: The toList() call is what triggers the work.
    1. toList() asks the take(2) sequence for its iterator.
    2. The take(2) iterator asks the map iterator for an element.
    3. The map iterator asks the filter iterator for an element.
    4. The filter iterator starts pulling from the original source (1..10).
    • It gets 1. “Filtering 1”. It fails the predicate.
    • It gets 2. “Filtering 2”. It passes. The filter iterator yields 2.
    5. The map iterator receives 2. “Mapping 2”. It applies the transform (2 * 2 = 4) and
    yields 4.
    6. The take(2) iterator receives 4. It’s the first element, so it adds it to its internal list.
    7. The take(2) iterator asks the map iterator for a second element.
    8. The process repeats:
    • filter gets 3. “Filtering 3”. Fails.

    • filter gets 4. “Filtering 4”. Passes. Yields 4.
    9. map receives 4. “Mapping 4”. Transforms it to 8. Yields 8.
    10. take(2) receives 8. It’s the second element. It adds it to its list and now knows its job
    is done. It signals that it hasNext() == false.
    11. The toList() operation completes, having only processed numbers 1 through 4 from
    the original source.
*

* */


fun main() {
    createSequence()
    sequenceExecution()
}
