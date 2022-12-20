class LoxFunction(val declaration: FunctionStmt) : LoxCallable {
    override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
        val environment = Environment(interpreter.globals)
        for (i in declaration.params.indices) {
            environment.define(declaration.params[i].lexeme, arguments[i])
        }

        interpreter.executeBlock(declaration.body, environment)
        return null
    }

    override fun arity(): Int {
        return declaration.params.size
    }

    override fun toString() : String {
        return "<fn ${declaration.name.lexeme}>"
    }

}