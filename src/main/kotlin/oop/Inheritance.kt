package org.example.oop

// Inheritance

// All Classes.kt in Kotlin have a common superclass, Any, which is the default superclass for a class with no supertypes declared:


class Example // Implicitly inherits from Any

// Example 1
// Class is open for inheritance
open class Base(p: Int) {
    init {
        println("P is = $p")
    }

    open fun hello() = println("Hello from the base class")
}
// To declare an explicit supertype, place the type after a colon in the class header:

// In Kotlin, when you declare a constructor parameter like p: Int,
// it is not a property, it’s just a local parameter to the constructor —
// so it’s only available inside the constructor and init blocks, not inside other functions.
class Derived(p: Int) : Base(p) {
    init {
        println("P is = $p")
    }

    override fun hello() {
        super.hello()
        println("Hello from the derived class")
    }
}

// Example 2
open class Person(val name: String) {
    // Open function that can be overridden in a subclass
    open fun introduce() {
        println("Hello, my name is $name.")
    }
}

// Subclass inheriting from Person and overriding the introduce() function
open class Student(
    name: String,
    open val school: String
): Person(name){
    final override fun introduce() {
        println("Hi, I'm $name, and I study at $school.")
        super.introduce()
    }
}
class CR(name: String, override val school: String): Student(name,school){
//    override fun introduce() {  This will give a error and will say make the introduce a open function
//        super.introduce()
//    }
}
// 3. Property overriding

interface Shape {
    val vertexCount: Int
}

class RectangleOne(override val vertexCount: Int = 4) : Shape // Always has 4 vertices
// Custom getters and setters
class Polygon : Shape {
    override val vertexCount: Int    // Can be set to any number later
        get() = 4

}

// A custom getter runs every time the property is accessed:
class Rectangle(val width: Int, val height: Int) {
    // You can omit the type if the compiler can infer it from the getter:
    val area: Int
        get() = this.width * this.height
}

// Set
// A custom setter runs every time you assign a value to the property, except during initialization. By convention, the name of the setter parameter is value, but you can choose a different name:
class Point(var x: Int, var y: Int) {
    var coordinates: String
        get() = "$x,$y"
        set(value) {
            val parts = value.split(",")
            x = parts[0].toInt()
            y = parts[1].toInt()
        }
}

// Changing visibility or adding annotations
class Bank(initialBalance:Int){
    var balance: Int = initialBalance
        // Only the class can modify the balance
       private set

    fun deposit(amount: Int) {
        if (amount > 0) balance += amount
    }

    fun withdraw(amount: Int) {
        if (amount > 0 && amount <= balance) balance -= amount
    }
}

// Fields and Backing fields
class Temperature {
    // Backing property storing temperature in Celsius
    private var _celsius: Double = 0.0

    var celsius: Double
        get() = _celsius
        set(value) { _celsius = value }

    var fahrenheit: Double
        get() = _celsius * 9 / 5 + 32
        set(value) { _celsius = (value - 32) * 5 / 9 }
}





fun main() {
    val d = Derived(10)
    d.hello()

    val st = Student("Alex","ABC")
    st.introduce()

    // Setter and getter
    val location = Point(1, 2)
    println(location.coordinates)
    // 1,2

    location.coordinates = "10,20"
    println("${location.x}, ${location.y}")
    // 10, 20

    val account = Bank(100)
    println("Initial balance: ${account.balance}")
    // 100

    account.deposit(50)
    println("After deposit: ${account.balance}")
    // 150

    account.withdraw(70)
    println("After withdrawal: ${account.balance}")
    // 80

    // account.balance = 1000
    // Error: cannot assign because setter is private

    val temp = TemperatureControl()
    temp.celsius = 25.0
    println("${temp.celsius}°C = ${temp.fahrenheit}°F")
    // 25.0°C = 77.0°F

    temp.fahrenheit = 212.0
    println("${temp.celsius}°C = ${temp.fahrenheit}°F")
    // 100.0°C = 212.0°F
}
