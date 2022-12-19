import TokenType.*

class Interpreter : Expr.Visitor<Any?>, Stmt.Visitor<Void?> {

    val environment = Environment()

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
        return environment.get(expr.name)
    }

    override fun visitAssignExpr(expr: Assign): Any? {
        TODO("Not yet implemented")
    }

    override fun visitUnaryExpr(expr: Unary): Any? {
        val right = evaluate(expr.right)

        if (expr.operator.type == MINUS) {
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

        return null
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

    override fun visitBlockStmt(stmt: BlockStmt): Void? {
        TODO("Not yet implemented")
    }

    override fun visitExpressionStmt(stmt: ExpressionStmt): Void? {
        evaluate(stmt.expression)
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

    override fun visitPrintStmt(stmt: PrintStmt): Void? {
        val value = evaluate(stmt.expression)
        println(stringify(value))
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