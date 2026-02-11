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


fun main() {
    val temp = TemperatureControl()
    temp.celsius = 25.0
    println("${temp.celsius}°C = ${temp.fahrenheit}°F")

    temp.fahrenheit = 212.0
    println("${temp.celsius}°C = ${temp.fahrenheit}°F")
}
