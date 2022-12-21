class Environment(val enclosing: Environment? = null) {

    val values: HashMap<String, Any?> = HashMap()

    fun define(name: String, value: Any?) {
        values[name] = value
    }

    fun get(name: Token) : Any? {
        if (values.contains(name.lexeme)) {
            return values[name.lexeme]
        }

        enclosing?.let { outerScope ->
            val outerName = outerScope.get(name)
            return outerName//outerScope.get(name)
        }

        throw RuntimeError(name, "Undefined variable '${name.lexeme}'.")
    }

    fun getAt(distance: Int, name: String) : Any? {
        return ancestor(distance).values[name]
    }

    fun ancestor(distance: Int) : Environment {
        var environment :Environment = this
        for (i in 0 until distance) {
            if (environment.enclosing != null) {
                environment = environment.enclosing!!
            } else {
                KLox.error(-1, "Resolver tried to find variable at impossible depth")
            }
        }
        return environment
    }

    fun assign(name: Token, value: Any?) {
        if (values.contains(name.lexeme)) {
            values[name.lexeme] = value
            return
        }

        enclosing?.let { outerScope ->
            outerScope.assign(name, value)
            return
        }

        throw RuntimeError(name, "Attempted to assign value to Undefined variable '${name.lexeme}'.")
    }

    fun assignAt(distance: Int, name: Token, value: Any?) {
        ancestor(distance).values[name.lexeme] = value
    }

}