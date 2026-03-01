package collections

import org.example.basic.str

/*
*  Kotlin offers a range of transformation operators that enable you to manipulate collections efficiently.
*  These operators create new collections based on the original ones, applying
    various transformation logic. Here are the main types of transformation operators supported
    by Kotlin:
* */

fun transformationOperators() {
    println("---------Map----------")
    // map - transform a list's elements
    val list = listOf<String>("THIS", "is", "A", "crow")
    // Using map
    val mapList = list.map { s ->
        s.lowercase()
            .replaceFirstChar { c ->
                if (c.isLowerCase()) c.titlecase() else c.toString()
            }
    }.joinToString(" ")

    println(mapList)

    // Using Join to String
    val newList = list.joinToString(" ") { s ->
        s.lowercase()
            .replaceFirstChar { c ->
                if (c.isLowerCase()) c.titlecase() else c.toString()
            }
    }
    println(newList)

    println("---------FlatMap----------")
    // flatmap - The flatMap operator transforms each element into another collection and then flattens the
    // result into a single list. The flatten operator works directly on nested collections.
    val dummyList = listOf(listOf("A", "B", "C"), listOf("D", "E", "F"))
    val mappedList = dummyList.map { it -> it.map { s -> s.lowercase() } } // [[a, b, c], [d, e, f]]
    val flatMappedList = dummyList.flatMap { it -> it.map { s -> s.lowercase() } } // [a, b, c, d, e, f]
    println(flatMappedList)

    println("---------GroupBy----------")
    // Group by - Use these when you need to reshape a list into a map. groupBy preserves all elements per
    // key (lists of values)
    val dummyListTwo = listOf("Kotlin", "Java", "Android", "Rust")
    val resultOfGroupBy = dummyListTwo.groupBy { s -> if (s.length > 4) "Length > 4" else "Length < 4" }
    println(resultOfGroupBy)

    println("---------AssociateBy----------")
    // associateBy - keeps only one element per key (last one wins on key collisions).
    // associateBy creates a map where each key is derived from an element’s property, and the value is the element itself.
    val associateBy = dummyListTwo.associateBy { s -> if (s.length > 4) "Length > 4" else "Length < 4" }
    println(associateBy)

    println("---------Zip----------")
    // associateBy - keeps only one element per key (last one wins on key collisions).
    // associateBy creates a map where each key is derived from an element’s property, and the value is the element itself.
    val dummyListThree = listOf("2.3", "24", "36", "16")
    val zipOperation = dummyListTwo.zip(dummyListThree) { name, version ->
        "$name has version : $version"

    }
    println(zipOperation)

    println("--------Unzip---------")
    // unzip() splits a List<Pair<A, B>> into two separate lists.
    val pairs = listOf(
        "Majid" to 25,
        "Ali" to 22,
        "Sara" to 24
    )

    val (names, ages) = pairs.unzip()

    println(names)
    println(ages)

    println("--------Filter---------")
    // filter values greater than 30
    // Keep only elements those match the given condition
    val filterRes = dummyListThree.filter { it.toFloat() > 30f }
    println(filterRes) // [36]

    println("--------Filter Not---------")
    // Exclude the elements those match with given condition
    val filterNotRes = dummyListThree.filterNot { it.toFloat() > 30f }
    println(filterNotRes) // [2.3, 24, 16]  excluded 36

    println("--------FilterIndexed---------")
    // Exclude the elements those match with given condition
    val filterIndexedRes =
        dummyListThree.filterIndexed { index, string -> if (index == 2) false else string.toFloat() < 30f }
    println(filterIndexedRes) // [2.3, 24, 16] excluded 36 because it is on the 2nd index

    // Iterator
    /*
    * Iterators4 let you step through a collection one element at a time without exposing its
    internal structure. In Kotlin, they’re the backbone of the collections API, enabling efficient,
    sequential processing. Under the hood, Iterator is a tiny interface with just two operator
    functions: hasNext() and next() that drive the traversal.
    * */
    println("--------Iterator---------")
    val namesOf = listOf("Alan", "Ele", "Pr")
    val iter = namesOf.iterator()
    while (iter.hasNext()) {
        print("${iter.next()} ")
    }
    println()

    println("--------MutableIterator---------")
    val numbers = mutableListOf(1, 2, 3, 4, 5, 6, 7, 8)
    val iterator = numbers.iterator() // .iterator() on a MutableList returns a MutableIterator

    while (iterator.hasNext()) {
        val number = iterator.next() // Get the next element
        if (number % 2 == 0) {
            iterator.remove() // Remove the element that was just returned
        }
    }

    println(numbers) // Prints: [1, 3, 5, 7]

}


fun main() {
    transformationOperators()
}
