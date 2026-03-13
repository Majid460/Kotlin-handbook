package basic

import kotlinx.coroutines.Delay
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.util.Timer
import kotlin.math.pow
import kotlin.properties.Delegates
import kotlin.properties.ObservableProperty


// If else
fun ifElse() {
    val  a = 2
    var b = a * a
    // use pow()
    var power = a.toDouble().pow(2)
    val x = if (a > 2) "Greater" else "Smaller or Equal"
    println("b: $b, x: $x, power: $power")
}

/** ## When conditional statement
 *
 * `is` used in when statement to check the type of the variable.
 *
 * `in` is used in when statement to check the range of the variable.
 *
 * `no` is or in is used when statement to check the value of the variable
 * */
fun whenStatement(){
    print("Enter a input: ")
    val x = readln()
    //when you don't provide an argument in parentheses, each branch is a boolean condition. The first branch that evaluates to true is executed.
    val type = when {
        x.toIntOrNull() != null -> "int"
        x.toDoubleOrNull() != null -> "double"
        else -> "string"
    }

    when(type){
        "int" -> {
            when (x.toInt()){
                in 0..100 -> println("The number is in range of 0 to 100")
                !in 101..299 -> println("The number is not in range of 101 to 299")
                else -> println("None of the above")
            }
        }
        "double"-> println("Processing the double")
        else -> {
            val output = x.startsWith("ID-")
            println("Output :: $output")
        }
    }
}
fun whenEx(){
    var x : Int by Delegates.observable(1,{ prop, old, new ->
        runWhen(new)
    })
    runWhen(x)   // trigger initial value
    runBlocking {
        delay(3000)
        x = 2
    }

}
fun runWhen(i:Int){
    when(i){
        1 -> println("It is 1")
        2 -> println("It is 2")
    }
}

fun main() {
//    whenStatement()
//    ifElse()
    whenEx()
    val x = listOf(1,2,3)

    x.map {
        println(it)
    }
}
