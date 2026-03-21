package kotlinxlibraries

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.Serializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import org.example.oop.A

/**
 * `kotlinx-serialization` is Kotlin’s official serialization library, providing a multiplatform
 * solution for converting Kotlin objects to and from various formats such as `JSON`, `Protocol`
 * `Buffers`, `CBOR`, and others. Unlike reflection-based serialization libraries like `Gson` or
 * `Jackson`, `kotlinx-serialization` uses compile-time code generation through a Kotlin compiler
 * plugin, resulting in better performance and full multiplatform support.
 * */
@Serializable
data class User(
    val name: String,
    val age: Int,
    val phone: Long
)
/**
 * The library consists of two main components: the compiler plugin and the runtime library.
 * The compiler plugin analyzes classes marked with `@Serializable` and generates serializer
 * classes at compile time. These generated serializers know exactly how to convert each
 * property to and from the target format without using reflection.
 * */
@Serializable
data class ApiResponse<T>(
    /**The `@SerialName` annotation changes the name used in the serialized format, useful when
    working with APIs that use different naming conventions.
     */
    @SerialName("data")
    val data: T,
    /**
     * The `@Required` annotation makes
     * a property mandatory during deserialization, throwing an exception if it’s missing.*/
    @Required
    val status:String,
    /**The `@Transient` annotation excludes a property from serialization entirely*/
    @Transient
    val localCache:String = "",
    /** `@EncodeDefault` ensures default values are included in the output even when they match the default.*/
    @EncodeDefault
    val version:Int = 1

)
inline fun <reified T> toJson(
    data: T,
    json: Json = Json.Default       // ← caller can override, default works out of box
): String {
    return json.encodeToString(serializer<T>(), data)
}

inline fun <reified T> fromJson(
    string: String,
    json: Json = Json.Default       // ← same here
): T {
    return json.decodeFromString<T>(string)
}

fun main(){
    val user = User("Test 1",20,98828282)
    // convert the data to json string
    val userJsonString = Json.encodeToString(User.serializer(),user)
    println("User:: $userJsonString")
    // Using extension function
    val jStr = toJson<User>(user)
    println(jStr)


    val deserialize = Json.decodeFromString<User>(userJsonString)
    println("User Deserialized: $deserialize")

    // Using extension function
    val jData = fromJson<User>(jStr)
    println("data from extension:: $jData")


    /*
    * User:: {"name":"Test 1","age":20,"phone":98828282}
    * User Deserialized: User(name=Test 1, age=20, phone=98828282)
    * */
    println("------ Using Extension --------")
    // 3. Pre-build a config and reuse it (recommended)
    val appJson = Json {
        prettyPrint = true           // Format output with indentation
        isLenient = true             // Accept non-standard JSON
        ignoreUnknownKeys = true     // Skip unknown properties during deserialization
        coerceInputValues = true     // Use default values for null inputs
        encodeDefaults = false      // Omit properties with default values
        explicitNulls = false       // Omit null values from output
    }

    val apiResponse = ApiResponse(user,"200" )
    val fromDataToJson = toJson(apiResponse,appJson)
    println(fromDataToJson)

    val toData = fromJson<ApiResponse<User>>(fromDataToJson,appJson)
    println(toData)


}
