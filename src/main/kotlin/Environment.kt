class Environment(val values: HashMap<String, Any?> = HashMap()) {

    fun define(name: String, value: Any?) {
        values[name] = value
    }

    fun get(name: Token) : Any? {
        if (values.contains(name.lexeme)) {
            return values[name.lexeme]
        }
        throw RuntimeError(name, "Undefined variable '${name.lexeme}'.")
    }

    fun assign(name: Token, value: Any?) {
        if (values.contains(name.lexeme)) {
            values[name.lexeme] = value
            return
        }

        throw RuntimeError(name, "Attemtped to assign value to Undefined variable '${name.lexeme}'.")
    }

}