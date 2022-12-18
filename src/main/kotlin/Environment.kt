class Environment(val enclosing: Environment? = null) {
    val values = HashMap<String, Any?>()

    fun define(name: String, value: Any?) {
        values[name] = value
    }

    fun get(name: Token) : Any? {
        if (values.contains(name.lexeme)) return values[name.lexeme]

        enclosing?.let { outerScope ->
            return outerScope.get(name)
        }

        throw RuntimeError(name, "Undefined variable '${name.lexeme}'.")
    }

    fun assign(name: Token, value: Any?) {
        if (values.contains(name.lexeme)) {
            values[name.lexeme] = value
            return
        }

        enclosing?.let { outerScope ->
            outerScope.assign(name, value)
        }

        throw RuntimeError(name, "Undefined variable '${name.lexeme}'.")
    }
}