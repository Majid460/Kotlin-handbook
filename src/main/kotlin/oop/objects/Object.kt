package org.example.oop.objects

// object creates a singleton — exactly one instance exists in the JVM.

object Logger {
    var level = "DEBUG"

    fun log(msg: String) {
        println("[$level] $msg")
    }
}

fun main() {
    Logger.log("App started")
    Logger.level = "INFO"
}
