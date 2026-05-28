package com.example.data.auth

class JsonObjectBuilder {
    private val map = mutableMapOf<String, Any>()

    fun put(key: String, value: Any) {
        map[key] = value
    }

    fun build(): Map<String, Any> = map
}

inline fun buildJsonObject(builderAction: JsonObjectBuilder.() -> Unit): Map<String, Any> {
    val builder = JsonObjectBuilder()
    builder.builderAction()
    return builder.build()
}
