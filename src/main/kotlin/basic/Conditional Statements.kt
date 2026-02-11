package org.example.basic


// When conditional statement


fun main() {
    print("Enter a input: ")
    val x = readln()
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
           val output = when(x){
                is String -> x.startsWith("ID-")
            }
            println("Output :: $output")
        }
    }
}
