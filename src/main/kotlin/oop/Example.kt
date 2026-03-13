package org.example.oop

class TemperatureControl {
    private var _celsius: Double = 0.0

    var celsius: Double
        get() = _celsius
        set(value) {
            _celsius = value
        }
    var fahrenheit: Double
        get() = _celsius * 9 / 5 + 32
        set(value) {
            _celsius = (value - 32) * 5 / 9
        }
}


//fun main() {
//    val temp = TemperatureControl()
//    temp.celsius = 25.0
//    println("${temp.celsius}°C = ${temp.fahrenheit}°F")
//
//    temp.fahrenheit = 212.0
//    println("${temp.celsius}°C = ${temp.fahrenheit}°F")
//}

// Bank manager with Encapsulation
class Accounts(initialBalance: Double = 0.0) {
    private var balance = initialBalance  // Hidden
    private val transactionHistory = mutableListOf<String>() // Hidden
    private var pin: Int = 0

    fun enterPin(enteredPin: Int) {
        pin = enteredPin
        validatePin(enteredPin)
    }

    private fun validatePin(enteredPin: Int) {
        check(enteredPin == pin) { "Invalid pin" }
        require(enteredPin.toString().length == 4) { "Invalid pin length" }
    }

    fun deposit(amount: Double) {
        check(pin == this.pin) { "Invalid pin" }
        require(balance > 0) { "Amount must be positive" }
        balance += amount
        transactionHistory.add("Deposit: $amount")
    }

    fun withDraw(amount: Double) {
        check(pin == this.pin) { "Invalid pin" }
        check(balance >= amount) { "Insufficient funds" }
        balance -= amount
        transactionHistory.add("Withdraw: $amount")
    }

    fun getBalance(): Double = balance

    fun getHistory(): List<String> = transactionHistory.toList()

}

fun main() {
    val account = Accounts(1000.0)
    print("Enter the pin: ")
    val pin = readlnOrNull()?.toIntOrNull()
    if (pin != null) {
        account.enterPin(pin)
    }
    println()

    // Menu
    while (true) {
        println("1. Check Balance")
        println("2. Withdraw")
        println("3. Deposit")
        println("4. Transaction History")
        println("5. Exit")
        print("Enter your choice: ")
        val choice = readlnOrNull()?.toIntOrNull()
        when (choice) {
            1 -> {
                println("Balance: ${account.getBalance()}")
            }
            2 -> {
                print("Enter the amount to withdraw: ")
                val amount = readlnOrNull()?.toDoubleOrNull()
                if (amount != null) {
                    account.withDraw(amount)
                }
            }
            3 -> {
                print("Enter the amount to deposit: ")
                val amount = readlnOrNull()?.toDoubleOrNull()
                if (amount != null) {
                    account.deposit(amount)
                }
            }
            4 -> {
                println("Transaction History:")
                account.getHistory().forEach { println(it) }
            }
            5 -> {
                println("Exiting...")
                return
            }
            else -> {
                println("Invalid choice. Please try again.")
            }
        }
    }

}