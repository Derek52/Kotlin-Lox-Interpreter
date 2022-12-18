class Environment {
    val values = HashMap<String, Any?>()

    fun define(name: String, value: Any?) {
        values[name] = value
    }

    fun get(name: Token) : Any? {
        if (values.contains(name.lexeme)) return values[name.lexeme]

        throw RuntimeError(name, "Undefined variable '${name.lexeme}'.")
    }
}