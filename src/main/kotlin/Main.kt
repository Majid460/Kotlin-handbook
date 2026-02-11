package org.example

import com.sun.activation.registries.LogSupport.log
import coroutines.createAChannel
import coroutines.passMessages
import coroutines.supervisorScopeFetchAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.example.coroutines.UserImpl
import org.example.coroutines.flowTransformation
import org.example.coroutines.simple
import kotlin.math.log


suspend fun CoroutineScope.callAllCoroutines() {
//        val job1 = async {coroutines()}
    val job2 = async {
        val output = supervisorScopeFetchAll()
        output.forEach { result ->
            result.onSuccess { value -> println("Success → $value") }.onFailure {
                println("Failed → ${it.message}")
            }
        }
    }
//        val job3 = async { scope.launchAsBuilder() }
//        val job4 = async { scope.asyncAsBuilder() }
//    val job5 = async { createAChannel() }
//    val job6 = async { passMessages() }
//    job6.await()
    awaitAll(job2)
}

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
suspend fun main() {
    log("Starting a Coroutine")
    val ar = listOf<Int>(1, 25)
    UserImpl()
    coroutineScope {
        callAllCoroutines()
//        // Flow practice problem
//        print("Flow value :")
//        simple().collect { it->
//            print("$it ")
//        }
//        println("Flow transformation")
//        flowTransformation()
    }

}
