package org.example.oop.objects


class User private constructor(private val name: String) {

    fun greet() = "Hello, my name is $name"

    // You can explicitly name a companion object,

    companion object Factory : Creator{
        val user:User = User("")
        private val createdUsers = mutableListOf<User>()

        // Acts as a Singleton: accessible without creating a User instance
         fun create(name: String): User {
            val user = User(name)
             createdUsers.add(user)
             return user
         }

        // Access to private members: can log internal state
         fun listAllUsers(): List<String> = createdUsers.map { it.name }

         override fun printFactoryInfo() {
             println("User factory created ${createdUsers.size} user(s).")
         }

    }
    fun display(){
        user.name

    }
}

interface Creator {
    fun printFactoryInfo()
}

fun main() {
    val user1 = User.create("Test1")
    val user2 = User.create("Test2")

    user1.greet()
    println(User.listAllUsers())
    User.printFactoryInfo()
}
