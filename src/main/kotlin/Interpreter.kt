import TokenType.*

class Interpreter : Expr.Visitor<Any?>, Stmt.Visitor<Void?> {

    val globals = Environment()
    private var environment = globals
    val locals = HashMap<Expr, Int>()

    init {
        globals.define("clock", object : LoxCallable {
            override fun arity(): Int {
                return 0
            }

            override fun call(interpreter: Interpreter, arguments: List<Any?>): Any {
                return System.currentTimeMillis().toDouble() / 1000.0
            }

            override fun toString(): String {
                return "<native fn>"
            }
        })
    }

    fun interpret(statements: List<Stmt>) {
        try {
            for (stmt in statements) {
                execute(stmt)
            }
        } catch (error: RuntimeError) {
            KLox.runtimeError(error)
        }
    }

    override fun visitLiteralExpr(expr: Literal): Any? {
        return expr.value
    }

    override fun visitVariableExpr(expr: Variable): Any? {
        return lookUpVariable(expr.name, expr)
    }

    fun lookUpVariable(name: Token, expr: Expr) : Any? {
        val distance :Int? = locals[expr]
        if (distance != null) {
            return environment.getAt(distance, name.lexeme)
        } else {
            return globals.get(name)
        }
    }


    override fun visitAssignExpr(expr: Assign): Any? {
        val value = evaluate(expr.value)

        val distance = locals[expr]
        if (distance != null) {
            environment.assignAt(distance, expr.name, value)
        } else {
            globals.assign(expr.name, value)
        }


        return value
    }

    override fun visitUnaryExpr(expr: Unary): Any? {
        val right = evaluate(expr.right)

        if (expr.operator.type == MINUS) {
            checkNumberOperand(expr.operator, right)
            return right as Double * -1
        } else if (expr.operator.type == BANG) {
            return isNotTruthy(right)
        }

        return null
    }

    override fun visitBinaryExpr(expr: Binary): Any? {
        val left = evaluate(expr.left)
        val right = evaluate(expr.right)

        when(expr.operator.type) {
            MINUS -> {
                checkNumberOperands(expr.operator, left, right)
                return left as Double - right as Double
            }
            SLASH -> {
                checkNumberOperands(expr.operator, left, right)
                return left as Double / right as Double
            }
            STAR -> {
                checkNumberOperands(expr.operator, left, right)
                return left as Double * right as Double
            }
            PLUS -> {
                if (left is Double && right is Double) {
                    return left as Double + right as Double
                }
                if (left is String && right is String) {
                    return (left + right) as String
                }

                throw RuntimeError(expr.operator, "Both operands must be numbers or strings")
            }

            GREATER -> {
                checkNumberOperands(expr.operator, left, right)
                return (left as Double) > (right as Double)
            }
            GREATER_EQUAL -> {
                checkNumberOperands(expr.operator, left, right)
                return (left as Double) >= (right as Double)
            }
            LESS -> {
                checkNumberOperands(expr.operator, left, right)
                return (left as Double) < (right as Double)
            }
            LESS_EQUAL -> {
                checkNumberOperands(expr.operator, left, right)
                return (left as Double) <= (right as Double)
            }

            EQUAL_EQUAL -> {
                return isEqual(left, right)
            }
            BANG_EQUAL -> {
                return !isEqual(left, right)
            }

            else -> {
                return null
            }
        }
    }

    override fun visitCallExpr(expr: Call): Any? {
        val callee = evaluate(expr.callee)

        val arguments = ArrayList<Any?>()
        for (arg in expr.arguments) {
            arguments.add(evaluate(arg))
        }

        if (callee !is LoxCallable) {
            throw RuntimeError(expr.paren, "Can only call functions and classes.")
        }

        val function = callee as LoxCallable
        if (arguments.size != function.arity()) {
            throw RuntimeError(expr.paren, "Expected ${function.arity()} arguments but got ${arguments.size}.")
        }
        return function.call(this, arguments)
    }

    override fun visitGetExpr(expr: GetExpr): Any? {
        val loxObject = evaluate(expr.loxObject)
        if (loxObject is LoxInstance) {
            return loxObject.get(expr.name)
        }
        throw RuntimeError(expr.name, "Only instances have properties.")
    }

    override fun visitLogicalExpr(expr: Logical): Any? {
        val left = evaluate(expr.left)

        if (expr.operator.type == OR) {
            if (isTruthy(left)) return left
        } else { //expr.operator.type == AND
            if (isNotTruthy(left)) return left
        }

        return evaluate(expr.right)
    }

    override fun visitSetExpr(expr: SetExpr): Any? {
        val loxObject = evaluate(expr.loxObject)

        if (loxObject !is LoxInstance) {
            throw RuntimeError(expr.name, "Only instances have fields.")
        }

        val value = evaluate(expr.value)
        loxObject.set(expr.name, value)
        return value
    }

    override fun visitSuperExpr(expr: Super): Any? {
        val distance = locals[expr]
        val superClass = environment.getAt(distance!!, "super") as LoxClass
        val loxObject = environment.getAt(distance - 1, "this") as LoxInstance

        val method = superClass.findMethod(expr.method.lexeme)
            ?: throw RuntimeError(expr.method, "Undefined property '${expr.method.lexeme}'.")

        return method.bind(loxObject)
    }

    override fun visitThisExpr(expr: This): Any? {
        return lookUpVariable(expr.keyword, expr)
    }

    override fun visitGroupingExpr(expr: Grouping): Any? {
        return evaluate(expr.expression)
    }

    fun evaluate(expr: Expr) : Any? {
        return expr.accept(this)
    }

    fun execute(stmt: Stmt) {
        stmt.accept(this)
    }

    fun resolve(expr: Expr, depth: Int) {
        locals.put(expr, depth)
    }

    fun executeBlock(statements: List<Stmt>, environment: Environment) {
        val previous = this.environment
        try {
            this.environment = environment

            for (stmt in statements) {
                execute(stmt)
            }
        } finally {
            this.environment = previous;
        }
    }

    override fun visitExpressionStmt(stmt: ExpressionStmt) : Void? {
        evaluate(stmt.expression)
        return null
    }

    override fun visitFunctionStmt(stmt: FunctionStmt): Void? {
        val function = LoxFunction(stmt, environment)
        environment.define(stmt.name.lexeme, function)
        return null
    }

    override fun visitBlockStmt(stmt: BlockStmt): Void? {
        executeBlock(stmt.statements, Environment(environment))
        return null
    }

    override fun visitClassStmt(stmt: ClassStmt): Void? {
        var superClass: Any? = null
        stmt.superClass?.let {
            superClass = evaluate(stmt.superClass)
            if (superClass !is LoxClass) {
                throw RuntimeError(stmt.superClass.name, "Superclass must be a class")
            }
        }

        environment.define(stmt.name.lexeme, null)

        stmt.superClass?.let {
            environment = Environment(environment)
            environment.define("super", superClass)
        }

        val methods = HashMap<String, LoxFunction>()
        for (method in stmt.methods) {
            val function = LoxFunction(method, environment, method.name.lexeme == "init")
            methods[method.name.lexeme] = function
        }

        val klass = if (superClass == null) {
            LoxClass(stmt.name.lexeme, null, methods)
        } else {
            environment = environment.enclosing!!
            LoxClass(stmt.name.lexeme, superClass as LoxClass, methods)
        }
        environment.assign(stmt.name, klass)
        return null
    }

    override fun visitVarStmt(stmt: VarStmt): Void? {
        var value : Any? = null
        stmt.initializer?.let {
            value = evaluate(stmt.initializer)
        }
        environment.define(stmt.name.lexeme, value)
        return null
    }

    override fun visitIfStmt(stmt: IfStmt): Void? {
        if (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.thenBranch)
        } else {
            stmt.elseBranch?.let { elseBranch ->
                execute(elseBranch)
            }
        }
        return null
    }

    override fun visitPrintStmt(stmt: PrintStmt): Void? {
        val value = evaluate(stmt.expression)
        println(stringify(value))
        return null
    }

    override fun visitReturnStmt(stmt: ReturnStmt): Void? {
        var value: Any? = null
        //stmt.value?.let { value = evaluate(it) }
        if (stmt.value != null) {
            value = evaluate(stmt.value)
        }
        throw Return(value)
    }

    override fun visitWhileStmt(stmt: WhileStmt): Void? {
        while(isTruthy(evaluate(stmt.condition))) {
            execute(stmt.body)
        }
        return null
    }

    fun checkNumberOperand(operator: Token, operand: Any?) {
        if (operand is Double) return
        throw RuntimeError(operator, "Operand must be a number")
    }

    fun checkNumberOperands(operator: Token, left: Any?, right: Any?) {
        if (left is Double && right is Double) return
        throw RuntimeError(operator, "Operands must be numbers")
    }

    fun stringify(item: Any?) : String {
        if(item == null) return "nil"

        if (item is Double) {
            var text = item.toString()
            if (text.endsWith(".0")) {
               text = text.substring(0, text.length - 2)
            }
            return text
        }

        return item.toString()
    }

    fun isEqual(a: Any?, b: Any?) : Boolean {
        if (a == null && b== null) return true
        if (a == null) return false
        return a == b
    }

    fun isTruthy(value: Any?) : Boolean {
        if (value == null) return false
        if (value is Boolean) return value as Boolean
        return true
    }

    fun isNotTruthy(value: Any?) : Boolean {
        return !isTruthy(value)
    }
}
